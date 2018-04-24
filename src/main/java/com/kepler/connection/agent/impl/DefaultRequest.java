package com.kepler.connection.agent.impl;

import java.util.LinkedHashMap;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.agent.Request;
import com.kepler.connection.agent.RequestHeaders;
import com.kepler.connection.agent.RequestQuery;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DefaultRequest implements Request {

	public static final String FIELD_CATALOG = PropertiesUtils.get(DefaultRequest.class.getName().toLowerCase() + ".field_catalog", "catalog");

	private final LinkedHashMap<String, Object> body;

	private final RequestHeaders headers;

	private final Service service;

	private final String method;

	public DefaultRequest(RequestQuery query) throws Exception {
		this(null, query, null);
	}

	public DefaultRequest(RequestHeaders headers, RequestQuery query) throws Exception {
		this(headers, query, null);
	}

	public DefaultRequest(RequestHeaders headers, RequestQuery query, LinkedHashMap<String, Object> body) throws Exception {
		super();
		this.headers = headers;
		this.body = query.merge(body);
		this.method = query.path(2, "");
		this.service = new Service(query.path(0, ""), query.path(1, ""), headers.headers().get(DefaultRequest.FIELD_CATALOG));
	}

	public LinkedHashMap<String, Object> body() {
		return this.body;
	}

	public RequestHeaders headers() {
		return this.headers;
	}

	public Service service() {
		return this.service;
	}

	public String method() {
		return this.method;
	}
}