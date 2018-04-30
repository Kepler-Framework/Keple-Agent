package com.kepler.connection.delegate.request;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.delegate.DelegateRequest;
import com.kepler.host.Host;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
abstract public class DelegateBase implements DelegateRequest {

	private static final int TIMEOUT_SOCKET = PropertiesUtils.get(DelegatePost.class.getName().toLowerCase() + ".timeout_socket", 5000);

	private static final int TIMEOUT_CONN = PropertiesUtils.get(DelegatePost.class.getName().toLowerCase() + ".timeout_conn", 5000);

	private static final int TIMEOUT_READ = PropertiesUtils.get(DelegatePost.class.getName().toLowerCase() + ".timeout_read", 5000);

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
		return host.host() + "?service=" + request.service().service() + "&method=" + request.method();
	}

	protected RequestConfig config() {
		return this.config;
	}

}
