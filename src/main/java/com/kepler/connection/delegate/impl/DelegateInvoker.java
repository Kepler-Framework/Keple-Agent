package com.kepler.connection.delegate.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.kepler.KeplerGenericException;
import com.kepler.KeplerRoutingException;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.delegate.DelegateHost;
import com.kepler.connection.delegate.DelegateRequest;
import com.kepler.connection.json.Json;
import com.kepler.connection.location.impl.ArgLocation;
import com.kepler.generic.reflect.impl.DelegateBean;
import com.kepler.mock.Mocker;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public class DelegateInvoker implements Mocker {

	private static final int CONN_ROUTE = PropertiesUtils.get(DelegateInvoker.class.getName().toLowerCase() + ".conn_route", 20);

	private static final int CONN_MAX = PropertiesUtils.get(DelegateInvoker.class.getName().toLowerCase() + ".conn_max", 50);

	private static final Log LOGGER = LogFactory.getLog(ArgLocation.class);

	private final CloseableHttpClient client;

	private final DelegateRequest request;

	private final DelegateHosts hosts;

	private final Json json;

	public DelegateInvoker(DelegateHosts hosts, DelegateRequest request, Json json) {
		super();
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setMaxConnPerRoute(DelegateInvoker.CONN_ROUTE);
		builder.setMaxConnTotal(DelegateInvoker.CONN_MAX);
		this.client = builder.build();
		this.request = request;
		this.hosts = hosts;
		this.json = json;
	}

	public void destroy() throws Exception {
		this.client.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object mock(Request request) throws Exception {
		try {
			DelegateHost host = this.hosts.host(request);
			if (host == null) {
				throw new KeplerRoutingException("None service for " + request.service());
			}
			try (CloseableHttpResponse response = this.client.execute(this.request.request(request, host))) {
				Object resp = this.json.read(response.getEntity().getContent(), Map.class);
				Map<String, String> mapping = host.mapping();
				if (mapping == null) {
					return resp;
				}
				return new DelegateBean(Map.class.cast(resp)).mapping(host.mapping()).args();
			}
		} catch (Throwable e) {
			DelegateInvoker.LOGGER.error(e.getMessage(), e);
			throw new KeplerGenericException(e);
		}
	}
}
