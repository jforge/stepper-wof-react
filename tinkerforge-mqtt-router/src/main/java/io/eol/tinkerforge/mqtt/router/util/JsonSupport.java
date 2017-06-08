package io.eol.tinkerforge.mqtt.router.util;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JsonSupport {
	public static String json(Map<String, Object> payloadMap) {
		String jsonPayload = null;
		try {
			Type type = new TypeToken<HashMap<String, Object>>() {
			}.getType();
			jsonPayload = new Gson().toJson(payloadMap, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonPayload;
	}

	public static Map<String, String> map(String jsonPayload) {
		Map<String, String> payload = new LinkedHashMap<>();
		if (jsonPayload != null && jsonPayload.trim().length() > 0) {
			try {
				Type type = new TypeToken<HashMap<String, String>>() {
				}.getType();
				GsonBuilder gsonBuilder = new GsonBuilder();
				Gson gson = gsonBuilder.setLenient().create();
				payload = gson.fromJson(jsonPayload, type);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return payload;
	}

	// payload via brick-mqtt-proxy is a json snippet:
	// {"_timestamp": <long>,"temperature":<int>}
	// ts in msecs, temp as documented in api (to get °C, divide by 100)
	public static Optional<String> parseValueByPayloadField(String deviceValueField, String jsonPayload) {
		Map<String, String> payloadMap = JsonSupport.map(jsonPayload);
		if (!payloadMap.isEmpty() && payloadMap.get(deviceValueField) != null) {
			return Optional.of(payloadMap.get(deviceValueField));
		} else {
			return Optional.empty();
		}
	}

	// [{"_timestamp":1462736773.718,"uid":"dzj","hardware_version":[1,1,0],
	// "device_identifier":216,"connected_uid":"6CPhaw",
	// "position":"c","firmware_version":[2,0,3]}]
	public static String normalizeEnumerationJsonPayload(String messagePayload) {
		// strip [ and ] for gson to map this payload
		String normalizedJson = messagePayload;
		if ('[' == messagePayload.charAt(0) && ']' == messagePayload.charAt(messagePayload.length() - 1)) {
			normalizedJson = messagePayload.substring(1, messagePayload.length() - 1);
		}

		// and fix un-quoted version arrays (Gson does not like this)
		normalizedJson = normalizedJson.replaceAll(":\\[", ":\"\\[");
		normalizedJson = normalizedJson.replaceAll("\\],", "\\]\",");
		normalizedJson = normalizedJson.replaceAll("\\]}", "\\]\"}");

		return normalizedJson;
	}
}
