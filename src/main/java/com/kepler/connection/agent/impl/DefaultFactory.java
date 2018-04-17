package com.kepler.connection.agent.impl;

import com.kepler.connection.agent.RespProcessor;
import com.kepler.header.impl.TraceContext;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DefaultFactory implements RespProcessor {

	@Override
	public Object process(Object resp) {
		return new DefaultResp(resp, TraceContext.getTraceOnCreate());
	}

	@Override
	public Service support() {
		return null;
	}

}
