package com.kepler.connection.delegate.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.host.Host;
import com.kepler.protocol.Request;
import com.kepler.router.Routing;
import com.kepler.router.routing.Routings;
import com.kepler.service.Exported;
import com.kepler.service.Service;
import com.kepler.service.exported.ExportedServices;

/**
 * @author KimShen
 *
 */
public class DelegateHosts {

	private static final String ROUTING = PropertiesUtils.get(DelegateHosts.class.getName().toLowerCase() + ".routing", Routing.NAME);

	private static final Log LOGGER = LogFactory.getLog(DelegateHosts.class);

	volatile private Map<Service, List<Host>> snapshot = new HashMap<Service, List<Host>>();

	volatile private Map<Service, List<Host>> current = new HashMap<Service, List<Host>>();

	private final Map<Host, List<Service>> services = new HashMap<Host, List<Service>>();

	private final ExportedServices context;

	private final DelegateInvoker invoker;

	private final Exported exported;

	private final Routings routings;

	public DelegateHosts(ExportedServices context, Exported exported, Routings routings) {
		super();
		this.invoker = new DelegateInvoker(this);
		this.routings = routings;
		this.exported = exported;
		this.context = context;
	}

	public DelegateHosts add(Host host, List<Service> services) throws Exception {
		this.services.put(host, services);
		for (Service service : services) {
			if (!this.context.services().containsKey(service)) {
				this.exported.export(service, this.invoker);
			}
			List<Host> hosts = this.snapshot.get(service);
			if (hosts == null) {
				this.snapshot.put(service, hosts = new ArrayList<Host>());
			}
			if (!hosts.contains(host)) {
				hosts.add(host);
			}
		}
		return this;
	}

	public DelegateHosts ban(Set<Host> hosts) throws Exception {
		for (Host host : hosts) {
			this.ban(host);
		}
		return this;
	}

	public DelegateHosts ban(Host host) throws Exception {
		List<Service> services = this.services.remove(host);
		if (services == null) {
			return this;
		}
		for (Service service : services) {
			List<Host> hosts = this.snapshot.get(service);
			if (hosts == null) {
				continue;
			}
			if (!hosts.contains(host)) {
				continue;
			}
			hosts.remove(host);
			DelegateHosts.LOGGER.info("[ban][service=" + service + "][host=" + host + "]");
			if (hosts.isEmpty()) {
				if (this.context.services().containsKey(service)) {
					this.exported.logout(service);
				}
				this.services.remove(service);
				DelegateHosts.LOGGER.info("[ban][service=" + service + "]");
			}
		}
		return this;
	}
	
	public void destroy() throws Exception {
		this.invoker.destroy();
	}

	public Host host(Request request) {
		return this.routings.get(DelegateHosts.ROUTING).route(request, this.current.get(request.service()));
	}

	public DelegateHosts prepare() {
		this.snapshot.clear();
		this.snapshot.putAll(this.current);
		return this;
	}

	public DelegateHosts finish() {
		Map<Service, List<Host>> temp = this.current;
		this.current = this.snapshot;
		this.snapshot = temp;
		return this;
	}
}
