package com.kepler.connection.agent;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface RespProcessor {

	public Object process(Object resp);

	public Service support();
}
