package com.kepler.connection.delegate.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.delegate.DelegateHost;
import com.kepler.connection.delegate.DelegateRequest;
import com.kepler.connection.delegate.DelegateService;
import com.kepler.connection.json.Json;
import com.kepler.host.Host;
import com.kepler.protocol.Request;
import com.kepler.router.Routing;
import com.kepler.router.routing.Routings;
import com.kepler.service.Exported;
import com.kepler.service.exported.ExportedServices;

/**
 * @author KimShen
 *
 */
public class DelegateHosts {

	private static final String ROUTING = PropertiesUtils.get(DelegateHosts.class.getName().toLowerCase() + ".routing", Routing.NAME);

	private static final Log LOGGER = LogFactory.getLog(DelegateHosts.class);

	private final ExportedServices context;

	private final DelegateInvoker invoker;

	private final Exported exported;

	private final Routings routings;

	volatile private Map<DelegateService, List<Host>> copy = new HashMap<DelegateService, List<Host>>();

	volatile private Map<DelegateService, List<Host>> curr = new HashMap<DelegateService, List<Host>>();

	public DelegateHosts(DelegateRequest request, ExportedServices context, Exported exported, Routings routings, Json json) {
		super();
		this.invoker = new DelegateInvoker(this, request, json);
		this.routings = routings;
		this.exported = exported;
		this.context = context;
	}

	public DelegateHosts add(Host host, Collection<DelegateService> services) throws Exception {
		for (DelegateService service : services) {
			// 发布服务
			if (!this.context.services().containsKey(service.target())) {
				this.exported.export(service.target(), this.invoker);
			}
			List<Host> hosts = this.copy.get(service);
			if (hosts == null) {
				this.copy.put(service, hosts = new ArrayList<Host>());
			}
			DelegateHost h = new DelegateHost(service.getMapping(), service.getHttp(), host);
			hosts.remove(h.reverse());
			// Replace Mapping
			hosts.remove(h);
			hosts.add(h);
		}
		return this;
	}

	public DelegateHosts ban(Host host, Collection<DelegateService> services) throws Exception {
		for (DelegateService service : services) {
			this.ban(host, service);
		}
		return this;
	}

	public DelegateHosts ban(Host host, DelegateService service) throws Exception {
		List<Host> hosts = this.copy.get(service);
		if (hosts == null) {
			return this;
		}
		DelegateHost h = DelegateHost.class.isAssignableFrom(host.getClass()) ? DelegateHost.class.cast(host) : new DelegateHost(service.getMapping(), service.getHttp(), host);
		if (!hosts.contains(h)) {
			return this;
		}
		hosts.remove(h);
		DelegateHosts.LOGGER.info("[ban][service=" + service.target() + "][host=" + h.host() + "]");
		if (hosts.isEmpty()) {
			if (this.context.services().containsKey(service.target())) {
				this.exported.logout(service.target());
			}
			this.copy.remove(service);
			DelegateHosts.LOGGER.info("[ban][service=" + service.target() + "]");
		}
		return this;
	}

	public DelegateHost host(Request request) {
		Host host = this.routings.get(DelegateHosts.ROUTING).route(request, this.curr.get(new DelegateService(request.service())));
		return host != null ? DelegateHost.class.cast(host) : null;
	}

	public void destroy() throws Exception {
		this.invoker.destroy();
	}

	public void exchange() {
		Map<DelegateService, List<Host>> temp = this.curr;
		this.curr = this.copy;
		this.copy = temp;
	}

	public void prepare() {
		this.copy.clear();
		this.copy.putAll(this.curr);
	}
}
