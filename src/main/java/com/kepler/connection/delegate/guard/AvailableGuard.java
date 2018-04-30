package com.kepler.connection.delegate.guard;

import com.kepler.KeplerGenericException;
import com.kepler.connection.delegate.DelegateGuard;
import com.kepler.connection.delegate.DelegateResponse;
import com.kepler.org.apache.commons.lang.StringUtils;

/**
 * @author KimShen
 *
 */
public class AvailableGuard implements DelegateGuard {

	@Override
	public DelegateResponse guard(String location, DelegateResponse response) throws Exception {
		if (StringUtils.isEmpty(response.getData().getRoot())) {
			throw new KeplerGenericException("[unavailable-host][location=" + location + "]");
		}
		return response;
	}
}
