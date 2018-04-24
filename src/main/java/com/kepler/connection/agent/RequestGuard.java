package com.kepler.connection.agent;

/**
 * @author KimShen
 *
 */
public interface RequestGuard {

	public void guard(Request request) throws Exception;
}
