package com.kepler.connection.agent.guard;

import java.util.List;

import com.kepler.connection.agent.Request;
import com.kepler.connection.agent.RequestGuard;

/**
 * @author KimShen
 *
 */
public class ChainedGuard implements RequestGuard {

	private final List<RequestGuard> guard;

	public ChainedGuard(List<RequestGuard> guard) {
		super();
		this.guard = guard;
	}

	@Override
	public void guard(Request request) throws Exception {
		for (RequestGuard each : this.guard) {
			each.guard(request);
		}
	}

}
