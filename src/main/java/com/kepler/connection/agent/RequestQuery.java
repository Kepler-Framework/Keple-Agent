package com.kepler.connection.agent;

import java.util.LinkedHashMap;

/**
 * @author KimShen
 *
 */
public interface RequestQuery {

	public LinkedHashMap<String, Object> merge(LinkedHashMap<String, Object> body);

	public String path(int index, String def);

	public String path(int index);

	public int length();
}
