package com.kepler.connection.delegate.guard;

import com.kepler.KeplerGenericException;
import com.kepler.connection.delegate.DelegateBody;
import com.kepler.connection.delegate.DelegateGuard;
import com.kepler.org.apache.commons.lang.StringUtils;

/**
 * @author KimShen
 *
 */
public class AvailableGuard implements DelegateGuard {

	@Override
	public DelegateBody guard(String location, DelegateBody body) throws Exception {
		if (StringUtils.isEmpty(body.getRoot())) {
			throw new KeplerGenericException("[unavailable-host][location=" + location + "]");
		}
		return body;
	}
}
