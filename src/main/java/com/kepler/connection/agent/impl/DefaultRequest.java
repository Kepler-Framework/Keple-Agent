package com.kepler.connection.agent.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.agent.Request;
import com.kepler.header.Headers;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DefaultRequest implements Request {

	public static final String FIELD_CLASSES = PropertiesUtils.get(DefaultRequest.class.getName().toLowerCase() + ".field_classes", "classes");

	public static final String FIELD_METHOD = PropertiesUtils.get(DefaultRequest.class.getName().toLowerCase() + ".field_method", "method");

	public static final String FIELD_ARGS = PropertiesUtils.get(DefaultRequest.class.getName().toLowerCase() + ".field_args", "args");

	private final Map<String, Object> content;

	private final Headers headers;

	private final URI uri;
	
	public DefaultRequest(URI uri, Headers headers) throws Exception {
		super();
		this.headers = headers;
		this.headers(this.uri = uri);
		this.content = new HashMap<String, Object>();
	}

	public DefaultRequest(URI uri, Headers headers, Map<String, Object> content) throws Exception {
		super();
		this.headers = headers;
		this.content = content;
		this.headers(this.uri = uri);
	}

	private DefaultRequest headers(URI uri) throws Exception {
		if (!StringUtils.isEmpty(uri.getQuery())) {
			// 填充URL Query
			for (String meta : uri.getQuery().split("&")) {
				String[] pair = meta.split("=");
				this.headers.put(pair[0], pair[1]);
			}
		}
		return this;
	}

	public String[] classes() throws Exception {
		@SuppressWarnings("unchecked")
		List<String> clazz = List.class.cast(this.content.get(DefaultRequest.FIELD_CLASSES));
		return clazz != null ? clazz.toArray(new String[] {}) : Request.EMPTY_CLASSES;
	}

	public Service service() throws Exception {
		String[] service = this.uri.getPath().split("/");
		return service.length >= 4 ? new Service(service[1], service[2], service[3]) : new Service(service[1], service[2]);
	}

	public String method() throws Exception {
		return String.class.cast(this.content.get(DefaultRequest.FIELD_METHOD));
	}

	public Object[] args() throws Exception {
		@SuppressWarnings("unchecked")
		List<Object> args = List.class.cast(this.content.get(DefaultRequest.FIELD_ARGS));
		return args != null ? args.toArray(new Object[] {}) : Request.EMPTY_ARGS;
	}
}