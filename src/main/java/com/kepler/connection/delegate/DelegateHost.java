package com.kepler.connection.delegate;

import java.util.Map;

import com.kepler.host.Host;
import com.kepler.host.impl.DefaultHost;

/**
 * @author KimShen
 *
 */
public class DelegateHost extends DefaultHost {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Map<String, String> mapping;

	private final DelegateHttp http;

	public DelegateHost(Map<String, String> mapping, DelegateHttp http, Host host) {
		super(host);
		this.mapping = mapping;
		this.http = http;
	}

	public Map<String, String> mapping() {
		return this.mapping;
	}

	public DelegateHost reverse() {
		switch (this.http) {
		case POST: {
			return new DelegateHost(this.mapping, DelegateHttp.GET, this);
		}
		case GET: {
			return new DelegateHost(this.mapping, DelegateHttp.POST, this);
		}
		default: {
			return null;
		}
		}
	}

	public DelegateHttp http() {
		return this.http;
	}

	public int hashCode() {
		return super.hashCode() ^ this.http.hashCode();
	}

	public boolean equals(Object ob) {
		return super.equals(ob) && DelegateHost.class.cast(ob).http().equals(this.http());
	}
}
