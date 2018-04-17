package com.kepler.connection.delegate;

/**
 * @author KimShen
 *
 */
public class DelegateResp {

	private DelegateBody data;

	private String errmsg;

	private int errno;

	public DelegateBody getData() {
		return this.data;
	}

	public void setData(DelegateBody data) {
		this.data = data;
	}

	public String getErrmsg() {
		return this.errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public int getErrno() {
		return this.errno;
	}

	public void setErrno(int errno) {
		this.errno = errno;
	}
}
