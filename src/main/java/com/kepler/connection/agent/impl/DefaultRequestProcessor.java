package com.kepler.connection.agent.impl;

import java.net.URI;
import java.util.Map;

import com.kepler.connection.agent.Request;
import com.kepler.connection.agent.RequestProcessor;
import com.kepler.connection.json.Json;
import com.kepler.connection.stream.WrapInputStream;
import com.kepler.header.HeadersContext;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author KimShen
 *
 */
public class DefaultRequestProcessor implements RequestProcessor {

	private final HeadersContext headers;

	private final Json json;

	public DefaultRequestProcessor(HeadersContext headers, Json json) {
		super();
		this.headers = headers;
		this.json = json;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Request process(FullHttpRequest request) throws Exception {
		try (WrapInputStream input = new WrapInputStream(request.content())) {
			Map<String, Object> content = input.available() > 0 ? Map.class.cast(DefaultRequestProcessor.this.json.read(input, Map.class)) : Request.EMPTY_CONTENT;
			URI uri = new URI(request.getUri());
			return new DefaultRequest(uri, this.headers.get(), content);
		}
	}

	@Override
	public boolean support(FullHttpRequest request) throws Exception {
		return true;
	}
}
