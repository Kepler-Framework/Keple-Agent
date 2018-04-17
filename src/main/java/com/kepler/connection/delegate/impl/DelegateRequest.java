package com.kepler.connection.delegate.impl;

import com.kepler.header.Headers;

/**
 * @author KimShen
 *
 */
public class DelegateRequest {

	private final Headers headers;

	private final Object[] args;

	public DelegateRequest(Headers headers, Object[] args) {
		super();
		this.headers = headers;
		this.args = args;
	}

	public Headers getHeaders() {
		return this.headers;
	}

	public Object[] getArgs() {
		return this.args;
	}
}
