package com.kepler.connection.agent.impl;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import com.kepler.connection.agent.Request;
import com.kepler.connection.agent.RequestParser;
import com.kepler.connection.agent.RequestProcessor;
import com.kepler.connection.agent.RequestURI;
import com.kepler.connection.stream.WrapInputStream;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author KimShen
 *
 */
public class DefaultRequestProcessor implements RequestProcessor {

	private final List<RequestParser> parser;

	private final RequestParser def;

	private final RequestURI uri;

	public DefaultRequestProcessor(List<RequestParser> parser, RequestParser def, RequestURI uri) {
		super();
		this.parser = parser;
		this.def = def;
		this.uri = uri;
	}

	private LinkedHashMap<String, Object> content(WrapInputStream input, String type) throws IOException, Exception {
		if (input.available() <= 0) {
			return null;
		}
		for (RequestParser each : this.parser) {
			if (each.support(type)) {
				return each.parse(input);
			}
		}
		return this.def.parse(input);
	}

	@Override
	public Request process(FullHttpRequest request) throws Exception {
		try (WrapInputStream input = new WrapInputStream(request.content())) {
			String content_type = request.headers().get(HttpHeaders.CONTENT_TYPE);
			return new DefaultRequest(new DefaultHeaders(request.headers()), new DefaultQuery(this.uri.uri(new URI(request.getUri()))), this.content(input, StringUtils.isEmpty(content_type) ? "" : content_type));
		}
	}

	@Override
	public boolean support(FullHttpRequest request) throws Exception {
		return true;
	}
}
