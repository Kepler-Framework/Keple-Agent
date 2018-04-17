package com.kepler.connection.location.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.location.DelegateLocation;
import com.kepler.host.Host;

/**
 * @author KimShen
 *
 */
public class ArgLocation implements DelegateLocation {

	private static final String LOCATIONS = PropertiesUtils.get(ArgLocation.class.getName().toLowerCase() + ".locations", "");

	private static final String SPLIT = PropertiesUtils.get(ArgLocation.class.getName().toLowerCase() + ".split", ";");

	private static final Log LOGGER = LogFactory.getLog(ArgLocation.class);

	private List<String> locations = new ArrayList<String>();

	public ArgLocation() {
		super();
		String locations = ArgLocation.LOCATIONS;
		for (String each : locations.split(ArgLocation.SPLIT)) {
			String location = each.trim();
			if (!StringUtils.isEmpty(location)) {
				ArgLocation.LOGGER.info("[location=" + location + "]");
				this.locations.add(location);
			}
		}
	}

	@Override
	public List<String> locations(Host host) throws Exception {
		return this.locations;
	}
}
