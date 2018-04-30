package com.kepler.connection.delegate.impl;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.agent.impl.DefaultAgent;
import com.kepler.connection.delegate.DelegateGuard;
import com.kepler.connection.delegate.DelegateResponse;
import com.kepler.connection.delegate.DelegateService;
import com.kepler.connection.json.Json;
import com.kepler.connection.location.DelegateLocation;
import com.kepler.connection.location.impl.ArgLocation;
import com.kepler.host.Host;

/**
 * @author KimShen
 *
 */
/**
 * @author KimShen
 *
 */
public class Delegate implements Runnable {

	private static final int TIMEOUT_CONN = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".timeout_conn", 5000);

	private static final int TIMEOUT_READ = PropertiesUtils.get(DefaultAgent.class.getName().toLowerCase() + ".timeout_read", 5000);

	private static final int INTERVAL = PropertiesUtils.get(Delegate.class.getName().toLowerCase() + ".interval", 20000);

	private static final int DELAY = PropertiesUtils.get(Delegate.class.getName().toLowerCase() + ".delay", 10000);

	private static final Log LOGGER = LogFactory.getLog(ArgLocation.class);

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

	private final Difference difference = new Difference();

	private final DelegateLocation location;

	private final DelegateGuard guard;

	private final DelegateHosts hosts;

	private final Host host;

	private final Json json;

	volatile private boolean running = false;

	public Delegate(DelegateLocation location, DelegateGuard guard, DelegateHosts hosts, Host host, Json json) {
		super();
		this.location = location;
		this.guard = guard;
		this.hosts = hosts;
		this.host = host;
		this.json = json;
	}

	private HttpURLConnection connection(String location) throws Exception {
		HttpURLConnection conn = HttpURLConnection.class.cast(new URL(location).openConnection());
		conn.setConnectTimeout(Delegate.TIMEOUT_CONN);
		conn.setReadTimeout(Delegate.TIMEOUT_READ);
		return conn;
	}

	private Delegate uninstall() throws Exception {
		try {
			Map<Host, Collection<DelegateService>> services = this.difference.diff();
			for (Host host : services.keySet()) {
				this.hosts.ban(host, services.get(host));
			}
			return this;
		} catch (Exception e) {
			Delegate.LOGGER.error(e.getMessage(), e);
			return this;
		}
	}

	private Delegate install() throws Exception {
		for (String location : this.location.locations(this.host)) {
			Delegate.LOGGER.info("[install][location=" + location + "]");
			try (InputStream input = this.connection(location).getInputStream()) {
				DelegateResponse resp = this.guard.guard(location, this.json.read(input, DelegateResponse.class));
				this.difference.add(resp.getData().getHost(), resp.getData().getServices());
				this.hosts.add(resp.getData().getHost(), resp.getData().getServices());
			} catch (Exception e) {
				Delegate.LOGGER.error("[location=" + location + "][message=" + e.getMessage() + "]", e);
			}
		}
		return this;
	}

	public void destroy() throws Exception {
		this.executor.shutdown();
	}

	public void init() throws Exception {
		this.executor.scheduleAtFixedRate(this, Delegate.DELAY, Delegate.INTERVAL, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		if (this.running) {
			return;
		}
		try {
			this.hosts.prepare();
			this.install().uninstall();
		} catch (Exception e) {
			Delegate.LOGGER.error(e.getMessage(), e);
		} finally {
			this.hosts.exchange();
			this.running = false;
		}
	}

	private class Difference {

		private Map<Host, Collection<DelegateService>> curr = new HashMap<Host, Collection<DelegateService>>();

		private Map<Host, Collection<DelegateService>> prev = new HashMap<Host, Collection<DelegateService>>();

		public Difference add(Host host, Collection<DelegateService> services) {
			Collection<DelegateService> pres = this.prev.get(host);
			if (pres != null) {
				// 差集
				pres.removeAll(services);
			}
			Collection<DelegateService> curs = this.curr.get(host);
			if (curs == null) {
				this.curr.put(host, new HashSet<DelegateService>(services));
			} else {
				// 并集
				curs.addAll(services);
			}
			return this;
		}

		public Map<Host, Collection<DelegateService>> diff() {
			Map<Host, Collection<DelegateService>> diff = this.prev;
			this.prev = this.curr;
			this.curr = new HashMap<Host, Collection<DelegateService>>();
			return diff;
		}
	}
}
