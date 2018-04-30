package com.kepler.connection.delegate.request;

import org.apache.http.HttpEntity;
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

	private final Json json;

	public DelegatePost(Json json) {
		super();
		this.json = json;
	}

	@Override
	public HttpUriRequest request(Request request, DelegateHost host) throws Exception {
		HttpPost post = new HttpPost(super.url(request, host));
		post.setConfig(super.config());
		super.headers(request, post);
		GenericBean bean = GenericBean.class.cast(request.args()[0]);
		if (bean != null) {
			HttpEntity entity = new StringEntity(this.json.write(bean), ContentType.APPLICATION_JSON);
			post.setEntity(entity);
		}
		return post;
	}
}
