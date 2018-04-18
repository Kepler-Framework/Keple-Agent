package com.kepler.connection.agent;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface ResponseProcessor {

	public Object exception(Throwable throwable) throws Exception;

	public Object response(Object response) throws Exception;

	public Service support() throws Exception;
}
