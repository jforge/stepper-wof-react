package io.eol.tinkerforge.mqtt.router.mqttproxy;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttSupport {
	private final static Logger LOG = LoggerFactory.getLogger(MqttSupport.class);

	public static int DEFAULT_QOS = 0;

	/**
	 * Publishes a MqttProxy message to the Broker.
	 *
	 * @param client   mqtt client instance to publish a message to
	 * @param topic    topic to publish the message on
	 * @param payload  the byte array to send as message.
	 * @param retained set the retained message flag true/false
	 */
	public static void publishMqttMessage(IMqttAsyncClient client, String topic, byte[] payload, boolean retained) {
		int qosLevel = DEFAULT_QOS;
		if (client != null) {
			try {
				// self.client.publish(GLOBAL_TOPIC_PREFIX + topic, json.dumps(payload, separators=(',',':')), *args, **kwargs)
				client.publish(topic, payload, qosLevel, retained);
			} catch (MqttException e) {
				LOG.error(e.toString());
			}
		}
	}

	public static void publishMqttMessage(IMqttClient client, String topic, byte[] payload, boolean retained) {
		int qosLevel = DEFAULT_QOS;
		if (client != null) {
			try {
				// self.client.publish(GLOBAL_TOPIC_PREFIX + topic, json.dumps(payload, separators=(',',':')), *args, **kwargs)
				client.publish(topic, payload, qosLevel, retained);
			} catch (MqttException e) {
				LOG.error(e.toString());
			}
		}
	}
}
