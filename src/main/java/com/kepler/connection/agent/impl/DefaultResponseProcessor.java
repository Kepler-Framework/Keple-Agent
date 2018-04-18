package com.kepler.connection.agent.impl;

import com.kepler.connection.agent.ResponseProcessor;
import com.kepler.header.impl.TraceContext;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DefaultResponseProcessor implements ResponseProcessor {

	public Object exception(Throwable throwable) throws Exception {
		return new DefaultResponse(throwable, TraceContext.getTraceOnCreate());
	}

	@Override
	public Object response(Object response) throws Exception {
		return new DefaultResponse(response, TraceContext.getTraceOnCreate());
	}

	@Override
	public Service support() {
		return null;
	}

}
