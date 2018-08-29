package com.kepler.connection.agent.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kepler.KeplerExceptionCodable;
import com.kepler.connection.ResponseStatus;

/**
 * @author KimShen
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultResponse {

	private final String errmsg;

	private final String trace;

	private final Object data;

	private final int errno;

	public DefaultResponse(Throwable exception, String trace) {
		super();
		this.data = null;
		this.trace = trace;
		this.errmsg = exception.getMessage();
        this.errno = exception instanceof KeplerExceptionCodable ? ((KeplerExceptionCodable) exception).getCode() : ResponseStatus.FAILED.code();
	}

	public DefaultResponse(String exception, String trace) {
		super();
		this.data = null;
		this.trace = trace;
		this.errmsg = exception;
		this.errno = ResponseStatus.FAILED.code();
	}

	public DefaultResponse(Object response, String trace) {
		super();
		this.errmsg = null;
		this.trace = trace;
		this.data = response;
		this.errno = ResponseStatus.SUCCESS.code();
	}

	public String getErrmsg() {
		return this.errmsg;
	}

	public String getTrace() {
		return this.trace;
	}

	public Object getData() {
		return this.data;
	}

	public int getErrno() {
		return this.errno;
	}
}
