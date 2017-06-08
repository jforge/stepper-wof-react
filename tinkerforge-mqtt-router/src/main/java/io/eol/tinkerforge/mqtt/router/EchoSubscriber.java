package io.eol.tinkerforge.mqtt.router;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import io.eol.tinkerforge.device.stepper.StepperHandler;
import io.eol.tinkerforge.mqtt.router.mqttproxy.MqttProxyConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EchoSubscriber implements MqttCallback {
	private static Logger log = Logger.getLogger(EchoSubscriber.class);

	@Value(value = "${mqtt.broker.uri}")
	public String brokerUri;

	@Value(value = "${mqtt.clientId}")
	public String clientId;

	@Value(value = "${mqtt.topic_prefix.amazon_echo}")
	public String incoming_topic_prefix;

	@Value(value = "${mqtt.broker.username}")
	private String brokerUsername;

	@Value(value = "${mqtt.broker.password}")
	private String brokerPassword;

	@Autowired
	private StepperHandler stepper;

//	private DeviceService deviceService = new DeviceService();

	@Autowired
	private MqttPublisher mqttPublisher;

	@PostConstruct
	public void connect() {
		MqttClient client = null;
		try {
			client = new MqttClient(brokerUri, clientId, null);
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(true);
			options.setUserName(brokerUsername);
			options.setPassword(brokerPassword.toCharArray());

			client.connect(options);
			client.setCallback(this);
			client.subscribe(incoming_topic_prefix + "+");

			// client.close();

		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		log.debug("connection to broker lost.", cause);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {

		// workflow:
		// accept simplified messages from aws lambda
		// topics:
		// - echo/move
		// - echo/count
		// - echo/display

		// payload:
		// - move: 5000 (steps) - even more simplified for now, TODO use a simple json or csv format
		// - count: 0;100;1000 (0 to 100 with duration 1000ms per step)
		// - display: any text

		if (message == null) {
			log.warn("Payload is null. No further routing.");
		}

		String payload = message.toString();

		if (topic.endsWith("move")) {
			// move motor
			try {
				int steps = Integer.valueOf(payload);

				// TODO add more options to control the motor by voice ui

				stepper.startMotor(true, steps, 500);
			} catch (Exception ex) {
				log.error("Error while processing data or moving the stepper", ex);
			}

		} else if (topic.endsWith("display")) {
			// display text
			try {
				int displayWidth = 20;
				int displayLines = 4;
				// contraints: 20 character per line, 4 lines, no display on texts exceeding limits
				payload = StringUtils.abbreviate(payload, displayLines * displayWidth);
				int lines = (displayWidth + payload.length()) / (displayWidth + 1);

				mqttPublisher.clearLcdDisplay(MqttProxyConstants.BRICKLET_TOPIC_LCD_20x4);

				for (int i = 0; i < lines; i++) {
					Map<String, Object> payloadMap = new LinkedHashMap<>();
					payloadMap.put("line", i);
					payloadMap.put("position", 0);
					payloadMap.put("text", StringUtils.substring(payload, i * displayWidth, (i + 1) * displayWidth));
					mqttPublisher.publish(MqttProxyConstants.BRICKLET_TOPIC_LCD_20x4, payloadMap);
				}
			} catch (Exception ex) {
				log.error("Error while processing data", ex);
			}

		} else if (topic.endsWith("count")) {
			// count on segments
			Map<String, Object> payloadMap = new LinkedHashMap<>();
			try {
				int value = Integer.valueOf(payload);
				payloadMap.put("value_from", 0);
				payloadMap.put("value_to", Math.abs(value));
				payloadMap.put("increment", 1);
				payloadMap.put("length", 1000);

				// TODO add more options to control the display by voice ui

				mqttPublisher.publish(MqttProxyConstants.BRICKLET_TOPIC_SEGMENT_DISPLAY_4x7, payloadMap);
			} catch (Exception ex) {
				log.error("Error while processing data", ex);
			}
		} else if (topic.endsWith("button")) {
			// display text
			mqttPublisher.publish("blackhole", Collections.singletonMap("message", "You are not allowed to publish here"));
		} else {
			log.info("no handler found for topic: " + topic);
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
	}
}
