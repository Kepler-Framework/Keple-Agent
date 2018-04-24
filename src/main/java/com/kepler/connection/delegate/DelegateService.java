package com.kepler.connection.delegate;

/**
 * @author KimShen
 *
 */
public class DelegateService {

	private String service;

	private String version;

	private String path;

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getService() {
		return this.service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
