package com.kepler.connection.delegate;

/**
 * @author KimShen
 *
 */
public interface DelegateGuard {

	public DelegateResponse guard(String location, DelegateResponse response) throws Exception;
}
