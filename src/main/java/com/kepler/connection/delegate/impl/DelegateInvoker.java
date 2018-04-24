package com.kepler.connection.delegate.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kepler.KeplerGenericException;
import com.kepler.KeplerRoutingException;
import com.kepler.config.PropertiesUtils;
import com.kepler.connection.location.impl.ArgLocation;
import com.kepler.host.Host;
import com.kepler.mock.Mocker;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public class DelegateInvoker implements Mocker {

	private static final int TIMEOUT_SOCKET = PropertiesUtils.get(DelegateInvoker.class.getName().toLowerCase() + ".timeout_socket", 5000);

	private static final int TIMEOUT_CONN = PropertiesUtils.get(DelegateInvoker.class.getName().toLowerCase() + ".timeout_conn", 5000);

	private static final int TIMEOUT_READ = PropertiesUtils.get(DelegateInvoker.class.getName().toLowerCase() + ".timeout_read", 5000);

	private static final int CONN_ROUTE = PropertiesUtils.get(DelegateInvoker.class.getName().toLowerCase() + ".conn_route", 20);

	private static final int CONN_MAX = PropertiesUtils.get(DelegateInvoker.class.getName().toLowerCase() + ".conn_max", 50);

	private static final Log LOGGER = LogFactory.getLog(ArgLocation.class);

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final CloseableHttpClient client;

	private final RequestConfig config;

	private final DelegateHosts hosts;

	public DelegateInvoker(DelegateHosts hosts) {
		super();
		this.config = RequestConfig.custom().setConnectTimeout(DelegateInvoker.TIMEOUT_CONN).setConnectionRequestTimeout(DelegateInvoker.TIMEOUT_READ).setSocketTimeout(DelegateInvoker.TIMEOUT_SOCKET).build();
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setMaxConnPerRoute(DelegateInvoker.CONN_ROUTE);
		builder.setMaxConnTotal(DelegateInvoker.CONN_MAX);
		this.client = builder.build();
		this.hosts = hosts;
	}

	public void destroy() throws Exception {
		this.client.close();
	}

	public String url(Request request, Host host) {
		return host.host() + "?service" + request.service().service() + "&method=" + request.method();
	}

	private HttpPost headers(Request request, HttpPost post) {
		for (String key : request.headers().get().keySet()) {
			post.addHeader(key, request.headers().get().get(key));
		}
		return post;
	}

	@Override
	public Object mock(Request request) throws Exception {
		try {
			Host host = this.hosts.host(request);
			if (host == null) {
				throw new KeplerRoutingException("None service for " + request.service());
			}
			HttpEntity entity = new StringEntity(DelegateInvoker.MAPPER.writeValueAsString(request.args()), ContentType.APPLICATION_JSON);
			HttpPost post = this.headers(request, new HttpPost(this.url(request, host)));
			post.setConfig(this.config);
			post.setEntity(entity);
			try (CloseableHttpResponse response = this.client.execute(post)) {
				return DelegateInvoker.MAPPER.readValue(response.getEntity().getContent(), Map.class);
			}
		} catch (Throwable e) {
			DelegateInvoker.LOGGER.error(e.getMessage(), e);
			throw new KeplerGenericException(e);
		}
	}
}
