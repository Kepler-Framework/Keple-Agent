package com.kepler.connection.agent.parser;

import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kepler.connection.agent.RequestParser;
import com.kepler.connection.json.Json;
import com.kepler.connection.stream.WrapInputStream;

/**
 * @author KimShen
 *
 */
public class JsonParser implements RequestParser {

	private static final Log LOGGER = LogFactory.getLog(PlainParser.class);

	private static final String TYPE = "application/json";

	private final Json json;

	public JsonParser(Json json) {
		super();
		this.json = json;
	}

	@SuppressWarnings("unchecked")
	@Override
	public LinkedHashMap<String, Object> parse(WrapInputStream input) {
		try {
			return this.json.read(input, LinkedHashMap.class);
		} catch (Exception e) {
			JsonParser.LOGGER.error(e.getMessage(), e);
			return new LinkedHashMap<String, Object>();
		}
	}

	@Override
	public boolean support(String type) {
		return type.contains(JsonParser.TYPE);
	}
}
