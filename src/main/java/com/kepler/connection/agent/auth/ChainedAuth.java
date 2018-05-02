package com.kepler.connection.agent.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.kepler.connection.agent.RequestAuth;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;

/**
 * @author KimShen
 *
 */
public class ChainedAuth implements RequestAuth, ApplicationContextAware {

	private static final Log LOGGER = LogFactory.getLog(ChainedAuth.class);

	private final List<RequestAuth> auth = new ArrayList<RequestAuth>();

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		// 加载处理器
		String[] names = context.getBeanNamesForType(RequestAuth.class);
		if (names != null) {
			for (String name : names) {
				try {
					RequestAuth auth = context.getBean(name, RequestAuth.class);
					if (auth != this) {
						this.auth.add(auth);
					}
				} catch (Exception e) {
					ChainedAuth.LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void auth(String uri, String method, Set<Cookie> cookie, HttpHeaders headers) throws Exception {
		for (RequestAuth each : this.auth) {
			each.auth(uri, method, cookie, headers);
		}
	}
}
