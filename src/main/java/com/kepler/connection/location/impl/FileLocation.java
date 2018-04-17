package com.kepler.connection.location.impl;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.location.DelegateLocation;
import com.kepler.host.Host;

/**
 * @author KimShen
 *
 */
public class FileLocation implements DelegateLocation {

	private static final String FILE = PropertiesUtils.get(FileLocation.class.getName().toLowerCase() + ".file", "kepler-agent.locations");

	@Override
	public List<String> locations(Host host) throws Exception {
		File file = new File(FileLocation.FILE);
		if (!file.exists()) {
			return null;
		}
		return FileUtils.readLines(file, Charset.defaultCharset());
	}

}
