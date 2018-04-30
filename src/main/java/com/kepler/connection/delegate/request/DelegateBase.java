package com.kepler.connection.delegate.request;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.delegate.DelegateRequest;
import com.kepler.host.Host;
import com.kepler.protocol.Request;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
abstract public class DelegateBase implements DelegateRequest {

	private static final String REPLACE_SERVICE = PropertiesUtils.get(DelegateBase.class.getName().toLowerCase() + ".replace_service", "#\\{service\\}");

	private static final String REPLACE_VERSION = PropertiesUtils.get(DelegateBase.class.getName().toLowerCase() + ".replace_version", "#\\{version\\}");

	private static final String REPLACE_CATALOG = PropertiesUtils.get(DelegateBase.class.getName().toLowerCase() + ".replace_catalog", "#\\{catalog\\}");

	private static final String REPLACE_METHOD = PropertiesUtils.get(DelegateBase.class.getName().toLowerCase() + ".replace_method", "#\\{method\\}");

	private static final int TIMEOUT_SOCKET = PropertiesUtils.get(DelegateBase.class.getName().toLowerCase() + ".timeout_socket", 5000);

	private static final int TIMEOUT_CONN = PropertiesUtils.get(DelegateBase.class.getName().toLowerCase() + ".timeout_conn", 5000);

	private static final int TIMEOUT_READ = PropertiesUtils.get(DelegateBase.class.getName().toLowerCase() + ".timeout_read", 5000);

	private final RequestConfig config;

	public DelegateBase() {
		super();
		this.config = RequestConfig.custom().setConnectTimeout(DelegateBase.TIMEOUT_CONN).setConnectionRequestTimeout(DelegateBase.TIMEOUT_READ).setSocketTimeout(DelegateBase.TIMEOUT_SOCKET).build();
	}

	protected HttpUriRequest headers(Request request, HttpUriRequest req) {
		for (String key : request.headers().get().keySet()) {
			req.addHeader(key, request.headers().get().get(key));
		}
		return req;
	}

	protected String url(Request request, Host host) {
		Service service = request.service();
		return host.host().replaceAll(DelegateBase.REPLACE_SERVICE, service.service()).replaceAll(DelegateBase.REPLACE_VERSION, service.version()).replaceAll(DelegateBase.REPLACE_CATALOG, service.catalog()).replaceAll(DelegateBase.REPLACE_METHOD, request.method());
	}

	protected RequestConfig config() {
		return this.config;
	}

}
