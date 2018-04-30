package com.kepler.connection.delegate.request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import com.kepler.connection.delegate.DelegateHost;
import com.kepler.connection.delegate.DelegateRequest;
import com.kepler.generic.reflect.GenericBean;
import com.kepler.host.Host;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public class DelegateGet extends DelegateBase implements DelegateRequest {

	private static final Log LOGGER = LogFactory.getLog(DelegateGet.class);

	public DelegateGet() {
		super();
	}

	protected String url(Request request, Host host) {
		StringBuffer buffer = new StringBuffer(super.url(request, host));
		GenericBean bean = GenericBean.class.cast(request.args()[0]);
		if (bean != null) {
			for (String key : bean.args().keySet()) {
				buffer.append("&").append(key).append("=").append(bean.args().get(key).toString());
			}
		}
		String url = buffer.toString();
		DelegateGet.LOGGER.debug(request.service() + "[method=" + request.method() + "][url=" + url + "]");
		return url;
	}

	@Override
	public HttpUriRequest request(Request request, DelegateHost host) throws Exception {
		String url = this.url(request, host);
		HttpGet get = new HttpGet(url);
		get.setConfig(super.config());
		super.headers(request, get);
		return get;
	}
}
