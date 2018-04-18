package com.kepler.connection.delegate;

import java.util.List;

import com.kepler.host.Host;

/**
 * @author KimShen
 *
 */
public class DelegateBody {

	private List<DelegateService> services;

	private Integer priority;

	private String root;

	private String tag;

	public DelegateBody() {
		this.priority = Host.PRIORITY_DEF;
		this.tag = Host.TAG_DEF;
	}

	public List<DelegateService> getServices() {
		return this.services;
	}

	public void setServices(List<DelegateService> services) {
		this.services = services;
	}

	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getRoot() {
		return this.root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getTag() {
		return this.tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}