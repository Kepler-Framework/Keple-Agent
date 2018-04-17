package com.kepler.connection.agent.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kepler.connection.RespStatus;

/**
 * @author KimShen
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultResp {

	private final String errmsg;

	private final String trace;

	private final Object data;

	private final int errno;

	public DefaultResp(Throwable exception, String trace) {
		super();
		this.data = null;
		this.trace = trace;
		this.errmsg = exception.getMessage();
		this.errno = RespStatus.FAILED.code();
	}

	public DefaultResp(Object response, String trace) {
		super();
		this.errmsg = null;
		this.trace = trace;
		this.data = response;
		this.errno = RespStatus.SUCCESS.code();
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
