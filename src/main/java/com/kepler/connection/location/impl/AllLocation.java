package com.kepler.connection.location.impl;

import java.util.ArrayList;
import java.util.List;

import com.kepler.connection.location.DelegateLocation;
import com.kepler.host.Host;

/**
 * @author KimShen
 *
 */
public class AllLocation implements DelegateLocation {

	private final List<DelegateLocation> locations;

	public AllLocation(List<DelegateLocation> locations) {
		super();
		this.locations = locations;
	}

	@Override
	public List<String> locations(Host host) throws Exception {
		List<String> locations = new ArrayList<String>();
		for (DelegateLocation each : this.locations) {
			List<String> l = each.locations(host);
			if (l != null && !l.isEmpty()) {
				locations.addAll(l);
			}
		}
		return locations;
	}

}
