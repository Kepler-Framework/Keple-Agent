package com.kepler.connection.agent.uri;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.kepler.connection.agent.RequestURI;

/**
 * @author KimShen
 *
 */
public class ChainedURI implements RequestURI, ApplicationContextAware {

	private static final Log LOGGER = LogFactory.getLog(ChainedURI.class);

	private List<RequestURI> uri = new ArrayList<RequestURI>();

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		// 加载处理器
		String[] names = context.getBeanNamesForType(RequestURI.class);
		if (names != null) {
			for (String name : names) {
				try {
					RequestURI uri = context.getBean(name, RequestURI.class);
					if (!uri.equals(this)) {
						this.uri.add(uri);
					}
				} catch (Exception e) {
					ChainedURI.LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public URI uri(URI uri) {
		URI curr = uri;
		for (RequestURI each : this.uri) {
			curr = each.uri(curr);
		}
		return curr;
	}
}
