package com.kepler.connection.agent;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author KimShen
 *
 */
public interface RequestProcessor {
	
	public Request process(FullHttpRequest request) throws Exception;

	public boolean support(FullHttpRequest request) throws Exception;
}
