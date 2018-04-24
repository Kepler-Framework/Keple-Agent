package com.kepler.connection.agent.impl;

import java.net.URI;
import java.util.LinkedHashMap;

import org.springframework.util.StringUtils;

import com.kepler.connection.agent.RequestQuery;

/**
 * @author KimShen
 *
 */
public class DefaultQuery implements RequestQuery {

	private final LinkedHashMap<String, Object> param;

	private final String[] path;

	public DefaultQuery(URI uri) {
		super();
		this.path = uri.getPath().split("/");
		if (!StringUtils.isEmpty(uri.getQuery())) {
			this.param = new LinkedHashMap<String, Object>();
			for (String each : uri.getQuery().split("&")) {
				String[] pair = each.split("=");
				this.param.put(pair[0], pair[1]);
			}
		} else {
			this.param = null;
		}
	}

	public LinkedHashMap<String, Object> merge(LinkedHashMap<String, Object> body) {
		if (body == null) {
			return this.param;
		}
		if (this.param == null) {
			return body;
		}
		body.putAll(this.param);
		return body;
	}

	public String path(int index, String def) {
		if (this.path == null) {
			return def;
		}
		int actual = index + 1;
		return actual >= this.path.length ? def : this.path[actual];
	}

	@Override
	public String path(int index) {
		return this.path(index, null);
	}
}
