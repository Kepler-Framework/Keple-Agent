package com.kepler.connection.agent.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.kepler.connection.agent.Request;
import com.kepler.connection.agent.RequestFactory;
import com.kepler.connection.agent.RequestProcessor;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author KimShen
 *
 */
public class DynamicRequestFactory implements RequestFactory, ApplicationContextAware {

	private static final Log LOGGER = LogFactory.getLog(DynamicResponseFactory.class);

	private final List<RequestProcessor> processor = new ArrayList<RequestProcessor>();

	private final RequestProcessor def;

	public DynamicRequestFactory(RequestProcessor def) {
		super();
		this.def = def;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		String[] names = context.getBeanNamesForType(RequestProcessor.class);
		if (names != null) {
			for (String name : names) {
				try {
					RequestProcessor processor = context.getBean(name, RequestProcessor.class);
					if (processor != this.def) {
						this.processor.add(processor);
					}
				} catch (Exception e) {
					DynamicRequestFactory.LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}

	public Request factory(FullHttpRequest request) throws Exception {
		for (RequestProcessor each : this.processor) {
			if (each.support(request)) {
				return each.process(request);
			}
		}
		return this.def.process(request);
	}

}
