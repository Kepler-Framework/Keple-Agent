package com.kepler.connection.agent.impl;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.agent.Request;
import com.kepler.connection.agent.RequestProcessor;
import com.kepler.connection.json.Json;
import com.kepler.connection.stream.WrapInputStream;
import com.kepler.header.HeadersContext;
import com.kepler.service.Service;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author KimShen
 *
 */
public class DefaultRequestProcessor implements RequestProcessor {

	private static final String FIELD_CLASSES = PropertiesUtils.get(DefaultRequestProcessor.class.getName().toLowerCase() + ".field_classes", "classes");

	private static final String FIELD_METHOD = PropertiesUtils.get(DefaultRequestProcessor.class.getName().toLowerCase() + ".field_method", "method");

	private static final String FIELD_ARGS = PropertiesUtils.get(DefaultRequestProcessor.class.getName().toLowerCase() + ".field_args", "args");

	private final HeadersContext headers;

	private final Json json;

	public DefaultRequestProcessor(HeadersContext headers, Json json) {
		super();
		this.headers = headers;
		this.json = json;
	}

	@Override
	public Request process(FullHttpRequest request) throws Exception {
		return new HttpRequest(request);
	}

	@Override
	public boolean support(FullHttpRequest request) throws Exception {
		return true;
	}

	private class HttpRequest implements Request {

		private final Map<String, Object> content;

		private final URI uri;

		@SuppressWarnings("unchecked")
		private HttpRequest(FullHttpRequest request) throws Exception {
			super();
			try (WrapInputStream input = new WrapInputStream(request.content())) {
				this.content = input.available() > 0 ? Map.class.cast(DefaultRequestProcessor.this.json.read(input, Map.class)) : Request.EMPTY_CONTENT;
				this.uri = new URI(request.getUri());
				this.headers(this.uri);
			}
		}

		private HttpRequest headers(URI uri) throws Exception {
			if (!StringUtils.isEmpty(uri.getQuery())) {
				// 填充URL Query
				for (String meta : uri.getQuery().split("&")) {
					String[] pair = meta.split("=");
					DefaultRequestProcessor.this.headers.get().put(pair[0], pair[1]);
				}
			}
			return this;
		}

		public String[] classes() throws Exception {
			@SuppressWarnings("unchecked")
			List<String> clazz = List.class.cast(this.content.get(DefaultRequestProcessor.FIELD_CLASSES));
			return clazz != null ? clazz.toArray(new String[] {}) : Request.EMPTY_CLASSES;
		}

		public Service service() throws Exception {
			String[] service = this.uri.getPath().split("/");
			return service.length >= 4 ? new Service(service[1], service[2], service[3]) : new Service(service[1], service[2]);
		}

		public String method() throws Exception {
			return String.class.cast(this.content.get(DefaultRequestProcessor.FIELD_METHOD));
		}

		public Object[] args() throws Exception {
			@SuppressWarnings("unchecked")
			List<Object> args = List.class.cast(this.content.get(DefaultRequestProcessor.FIELD_ARGS));
			return args != null ? args.toArray(new Object[] {}) : Request.EMPTY_ARGS;
		}
	}
}
