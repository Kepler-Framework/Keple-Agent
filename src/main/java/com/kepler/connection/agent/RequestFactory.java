package com.kepler.connection.agent;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author KimShen
 *
 */
public interface RequestFactory {

	/**
	 * @param request
	 * @return
	 */
	public Request factory(FullHttpRequest request) throws Exception;
}
