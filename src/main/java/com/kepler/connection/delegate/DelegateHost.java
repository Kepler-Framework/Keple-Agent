package com.kepler.connection.delegate;

import com.kepler.host.Host;
import com.kepler.host.impl.DefaultHost;

/**
 * @author KimShen
 *
 */
public class DelegateHost extends DefaultHost{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final DelegateHttp http;

	public DelegateHost(DelegateHttp http, Host host) {
		super(host);
		this.http = http;
	}

	public DelegateHttp http() {
		return this.http;
	}
}
