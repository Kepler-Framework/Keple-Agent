package com.kepler.connection.delegate.request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.kepler.connection.delegate.DelegateHost;
import com.kepler.connection.delegate.DelegateRequest;
import com.kepler.connection.json.Json;
import com.kepler.generic.reflect.GenericBean;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public class DelegatePost extends DelegateBase implements DelegateRequest {

	private static final Log LOGGER = LogFactory.getLog(DelegateGet.class);

	private final Json json;

	public DelegatePost(Json json) {
		super();
		this.json = json;
	}

	@Override
	public HttpUriRequest request(Request request, DelegateHost host) throws Exception {
		String url = super.url(request, host);
		HttpPost post = new HttpPost(url);
		post.setConfig(super.config());
		super.headers(request, post);
		GenericBean bean = GenericBean.class.cast(request.args()[0]);
		if (bean != null) {
			String body = this.json.write(bean);
			DelegatePost.LOGGER.debug(request.service() + "[method=" + request.method() + "][url=" + url + "][body=" + body + "]");
			post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
		}
		return post;
	}
}
