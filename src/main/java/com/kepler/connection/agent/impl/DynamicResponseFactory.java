package com.kepler.connection.agent.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.kepler.connection.agent.ResponseFactory;
import com.kepler.connection.agent.ResponseProcessor;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DynamicResponseFactory implements ResponseFactory, ApplicationContextAware {

	private static final Log LOGGER = LogFactory.getLog(DynamicResponseFactory.class);

	private final Map<Service, ResponseProcessor> processor = new HashMap<Service, ResponseProcessor>();

	private final ResponseProcessor def;

	public DynamicResponseFactory(ResponseProcessor def) {
		super();
		this.def = def;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		String[] names = context.getBeanNamesForType(ResponseProcessor.class);
		if (names != null) {
			for (String name : names) {
				try {
					ResponseProcessor processor = context.getBean(name, ResponseProcessor.class);
					if (processor != this.def) {
						this.processor.put(processor.support(), processor);
					}
				} catch (Exception e) {
					DynamicResponseFactory.LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public Object throwable(Service service, Throwable throwable) throws Exception {
		try {
			ResponseProcessor handler = this.processor.get(service);
			return handler != null ? handler.exception(throwable) : this.def.exception(throwable);
		} catch (Exception e) {
			DynamicResponseFactory.LOGGER.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Object response(Service service, Object resp) throws Exception {
		try {
			ResponseProcessor handler = this.processor.get(service);
			return handler != null ? handler.response(resp) : this.def.response(resp);
		} catch (Exception e) {
			DynamicResponseFactory.LOGGER.error(e.getMessage(), e);
			throw e;
		}
	}
}
