package com.kepler.connection.agent.impl;

import java.net.URI;
import java.util.LinkedHashMap;

import com.kepler.connection.agent.Request;
import com.kepler.connection.agent.RequestProcessor;
import com.kepler.connection.json.Json;
import com.kepler.connection.stream.WrapInputStream;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author KimShen
 *
 */
public class DefaultRequestProcessor implements RequestProcessor {

	private final Json json;

	public DefaultRequestProcessor(Json json) {
		super();
		this.json = json;
	}

	@Override
	public Request process(FullHttpRequest request) throws Exception {
		try (WrapInputStream input = new WrapInputStream(request.content())) {
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> content = input.available() > 0 ? DefaultRequestProcessor.this.json.read(input, LinkedHashMap.class) : null;
			return new DefaultRequest(new DefaultHeaders(request.headers()), new DefaultQuery(new URI(request.getUri())), content);
		}
	}

	@Override
	public boolean support(FullHttpRequest request) throws Exception {
		return true;
	}
}
