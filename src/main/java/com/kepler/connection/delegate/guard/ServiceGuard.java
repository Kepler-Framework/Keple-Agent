package com.kepler.connection.delegate.guard;

import com.kepler.KeplerGenericException;
import com.kepler.connection.delegate.DelegateGuard;
import com.kepler.connection.delegate.DelegateResponse;

/**
 * @author KimShen
 *
 */
public class ServiceGuard implements DelegateGuard {

	@Override
	public DelegateResponse guard(String location, DelegateResponse response) throws Exception {
		if (response.getData().getServices() == null || response.getData().getServices().isEmpty()) {
			throw new KeplerGenericException("[empty-service][location=" + location + "]");
		}
		return response;
	}

}
