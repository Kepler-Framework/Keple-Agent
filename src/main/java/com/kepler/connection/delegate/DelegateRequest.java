package com.kepler.connection.delegate;

import org.apache.http.client.methods.HttpUriRequest;

import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public interface DelegateRequest {

	public HttpUriRequest request(Request request, DelegateHost host) throws Exception;
}
