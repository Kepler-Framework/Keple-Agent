package com.kepler.connection.delegate;

/**
 * @author KimShen
 *
 */
public interface DelegateGuard {

	public DelegateBody guard(String location, DelegateBody body) throws Exception;
}
