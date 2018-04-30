package com.kepler.connection.json;

import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author KimShen
 *
 */
public class Json {

	public static final ObjectMapper MAPPER = new ObjectMapper();

	static {
		Json.MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Json.MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		Json.MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	public OutputStream write(OutputStream output, Object ob) throws Exception {
		Json.MAPPER.writeValue(output, ob);
		return output;
	}

	public String write(Object ob) throws Exception {
		return Json.MAPPER.writeValueAsString(ob);
	}

	public <T> T read(InputStream input, Class<T> t) throws Exception {
		return Json.MAPPER.readValue(input, t);
	}
}
