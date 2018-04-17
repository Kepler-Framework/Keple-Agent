package com.kepler.connection.agent.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.kepler.connection.agent.RespFactory;
import com.kepler.connection.agent.RespProcessor;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DynamicFactory implements RespFactory, ApplicationContextAware {

	private final Map<Service, RespProcessor> processor = new HashMap<Service, RespProcessor>();

	private final RespProcessor def = new DefaultFactory();

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		String[] names = context.getBeanNamesForType(RespProcessor.class);
		if (names != null) {
			for (String name : names) {
				RespProcessor handler = context.getBean(name, RespProcessor.class);
				this.processor.put(handler.support(), handler);
			}
		}
	}

	@Override
	public Object resp(Service service, Object resp) {
		RespProcessor handler = this.processor.get(service);
		return handler != null ? handler.process(resp) : this.def.process(resp);
	}
}
