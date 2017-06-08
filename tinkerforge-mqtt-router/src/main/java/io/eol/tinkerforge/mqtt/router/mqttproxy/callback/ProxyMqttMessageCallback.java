package io.eol.tinkerforge.mqtt.router.mqttproxy.callback;

import io.eol.tinkerforge.mqtt.router.mqttproxy.MqttProxyConstants;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyMqttMessageCallback implements MqttCallback {
	private final static Logger LOG = LoggerFactory.getLogger(ProxyMqttMessageCallback.class);

	@Override
	public void connectionLost(Throwable error) {
		LOG.error(error.toString());
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		LOG.info(token.toString());
	}

	//
	// TODO payload analysis
	// https://docs.python.org/2/library/json.html
	// >>> json.loads('["foo", {"bar":["baz", null, 1.0, 2]}]')
	// [u'foo', {u'bar': [u'baz', None, 1.0, 2]}]
	// >>> json.dumps([1,2,3,{'4': 5, '6': 7}], separators=(',',':'))
	// '[1,2,3,{"4":5,"6":7}]'
	//
	// https://pypi.python.org/pypi/paho-mqtt/1.1
	// class MQTTMessage:
	// """
	// topic : String. topic that the message was published on.
	// payload : String/bytes the message payload.
	// qos : Integer. The message Quality of Service 0, 1 or 2.
	// retain : Boolean. If true, the message is a retained message and not fresh.
	// mid : Integer. The message id.
	// """
	// def __init__(self):
	// self.timestamp = 0
	// self.state = mqtt_ms_invalid
	// self.dup = False
	// self.mid = 0
	// self.topic = ""
	// self.payload = None
	// self.qos = 0
	// self.retain = False
	@Override
	public void messageArrived(String sourceTopic, MqttMessage message) throws Exception {
		LOG.debug("Received message for topic " + sourceTopic);

		String topic = sourceTopic.substring(MqttProxyConstants.GLOBAL_TOPIC_PREFIX_TINKERFORGE.length());

		if (topic.startsWith("brick/") || topic.startsWith("bricklet/")) {
			String[] topicParts = topic.split("/", 3);
			String topicPrefix1 = topicParts[0];
			String topicPrefix2 = topicParts[1];
			String uid = topicParts[2];
			String topicSuffix = topicParts[3];
			String topicPrefix = topicPrefix1 + "/" + topicPrefix2;

//            Map<String, DeviceProxy> proxies = proxy.getDeviceProxies();
//            if (proxies.containsKey(uid) && topicPrefix.equals(proxies.get(uid).getTopicPrefix())) {
//                String messagePayload = new String(message.getPayload(), Charset.forName("UTF-8"));
//                Map<String, String> payload = JsonSupport.map(messagePayload);
//                if (!payload.isEmpty()) {
//                    proxies.get(uid).handleMessage(topicSuffix, payload);
//                }
//            }
		}

		LOG.debug("Unknown topic " + sourceTopic);
	}

}
