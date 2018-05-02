package com.kepler.connection.agent.guard;

import com.kepler.KeplerGenericException;
import com.kepler.connection.agent.Request;
import com.kepler.connection.agent.RequestGuard;
import com.kepler.org.apache.commons.lang.StringUtils;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class ServiceGuard implements RequestGuard {

	@Override
	public void guard(Request request) throws Exception {
		Service service = request.service();
		if (service == null) {
			throw new KeplerGenericException("[guard-service][null]");
		}
		if (StringUtils.isEmpty(service.service())) {
			throw new KeplerGenericException("[guard-service][service]");
		}
	}

}
