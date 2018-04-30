package com.kepler.connection.delegate;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DelegateService {

	private DelegateHttp http = DelegateHttp.GET;

	private Service target;

	private String service;

	private String version;

	public DelegateService() {
		super();
	}

	public DelegateService(Service service) {
		super();
		this.service = service.service();
		this.version = service.version();
	}

	public DelegateService(String service, String version) {
		super();
		this.service = service;
		this.version = version;
	}

	public Service target() {
		return this.target != null ? this.target : (this.target = new Service(this.service, this.version));
	}

	public DelegateHttp getHttp() {
		return this.http;
	}

	public void setHttp(DelegateHttp http) {
		this.http = http;
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

	public int hashCode() {
		return this.target().hashCode();
	}

	public boolean equals(Object ob) {
		if (ob == null) {
			return false;
		}
		return DelegateService.class.cast(ob).target().equals(this.target());
	}
}
