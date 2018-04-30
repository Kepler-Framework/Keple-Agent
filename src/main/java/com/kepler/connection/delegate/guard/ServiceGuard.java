package com.kepler.connection.delegate.guard;

import com.kepler.KeplerGenericException;
import com.kepler.connection.delegate.DelegateBody;
import com.kepler.connection.delegate.DelegateGuard;

/**
 * @author KimShen
 *
 */
public class ServiceGuard implements DelegateGuard {

	@Override
	public DelegateBody guard(String location, DelegateBody body) throws Exception {
		if (body.getServices() == null || body.getServices().isEmpty()) {
			throw new KeplerGenericException("[empty-service][location=" + location + "]");
		}
		return body;
	}

}
