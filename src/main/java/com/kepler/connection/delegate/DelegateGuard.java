package com.kepler.connection.delegate;

/**
 * @author KimShen
 *
 */
public interface DelegateGuard {

	public boolean guard(String location, DelegateBody body);
}
