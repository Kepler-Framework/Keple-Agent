package com.kepler.connection.delegate.guard;

import java.util.List;

import com.kepler.connection.delegate.DelegateGuard;
import com.kepler.connection.delegate.DelegateResponse;

/**
 * @author KimShen
 *
 */
public class ChainedGuard implements DelegateGuard {

	private final List<DelegateGuard> guards;

	public ChainedGuard(List<DelegateGuard> guards) {
		super();
		this.guards = guards;
	}

	@Override
	public DelegateResponse guard(String location, DelegateResponse response) throws Exception {
		for (DelegateGuard each : this.guards) {
			each.guard(location, response);
		}
		return response;
	}

}
