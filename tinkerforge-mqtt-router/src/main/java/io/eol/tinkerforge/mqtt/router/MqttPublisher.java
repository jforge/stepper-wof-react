package io.eol.tinkerforge.mqtt.router;

import java.util.Map;
import java.util.Objects;

import io.eol.tinkerforge.mqtt.router.mqttproxy.MqttProxyConstants;
import io.eol.tinkerforge.mqtt.router.mqttproxy.MqttSupport;
import io.eol.tinkerforge.mqtt.router.util.JsonSupport;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MqttPublisher {
	private static Logger log = Logger.getLogger(MqttPublisher.class);

	@Value(value = "${mqtt.broker.uri}")
	public String brokerUri;

	@Value(value = "${mqtt.clientId}")
	public String clientId;

	@Value(value = "${mqtt.topic_prefix.tinkerforge}")
	public String outgoing_topic_prefix;

	@Value(value = "${mqtt.broker.username}")
	private String brokerUsername;

	@Value(value = "${mqtt.broker.password}")
	private String brokerPassword;

//	@Autowired
//	private DeviceService deviceService;

	@Value(value = "${tinkerforge.actuator.segment_display_4x7.first.uid}")
	public String uid_segment_display_first;

	@Value(value = "${tinkerforge.actuator.segment_display_4x7.second.uid}")
	public String uid_segment_display_second;

	@Value(value = "${tinkerforge.actuator.segment_display_4x7.first.brightness}")
	public String segment_brightness;

	@Value(value = "${tinkerforge.actuator.lcd_20x4.uid}")
	public String uid_lcd_display;

	@Value(value = "${tinkerforge.actuator.dual_button.uid}")
	public String uid_dual_button;

	public void publish(String topic, Map<String, Object> payloadMap) {
		// TODO use the MqttProxy routines already specified
		// TODO more sophisticated control parameter mapping
		try {
			MqttClient client = connectBroker();
			int qosLevel = 0;
			boolean retained = false;

			if (topic.endsWith(MqttProxyConstants.BRICKLET_NAME_SEGMENT_DISPLAY)) {
				String brickletTopic = String.format("%s/%s/%s", topic, uid_segment_display_first, "start_counter/set");
				publishAsJson(client, brickletTopic, payloadMap, qosLevel, retained);
				brickletTopic = String.format("%s/%s/%s", topic, uid_segment_display_second, "start_counter/set");
				publishAsJson(client, brickletTopic, payloadMap, qosLevel, retained);
			} else if (topic.endsWith(MqttProxyConstants.BRICKLET_NAME_LCD_20x4)) {
				String brickletTopic = String.format("%s/%s/%s", topic, uid_lcd_display, "write_line/set");
				publishAsJson(client, brickletTopic, payloadMap, qosLevel, retained);
			} else if (topic.endsWith(MqttProxyConstants.BRICKLET_NAME_DUAL_BUTTON)) {
				String brickletTopic = String.format("%s/%s/%s", topic, uid_dual_button, "led_state/set");
				publishAsJson(client, brickletTopic, payloadMap, qosLevel, retained);
				log.info("dual button not yet supported.");
			}

			client.disconnect();
			client.close();
		} catch (MqttException e) {
			log.error("", e);
		}
	}

	public MqttClient connectBroker() throws MqttException {
		MqttClient client = new MqttClient(brokerUri, clientId + "_publisher", null);
		MqttConnectOptions options = new MqttConnectOptions();
		options.setKeepAliveInterval(0);
		options.setConnectionTimeout(1);
		options.setCleanSession(true);
		options.setUserName(brokerUsername);
		options.setPassword(brokerPassword.toCharArray());
		client.setCallback(null);
		client.connect(options);
		return client;
	}

	public void disconnectBroker(MqttClient client) throws MqttException {
		client.disconnect();
		client.close();
	}

	public void clearLcdDisplay(String topic) {
		try {
			MqttClient client = connectBroker();

			String brickletTopic = String.format("%s/%s/%s", topic, uid_lcd_display, "backlight_off/set");
			MqttSupport.publishMqttMessage(client, brickletTopic, "".getBytes(), false);
			brickletTopic = String.format("%s/%s/%s", topic, uid_lcd_display, "clear_display/set");
			MqttSupport.publishMqttMessage(client, brickletTopic, "".getBytes(), false);
			brickletTopic = String.format("%s/%s/%s", topic, uid_lcd_display, "backlight_on/set");
			MqttSupport.publishMqttMessage(client, brickletTopic, "".getBytes(), false);

			disconnectBroker(client);
		} catch (MqttException e) {
			log.error("", e);
		}
	}

	private void publishAsJson(MqttClient client, String topic, Map<String, Object> payloadMap, int qos, boolean retained) {
		String jsonPayload = JsonSupport.json(payloadMap);
		if (!Objects.isNull(jsonPayload)) {
			MqttSupport.publishMqttMessage(client, topic, jsonPayload.getBytes(), retained);
		}
	}
}
