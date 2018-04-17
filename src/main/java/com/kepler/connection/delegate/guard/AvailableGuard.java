package com.kepler.connection.delegate.guard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.kepler.connection.delegate.DelegateBody;
import com.kepler.connection.delegate.DelegateGuard;

/**
 * @author KimShen
 *
 */
public class AvailableGuard implements DelegateGuard {

	private static final Log LOGGER = LogFactory.getLog(AvailableGuard.class);

	@Override
	public boolean guard(String location, DelegateBody body) {
		if (StringUtils.isEmpty(body.getRoot())) {
			AvailableGuard.LOGGER.warn("[guard-host][location=" + location + "]");
			return false;
		}
		return true;
	}
}
