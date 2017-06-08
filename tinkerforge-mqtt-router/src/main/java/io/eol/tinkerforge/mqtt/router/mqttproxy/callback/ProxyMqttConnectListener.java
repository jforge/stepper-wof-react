package io.eol.tinkerforge.mqtt.router.mqttproxy.callback;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyMqttConnectListener implements IMqttActionListener {
	private final static Logger LOG = LoggerFactory.getLogger(ProxyMqttConnectListener.class);

	@Override
	public void onFailure(IMqttToken token, Throwable error) {
		LOG.error("Failed to connect to MqttBroker: " + token.toString() + ", " + error.getMessage());
	}

	@Override
	public void onSuccess(IMqttToken token) {
		LOG.info("Successfully connected to MqttBroker: " + token);
//        if (!Objects.isNull(proxy)) {
//            proxy.connectBrickDaemon();
//        }
	}

}
