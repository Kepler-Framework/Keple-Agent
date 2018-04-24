package com.kepler.connection.delegate;

import com.kepler.host.Host;
import com.kepler.host.impl.DefaultHost;

/**
 * @author KimShen
 *
 */
public class DelegateHost extends DefaultHost {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DelegateHost(DelegateBody body) {
		super(Host.LOCATION, Host.GROUP_DEF, Host.TOKEN_VAL, Host.NAME, body.getTag(), "", body.getRoot(), 0, Host.FEATURE, body.getPriority());
	}
}
