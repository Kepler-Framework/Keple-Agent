package com.kepler.connection.delegate;

import java.util.ArrayList;

import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public class DelegateServices extends ArrayList<Service> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DelegateServices(DelegateBody body) throws Exception {
		for (DelegateService each : body.getServices()) {
			super.add((new Service(each.getService(), each.getVersion())));
		}
	}
}
