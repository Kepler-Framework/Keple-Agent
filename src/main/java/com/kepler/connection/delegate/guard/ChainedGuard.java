package com.kepler.connection.delegate.guard;

import java.util.List;

import com.kepler.connection.delegate.DelegateBody;
import com.kepler.connection.delegate.DelegateGuard;

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
	public boolean guard(String location, DelegateBody body) {
		for (DelegateGuard each : this.guards) {
			if (!each.guard(location, body)) {
				return false;
			}
		}
		return true;
	}

}
