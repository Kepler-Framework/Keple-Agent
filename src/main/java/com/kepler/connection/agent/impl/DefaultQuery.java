package com.kepler.connection.agent.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.connection.agent.RequestQuery;
import com.kepler.org.apache.commons.lang.StringUtils;

/**
 * @author KimShen
 *
 */
public class DefaultQuery implements RequestQuery {

	private static final Log LOGGER = LogFactory.getLog(DefaultQuery.class);

	private final LinkedHashMap<String, Object> param;

	private final String[] path;

	public DefaultQuery(URI uri) throws Exception {
		super();
		this.path = uri.getPath().split("/");
		// 拆解Get参数
		if (!StringUtils.isEmpty(uri.getQuery())) {
			this.param = new LinkedHashMap<String, Object>();
			for (String each : uri.getQuery().split("&")) {
				String[] pair = each.split("=");
				DefaultQuery.put(this.param, pair[0], pair.length != 1 ? pair[1] : null);
			}
		} else {
			this.param = null;
		}
	}

	@SuppressWarnings("unchecked")
	private static void put(LinkedHashMap<String, Object> param, String key, String value) throws Exception {
		if (!key.contains(".")) {
			param.put(key, value);
			return;
		}
		try {
			// 当前取值路径
			StringBuffer path = new StringBuffer();
			String[] key_each = key.split("\\.");
			for (int index = 0; index < key_each.length - 1; index++) {
				// Root Path
				String each = key_each[index];
				if (path.length() == 0) {
					if (!param.containsKey(each)) {
						param.put(each, new HashMap<String, Object>());
					}
				} else {
					Map<String, Object> inner = Map.class.cast(PropertyUtils.getProperty(param, path.toString()));
					if (!inner.containsKey(each)) {
						inner.put(each, new HashMap<String, Object>());
					}
				}
				path.append(path.length() == 0 ? each : path.toString() + each);
			}
			PropertyUtils.setProperty(param, key, value);
		} catch (Exception e) {
			DefaultQuery.LOGGER.info("[query-error][key=" + key + "][message=" + e.getMessage() + "]", e);
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
