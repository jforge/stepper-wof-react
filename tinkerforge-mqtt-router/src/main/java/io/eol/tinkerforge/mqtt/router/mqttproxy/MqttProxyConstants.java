package io.eol.tinkerforge.mqtt.router.mqttproxy;

import java.util.function.Function;

public interface MqttProxyConstants {
	String GLOBAL_TOPIC_PREFIX_TINKERFORGE = "tinkerforge/";

	int UPDATE_INTERVAL = 3; // seconds
	int ENUMERATE_INTERVAL = 15; // seconds
	int QUIESCE_TIMEOUT = 2; // seconds

	Function<String, String> deviceTopicPrefix = deviceName -> GLOBAL_TOPIC_PREFIX_TINKERFORGE + "bricklet/" + deviceName;

	String BRICKLET_NAME_SEGMENT_DISPLAY = "segment_display_4x7";
	String BRICKLET_NAME_LCD_20x4 = "lcd_20x4";
	String BRICKLET_NAME_LOAD_CELL = "load_cell";
	String BRICKLET_NAME_DUAL_BUTTON = "dual_button";

	String BRICKLET_TOPIC_SEGMENT_DISPLAY_4x7 = deviceTopicPrefix.apply(BRICKLET_NAME_SEGMENT_DISPLAY);
	String BRICKLET_TOPIC_LCD_20x4 = deviceTopicPrefix.apply(BRICKLET_NAME_LCD_20x4);
	String BRICKLET_TOPIC_LOAD_CELL = deviceTopicPrefix.apply(BRICKLET_NAME_LOAD_CELL);
	String BRICKLET_TOPIC_DUAL_BUTTON = deviceTopicPrefix.apply(BRICKLET_NAME_DUAL_BUTTON);
}

