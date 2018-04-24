package com.kepler.connection.agent;

import java.util.Map;

/**
 * @author KimShen
 *
 */
public interface RequestHeaders {
	
	public RequestHeaders put(Map<String, String> headers);

	public Map<String, String> headers();
}
