package com.kepler.connection.delegate.impl;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.RespStatus;
import com.kepler.connection.agent.impl.DefaultAgent;
import com.kepler.connection.delegate.DelegateGuard;
import com.kepler.connection.delegate.DelegateHost;
import com.kepler.connection.delegate.DelegateResp;
import com.kepler.connection.delegate.DelegateServices;
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

	private static final int INTERVAL = PropertiesUtils.get(Delegate.class.getName().toLowerCase() + ".interval", 60000);

	private static final int DELAY = PropertiesUtils.get(Delegate.class.getName().toLowerCase() + ".delay", 10000);

	private static final Log LOGGER = LogFactory.getLog(ArgLocation.class);

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

	private final ObjectMapper mapper = new ObjectMapper();

	private final Diffenence diffe = new Diffenence();

	volatile private boolean running = false;

	private final DelegateLocation location;

	private final DelegateGuard guard;

	private final DelegateHosts hosts;

	private final Host host;

	public Delegate(DelegateLocation location, DelegateGuard guard, DelegateHosts hosts, Host host) {
		super();
		this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		this.mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		this.location = location;
		this.guard = guard;
		this.hosts = hosts;
		this.host = host;
	}

	private HttpURLConnection connection(String location) throws Exception {
		HttpURLConnection conn = HttpURLConnection.class.cast(new URL(location).openConnection());
		conn.setConnectTimeout(Delegate.TIMEOUT_CONN);
		conn.setReadTimeout(Delegate.TIMEOUT_READ);
		return conn;
	}

	private Delegate uninstall() throws Exception {
		try {
			// 卸载节点
			this.hosts.ban(this.diffe.diff());
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
				DelegateResp resp = this.mapper.readValue(input, DelegateResp.class);
				if (RespStatus.SUCCESS.code() == resp.getErrno() && this.guard.guard(location, resp.getData())) {
					this.hosts.add(this.diffe.add(new DelegateHost(resp.getData())), new DelegateServices(resp.getData()));
				}
			} catch (Exception e) {
				Delegate.LOGGER.error("[location=" + location + "][message=" + e.getMessage() + "]", e);
			}
		}
		return this;
	}

	public void init() throws Exception {
		this.executor.scheduleAtFixedRate(this, Delegate.DELAY, Delegate.INTERVAL, TimeUnit.MILLISECONDS);
	}

	public void destroy() throws Exception {
		this.executor.shutdown();
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
			this.hosts.finish();
			this.running = false;
		}
	}

	private class Diffenence {

		private Set<Host> prev = new HashSet<Host>();

		private Set<Host> curr = new HashSet<Host>();

		public Host add(Host host) {
			this.prev.remove(host);
			this.curr.add(host);
			return host;
		}

		public Set<Host> diff() {
			Set<Host> diff = this.prev;
			this.prev = this.curr;
			this.curr = new HashSet<Host>();
			return diff;
		}
	}
}
