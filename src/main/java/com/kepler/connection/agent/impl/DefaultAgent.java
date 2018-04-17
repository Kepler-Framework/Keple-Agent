package com.kepler.connection.agent.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.agent.RespFactory;
import com.kepler.connection.impl.DefaultChannelFactory;
import com.kepler.connection.impl.ExceptionListener;
import com.kepler.connection.stream.WrapInputStream;
import com.kepler.connection.stream.WrapOutputStream;
import com.kepler.generic.reflect.GenericService;
import com.kepler.header.HeadersContext;
import com.kepler.header.impl.TraceContext;
import com.kepler.service.Service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContentEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;

/**
 * @author KimShen
 *
 */
public class DefaultAgent {

	private static final int EVENTLOOP_CHILD = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".eventloop_child", Runtime.getRuntime().availableProcessors() * 2);

	private static final int EVENTLOOP_PARENT = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".eventloop_parent", 1);

	private static final int BUFFER_SEND = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".buffer_send", Integer.MAX_VALUE);

	private static final int BUFFER_RECV = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".buffer_recv", Integer.MAX_VALUE);

	private static final int MAX_INITLINE = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".max_initline", Integer.MAX_VALUE);

	private static final int MAX_LENGTH = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".max_length", Integer.MAX_VALUE);

	private static final int MAX_HEADER = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".max_header", Integer.MAX_VALUE);

	private static final int MAX_CHUNK = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".max_chunk", Integer.MAX_VALUE);

	private static final String FIELD_CLASSES = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".field_classes", "classes");

	private static final String FIELD_METHOD = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".field_method", "method");

	private static final String FIELD_ARGS = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".field_args", "args");

	private static final String BINDING = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".binding", "0.0.0.0");

	private static final boolean POOLED = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".pooled", true);

	private static final int PORT = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".port", 8080);

	private static final Map<String, Object> EMPTY_CONTENT = new HashMap<String, Object>();

	private static final Log LOGGER = LogFactory.getLog(DefaultAgent.class);

	private static final String[] EMPTY_CLASSES = new String[] {};

	private static final Object[] EMPTY_ARGS = new Object[] {};

	private final ByteBufAllocator allocator = DefaultAgent.POOLED ? PooledByteBufAllocator.DEFAULT : UnpooledByteBufAllocator.DEFAULT;

	private final InitializerFactory inits = new InitializerFactory();

	private final ServerBootstrap bootstrap = new ServerBootstrap();

	private final RequestHandler handler = new RequestHandler();

	private final ObjectMapper mapper = new ObjectMapper();

	private final ThreadPoolExecutor executor;

	private final HeadersContext headers;

	private final GenericService generic;

	private final ObjectWriter writer;

	private final ObjectReader reader;

	private final RespFactory resp;

	public DefaultAgent(ThreadPoolExecutor executor, HeadersContext headers, GenericService generic, RespFactory resp) {
		this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		this.mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		this.writer = this.mapper.writerWithType(DefaultResp.class);
		this.reader = this.mapper.reader(Map.class);
		this.executor = executor;
		this.generic = generic;
		this.headers = headers;
		this.resp = resp;
	}

	/**
	 * For Spring
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		// 服务配置(绑定端口,SO_REUSEADDR=true)
		this.bootstrap.group(new NioEventLoopGroup(DefaultAgent.EVENTLOOP_PARENT), new NioEventLoopGroup(DefaultAgent.EVENTLOOP_CHILD)).channelFactory(DefaultChannelFactory.INSTANCE_SERVER).childHandler(this.inits.factory()).option(ChannelOption.SO_REUSEADDR, true).bind(DefaultAgent.BINDING, DefaultAgent.PORT).sync();
		DefaultAgent.LOGGER.info("Server " + DefaultAgent.PORT + " started ... ");
	}

	/**
	 * For Spring
	 * 
	 * @throws Exception
	 */
	public void destroy() throws Exception {
		this.bootstrap.childGroup().shutdownGracefully().sync();
		this.bootstrap.group().shutdownGracefully().sync();
		DefaultAgent.LOGGER.warn("Server shutdown ... ");
	}

	private class InitializerFactory {

		public ChannelInitializer<SocketChannel> factory() {
			return new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel channel) throws Exception {
					channel.config().setSendBufferSize(DefaultAgent.BUFFER_SEND);
					channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
					channel.config().setReceiveBufferSize(DefaultAgent.BUFFER_RECV);
					channel.pipeline().addLast(new HttpRequestDecoder(DefaultAgent.MAX_INITLINE, DefaultAgent.MAX_HEADER, DefaultAgent.MAX_CHUNK));
					channel.pipeline().addLast(new HttpObjectAggregator(DefaultAgent.MAX_LENGTH));
					channel.pipeline().addLast(new HttpResponseEncoder());
					channel.pipeline().addLast(new HttpContentEncoderImpl());
					channel.pipeline().addLast(DefaultAgent.this.handler);
				}
			};
		}
	}

	/**
	 * @author KimShen
	 *
	 */
	@Sharable
	private class RequestHandler extends ChannelInboundHandlerAdapter {

		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			DefaultAgent.LOGGER.info("Connect active (" + ctx.channel().localAddress().toString() + " to " + ctx.channel().remoteAddress().toString() + ") ...");
			ctx.fireChannelActive();
		}

		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			DefaultAgent.LOGGER.info("Connect inactive (" + ctx.channel().localAddress().toString() + " to " + ctx.channel().remoteAddress().toString() + ") ...");
			ctx.fireChannelInactive();
		}

		// 任何未捕获异常(如OOM)均需要终止通道
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			DefaultAgent.LOGGER.error(cause.getMessage(), cause);
			ctx.close().addListener(ExceptionListener.listener(ctx));
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
			DefaultAgent.this.executor.execute(new InvokeRunnable(ctx, FullHttpRequest.class.cast(message)));
		}
	}

	private class HttpContentEncoderImpl extends HttpContentEncoder {

		protected Result beginEncode(HttpResponse headers, String encoding) throws Exception {
			return null;
		}
	}

	private class InvokeRunnable implements Runnable {

		private final ChannelHandlerContext ctx;

		private final FullHttpRequest req;

		private ByteBuf buf;

		private InvokeRunnable(ChannelHandlerContext ctx, FullHttpRequest req) {
			super();
			this.req = req;
			this.ctx = ctx;
		}

		/**
		 * @param response
		 * @return
		 * @throws Exception
		 */
		private FullHttpResponse response(Object response) throws Exception {
			try (WrapOutputStream output = new WrapOutputStream(this.buf)) {
				DefaultAgent.this.writer.writeValue(output, response);
				DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, output.buffer());
				res.headers().add(HttpHeaders.CONTENT_LENGTH, output.buffer().readableBytes());
				res.headers().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
				res.headers().add(HttpHeaders.CONNECTION, "keep-alive");
				return res;
			}
		}

		private InvokeRunnable allocate() {
			this.buf = DefaultAgent.this.allocator.buffer();
			return this;
		}

		private InvokeRunnable release() {
			if (this.buf == null) {
				return this;
			}
			// 异常, 释放ByteBuf
			if (this.buf.refCnt() > 0) {
				ReferenceCountUtil.release(this.buf);
			}
			return this;
		}

		private InvokeRunnable reset() {
			this.buf.writerIndex(0);
			return this;
		}

		@Override
		public void run() {
			try {
				HttpRequest request = new HttpRequest(this.allocate().req);
				try (WrapInputStream input = new WrapInputStream(request.buffer())) {
					Object resp = DefaultAgent.this.resp.resp(request.service(), DefaultAgent.this.generic.invoke(request.service(), request.method(), request.classes(), request.args()));
					this.ctx.writeAndFlush(this.response(resp)).addListener(ExceptionListener.listener(this.ctx, TraceContext.getTraceOnCreate()));
				}
			} catch (Throwable root) {
				try {
					this.ctx.writeAndFlush(this.reset().response(new DefaultResp(root, TraceContext.getTraceOnCreate()))).addListener(ExceptionListener.listener(this.ctx, TraceContext.getTraceOnCreate()));
				} catch (Throwable inner) {
					this.release();
				}
			} finally {
				DefaultAgent.this.headers.release();
			}
		}
	}

	private class HttpRequest {

		private final Map<String, Object> content;

		private final FullHttpRequest request;

		private final URI uri;

		@SuppressWarnings("unchecked")
		private HttpRequest(FullHttpRequest request) throws Exception {
			super();
			try (WrapInputStream input = new WrapInputStream(request.content())) {
				this.content = input.available() > 0 ? Map.class.cast(DefaultAgent.this.reader.readValue(input)) : DefaultAgent.EMPTY_CONTENT;
				this.uri = new URI((this.request = request).getUri());
				this.headers(this.uri);
			}
		}

		private HttpRequest headers(URI uri) throws Exception {
			if (!StringUtils.isEmpty(uri.getQuery())) {
				// 填充URL Query
				for (String meta : uri.getQuery().split("&")) {
					String[] pair = meta.split("=");
					DefaultAgent.this.headers.get().put(pair[0], pair[1]);
				}
			}
			return this;
		}

		public String[] classes() throws Exception {
			@SuppressWarnings("unchecked")
			List<String> clazz = List.class.cast(this.content.get(DefaultAgent.FIELD_CLASSES));
			return clazz != null ? clazz.toArray(new String[] {}) : DefaultAgent.EMPTY_CLASSES;
		}

		public Service service() throws Exception {
			String[] service = this.uri.getPath().split("/");
			return service.length >= 4 ? new Service(service[1], service[2], service[3]) : new Service(service[1], service[2]);
		}

		public ByteBuf buffer() throws Exception {
			return this.request.content();
		}

		public String method() throws Exception {
			return String.class.cast(this.content.get(DefaultAgent.FIELD_METHOD));
		}

		public Object[] args() throws Exception {
			@SuppressWarnings("unchecked")
			List<Object> args = List.class.cast(this.content.get(DefaultAgent.FIELD_ARGS));
			return args != null ? args.toArray(new Object[] {}) : DefaultAgent.EMPTY_ARGS;
		}
	}
}
