package com.kepler.connection.delegate.guard;

import com.kepler.KeplerGenericException;
import com.kepler.connection.ResponseStatus;
import com.kepler.connection.delegate.DelegateGuard;
import com.kepler.connection.delegate.DelegateResponse;

/**
 * @author KimShen
 *
 */
public class CodeGuard implements DelegateGuard {

	@Override
	public DelegateResponse guard(String location, DelegateResponse response) throws Exception {
		if (ResponseStatus.SUCCESS.code() != response.getErrno()) {
			throw new KeplerGenericException("[error-code][code=" + response.getErrno() + "]");
		}
		return response;
	}

}
