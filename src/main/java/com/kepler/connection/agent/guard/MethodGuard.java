package com.kepler.connection.agent.guard;

import org.springframework.util.StringUtils;

import com.kepler.KeplerGenericException;
import com.kepler.connection.agent.Request;
import com.kepler.connection.agent.RequestGuard;

/**
 * @author KimShen
 *
 */
public class MethodGuard implements RequestGuard {

	@Override
	public void guard(Request request) throws Exception {
		if (StringUtils.isEmpty(request.method())) {
			throw new KeplerGenericException("[guard-method]" + request.service());
		}
	}

}
