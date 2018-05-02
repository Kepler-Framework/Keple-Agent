package com.kepler.connection.agent.parser;

import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.connection.agent.RequestParser;
import com.kepler.connection.stream.WrapInputStream;
import com.kepler.generic.reflect.GenericBean;
import com.kepler.generic.reflect.impl.DelegateBean;
import com.kepler.org.apache.commons.io.IOUtils;

/**
 * @author KimShen
 *
 */
public class PlainParser implements RequestParser {

	private static final Log LOGGER = LogFactory.getLog(PlainParser.class);

	private static final String TYPE = "application/x-www-form-urlencoded";

	@Override
	public LinkedHashMap<String, Object> parse(WrapInputStream input) {
		try {
			GenericBean bean = new DelegateBean();
			for (String param : IOUtils.toString(input).split("&")) {
				String[] pair = param.split("=");
				bean.put(pair[0], pair[1]);
			}
			return bean.args();
		} catch (Exception e) {
			PlainParser.LOGGER.error(e.getMessage(), e);
			return new LinkedHashMap<String, Object>();
		}
	}

	@Override
	public boolean support(String type) {
		return type.contains(PlainParser.TYPE);
	}
}
