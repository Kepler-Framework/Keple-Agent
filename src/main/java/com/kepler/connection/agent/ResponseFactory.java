package com.kepler.connection.agent;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface ResponseFactory {

	public Object response(Service service, Object resp) throws Exception;
	
	public Object throwable(Service service, Throwable throwable) throws Exception;
}
