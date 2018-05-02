package com.kepler.connection.agent;

import java.util.LinkedHashMap;

import com.kepler.connection.stream.WrapInputStream;

/**
 * @author KimShen
 *
 */
public interface RequestParser {

	public LinkedHashMap<String, Object> parse(WrapInputStream input);

	public boolean support(String type);
}
