package com.kepler.connection.agent.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.ResponseListener;
import com.kepler.connection.agent.Request;
import com.kepler.connection.agent.RequestAuth;
import com.kepler.connection.agent.RequestFactory;
import com.kepler.connection.agent.RequestGuard;
import com.kepler.connection.agent.ResponseFactory;
import com.kepler.connection.impl.DefaultChannelFactory;
import com.kepler.connection.impl.ExceptionListener;
import com.kepler.connection.json.Json;
import com.kepler.connection.stream.WrapOutputStream;
import com.kepler.generic.reflect.GenericService;
import com.kepler.header.HeadersContext;
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
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.ReferenceCountUtil;

/**
 * @author KimShen
 *
 */
public class DefaultAgent {

	private static final int EVENTLOOP_PARENT = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".eventloop_parent", Runtime.getRuntime().availableProcessors() * 2);

	private static final int EVENTLOOP_CHILD = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".eventloop_child", Runtime.getRuntime().availableProcessors() * 10);

	private static final int BUFFER_SEND = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".buffer_send", Integer.MAX_VALUE);

	private static final int BUFFER_RECV = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".buffer_recv", Integer.MAX_VALUE);

	private static final int MAX_INITLINE = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".max_initline", Integer.MAX_VALUE);

	private static final int MAX_LENGTH = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".max_length", Integer.MAX_VALUE);

	private static final int MAX_HEADER = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".max_header", Integer.MAX_VALUE);

	private static final int MAX_CHUNK = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".max_chunk", Integer.MAX_VALUE);

	private static final String BINDING = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".bind", "0.0.0.0");

	private static final boolean POOLED = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".pooled", true);

	private static final int PORT = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".port", 8080);

	private static final Set<Cookie> EMPTY = Collections.unmodifiableSet(new HashSet<Cookie>());

	private static final Log LOGGER = LogFactory.getLog(DefaultAgent.class);

	private final ByteBufAllocator allocator = DefaultAgent.POOLED ? PooledByteBufAllocator.DEFAULT : UnpooledByteBufAllocator.DEFAULT;

	private final InitializerFactory inits = new InitializerFactory();

	private final ServerBootstrap bootstrap = new ServerBootstrap();

	private final RequestHandler handler = new RequestHandler();

	private final ThreadPoolExecutor executor;

	private final HeadersContext headers;

	private final GenericService generic;

	private final ResponseFactory resp;

	private final RequestFactory resq;

	private final RequestGuard guard;

	private final RequestAuth auth;

	private final Json json;

	public DefaultAgent(ThreadPoolExecutor executor, GenericService generic, HeadersContext headers, RequestGuard guard, ResponseFactory resp, RequestFactory resq, RequestAuth auth, Json json) {
		this.executor = executor;
		this.generic = generic;
		this.headers = headers;
		this.guard = guard;
		this.resp = resp;
		this.resq = resq;
		this.json = json;
		this.auth = auth;
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

		private Request request;

		private ByteBuf buffer;

		private long running;

		private long created;

		private long remote;

		private InvokeRunnable(ChannelHandlerContext ctx, FullHttpRequest req) {
			super();
			this.req = req;
			this.ctx = ctx;
			this.created = System.currentTimeMillis();
		}

		/**
		 * @param response
		 * @return
		 * @throws Exception
		 */
		private FullHttpResponse response(Object response) throws Exception {
			try (WrapOutputStream output = WrapOutputStream.class.cast(DefaultAgent.this.json.write(new WrapOutputStream(this.buffer), response))) {
				DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, output.buffer());
				res.headers().add(HttpHeaders.CONTENT_LENGTH, output.buffer().readableBytes());
				res.headers().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
				res.headers().add(HttpHeaders.CONNECTION, "keep-alive");
				this.remote = System.currentTimeMillis();
				return res;
			}
		}

		private InvokeRunnable prepare() throws Exception {
			try {
				// 解析Request并准备Header
				DefaultAgent.this.headers.get().put((this.request = DefaultAgent.this.resq.factory(this.req)).headers().headers());
				this.buffer = DefaultAgent.this.allocator.directBuffer();
				this.running = System.currentTimeMillis();
				return this;
			} catch (Exception e) {
				DefaultAgent.LOGGER.error(e.getMessage(), e);
				throw e;
			}
		}

		private Object generic() throws Throwable {
			String method = this.request.method();
			Service service = this.request.service();
			return DefaultAgent.this.generic.invoke(service, method, this.request.body());
		}

		private ResponseListener listener() {
			return new ResponseListener(this.req.getUri(), this.created, this.running, this.remote);
		}

		private InvokeRunnable release() {
			if (this.buffer == null) {
				return this;
			}
			// 异常, 释放ByteBuf
			if (this.buffer.refCnt() > 0) {
				ReferenceCountUtil.release(this.buffer);
			}
			return this;
		}

		private InvokeRunnable reset() {
			this.buffer.writerIndex(0);
			return this;
		}

		private void running() throws Exception {
			try {
				this.auth();
				// 请求校验
				DefaultAgent.this.guard.guard(this.request);
				Object respones_process = DefaultAgent.this.resp.response(this.request.service(), this.generic());
				Object response_netty = this.response(respones_process);
				this.ctx.writeAndFlush(response_netty).addListener(this.listener());
			} catch (NullPointerException e) {
				Object response = this.reset().response(DefaultAgent.this.resp.throwable(this.request.service(), "NPE"));
				this.ctx.writeAndFlush(response).addListener(this.listener());
			} catch (Throwable e) {
				Object response = this.reset().response(DefaultAgent.this.resp.throwable(this.request.service(), e));
				this.ctx.writeAndFlush(response).addListener(this.listener());
			}
		}

		private void auth() throws Exception {
			String cookie = this.req.headers().get(HttpHeaders.COOKIE);
			DefaultAgent.this.auth.auth(this.req.getUri(), this.req.getMethod().name(), StringUtils.isEmpty(cookie) ? DefaultAgent.EMPTY : ServerCookieDecoder.STRICT.decode(cookie), this.req.headers());
		}

		@Override
		public void run() {
			try {
				this.prepare();
				this.running();
			} catch (Throwable e) {
				DefaultAgent.LOGGER.error(e.getMessage(), e);
				this.release();
			} finally {
				DefaultAgent.this.headers.release();
			}
		}
	}
}
