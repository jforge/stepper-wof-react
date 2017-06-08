package io.eol.tinkerforge.mqtt.router.mqttproxy.callback;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyMqttDisconnectListener implements IMqttActionListener {
	private final static Logger LOG = LoggerFactory.getLogger(ProxyMqttDisconnectListener.class);

	@Override
	public void onFailure(IMqttToken token, Throwable error) {
		LOG.error("Failed to disconnect from MqttBroker: " + token.toString() + ", " + error.getMessage());
	}

	@Override
	public void onSuccess(IMqttToken token) {
//        if (!Objects.isNull(proxy)) {
//            try {
//                if (!Objects.isNull(proxy.getIpcon())) {
//                    proxy.getIpcon().disconnect();
//                }
//            } catch (NotConnectedException e) {
//                e.printStackTrace();
//            }
//
//            for (DeviceProxy io.eol.tinkerforge.device : proxy.getDeviceProxies().values()) {
//                io.eol.tinkerforge.device.destroy();
//            }
//            proxy.setDeviceProxies(new HashMap<>());
//        }

		LOG.info("Successfully disconnected from MqttBroker: " + token);
	}

}
