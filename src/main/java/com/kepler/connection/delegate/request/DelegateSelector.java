package com.kepler.connection.delegate.request;

import org.apache.http.client.methods.HttpUriRequest;

import com.kepler.connection.delegate.DelegateHost;
import com.kepler.connection.delegate.DelegateHttp;
import com.kepler.connection.delegate.DelegateRequest;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public class DelegateSelector implements DelegateRequest {

	private final DelegateRequest post;

	private final DelegateRequest get;

	public DelegateSelector(DelegateRequest post, DelegateRequest get) {
		super();
		this.post = post;
		this.get = get;
	}

	@Override
	public HttpUriRequest request(Request request, DelegateHost host) throws Exception {
		return host.http().equals(DelegateHttp.GET) ? this.get.request(request, host) : this.post.request(request, host);
	}
}
