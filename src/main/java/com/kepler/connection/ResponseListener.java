package com.kepler.connection;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.config.PropertiesUtils;
import com.kepler.header.impl.TraceContext;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * @author kim 2015年7月8日
 */
public class ResponseListener implements GenericFutureListener<Future<Void>> {

	/**
	 * 等待预警
	 */
	private static final int WAIT_WARN = PropertiesUtils.get(ResponseListener.class.getName().toLowerCase() + ".wait_warn", 5000);

	private static final Log LOGGER = LogFactory.getLog(ResponseListener.class);

	private final String request;

	private final String trace;

	private final long running;

	private final long created;

	private final long remote;

	public ResponseListener(String request, long created, long running, long remote) {
		super();
		this.trace = TraceContext.getTrace();
		this.created = created;
		this.running = running;
		this.request = request;
		this.remote = remote;
	}

	@Override
	public void operationComplete(Future<Void> future) throws Exception {
		long net = System.currentTimeMillis();
		if ((this.created - net) >= ResponseListener.WAIT_WARN) {
			ResponseListener.LOGGER.warn("[wait-warn][request=" + this.request + "][trace=" + trace + "][create=" + new Date(this.created) + "][running=" + new Date(this.running) + "][remote=" + new Date(this.remote) + "][net=" + new Date(net) + "]");
		}
		if (!future.isSuccess() && future.cause() != null) {
			// 如果存在Context则获取Remote
			String message = "[message=" + future.cause().getMessage() + "][request=" + this.request + "][trace=" + trace + "]";
			ResponseListener.LOGGER.error(message, future.cause());
		}
	}
}