package com.kepler.connection.agent;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface RespFactory {

	public Object resp(Service service, Object resp);
}
