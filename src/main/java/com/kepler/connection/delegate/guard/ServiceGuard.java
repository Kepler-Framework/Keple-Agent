package com.kepler.connection.delegate.guard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.connection.delegate.DelegateBody;
import com.kepler.connection.delegate.DelegateGuard;

/**
 * @author KimShen
 *
 */
public class ServiceGuard implements DelegateGuard {

	private static final Log LOGGER = LogFactory.getLog(ServiceGuard.class);

	@Override
	public boolean guard(String location, DelegateBody body) {
		if (body.getServices() == null || body.getServices().isEmpty()) {
			ServiceGuard.LOGGER.warn("[guard-service-empty][location=" + location + "]");
			return false;
		}
		return true;
	}

}
