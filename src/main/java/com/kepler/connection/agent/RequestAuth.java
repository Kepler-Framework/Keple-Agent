package com.kepler.connection.agent;

import java.util.Set;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;

/**
 * @author KimShen
 *
 */
public interface RequestAuth {

	public void auth(String uri, String method, Set<Cookie> cookie, HttpHeaders headers) throws Exception;

}
