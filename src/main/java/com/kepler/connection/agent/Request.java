package com.kepler.connection.agent;

import java.util.LinkedHashMap;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface Request {

	public LinkedHashMap<String, Object> body();

	public RequestHeaders headers();

	public Service service();

	public String method();
}
