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

	private String catalog;

	public DelegateService() {
		super();
	}

	public DelegateService(Service service) {
		super();
		this.service = service.service();
		this.version = service.version();
		this.catalog = service.catalog();
	}

	public DelegateService(String service, String version) {
		this(service, version, null);
	}

	public DelegateService(String service, String version, String catalog) {
		super();
		this.service = service;
		this.version = version;
		this.catalog = catalog;
	}

	public Service target() {
		return this.target != null ? this.target : (this.target = new Service(this.service, this.version, this.catalog));
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

	public String getCatalog() {
		return this.catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
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
