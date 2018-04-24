package com.kepler.connection.invoker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.generic.reflect.GenericBean;
import com.kepler.generic.reflect.impl.DelegateArgs;
import com.kepler.host.Host;
import com.kepler.invoker.InvokerProcessor;
import com.kepler.protocol.Request;

/**
 * @author KimShen
 *
 */
public class GenericProcessor implements InvokerProcessor {

	private static final boolean WARN = PropertiesUtils.get(GenericProcessor.class.getName().toLowerCase() + ".warn", true);

	private static final Log LOGGER = LogFactory.getLog(GenericProcessor.class);

	private static final int FEATHRE_VERSION = Host.FEATURE;

	@Override
	public Request before(Request request, Host host) {
		if (host.feature() >= GenericProcessor.FEATHRE_VERSION) {
			return request;
		}
		if (GenericProcessor.WARN) {
			GenericProcessor.LOGGER.info("[host=" + host.address() + "][feature=" + host.feature() + "]");
		}
		request.args()[0] = new DelegateArgs(GenericBean.class.cast(request.args()[0]));
		return request;
	}

}
