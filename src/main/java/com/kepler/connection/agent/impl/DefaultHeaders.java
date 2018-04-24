package com.kepler.connection.agent.impl;

import java.util.HashMap;
import java.util.Map;

import com.kepler.connection.agent.RequestHeaders;

import io.netty.handler.codec.http.HttpHeaders;

/**
 * @author KimShen
 *
 */
public class DefaultHeaders implements RequestHeaders {

	private final Map<String, String> headers = new HashMap<String, String>();

	public DefaultHeaders(HttpHeaders headers) {
		for (String name : headers.names()) {
			this.headers.put(name, headers.get(name));
		}
	}

	public RequestHeaders put(Map<String, String> headers) {
		this.headers.putAll(headers);
		return this;
	}

	@Override
	public Map<String, String> headers() {
		return this.headers;
	}

}
