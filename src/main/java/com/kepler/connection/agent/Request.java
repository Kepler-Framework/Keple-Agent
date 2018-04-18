package com.kepler.connection.agent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface Request {

	public static final Map<String, Object> EMPTY_CONTENT = Collections.unmodifiableMap(new HashMap<String, Object>());

	public static final String[] EMPTY_CLASSES = new String[] {};

	public static final Object[] EMPTY_ARGS = new Object[] {};

	public String[] classes() throws Exception;

	public Service service() throws Exception;

	public String method() throws Exception;

	public Object[] args() throws Exception;
}
