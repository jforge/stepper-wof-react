package io.eol.tinkerforge.mqtt.router.mqttproxy;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static io.eol.tinkerforge.mqtt.router.mqttproxy.MqttProxyConstants.ENUMERATE_INTERVAL;
import static io.eol.tinkerforge.mqtt.router.mqttproxy.MqttProxyConstants.QUIESCE_TIMEOUT;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.IPConnection;
import com.tinkerforge.TinkerforgeException;
import io.eol.tinkerforge.mqtt.router.DeviceProxy;
import io.eol.tinkerforge.mqtt.router.enumerator.EnumerationExecutor;
import io.eol.tinkerforge.mqtt.router.mqttproxy.callback.ProxyMqttConnectListener;
import io.eol.tinkerforge.mqtt.router.mqttproxy.callback.ProxyMqttDisconnectListener;
import io.eol.tinkerforge.mqtt.router.mqttproxy.callback.ProxyMqttMessageCallback;
import io.eol.tinkerforge.mqtt.router.util.JsonSupport;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttProxy {
	private final static Logger LOG = LoggerFactory.getLogger(MqttProxy.class);

	private String brickdHost;
	private int brickdPort;
	private String brokerHost;
	private int brokerPort;
	private int updateInterval;

	private IPConnection ipcon;
	private EnumerationExecutor enumerator;

	private MqttAsyncClient client;
	private IMqttActionListener mqttConnectListener;
	private IMqttActionListener mqttDisconnectListener;

	private Map<String, DeviceProxy> deviceProxies;
	private Map<Integer, Class<? extends DeviceProxy>> deviceProxyClasses;

	public MqttProxy(String brickdHost, int brickdPort, String brokerHost, int brokerPort, int updateInterval) {
		this.brickdHost = brickdHost;
		this.brickdPort = brickdPort;
		this.brokerHost = brokerHost;
		this.brokerPort = brokerPort;
		this.updateInterval = updateInterval;

		// Device Connection
		this.ipcon = new IPConnection();
		addIpconConnectCallback();
		addIpconEnumerateCallback();

		this.enumerator = new EnumerationExecutor();

		// Mqtt Connection
		String broker = "tcp://" + this.brokerHost + ":" + this.brokerPort;
		String clientId = "BrickProxy_" + MqttClient.generateClientId();
		MemoryPersistence persistence = new MemoryPersistence();
		this.mqttConnectListener = new ProxyMqttConnectListener();
		this.mqttDisconnectListener = new ProxyMqttDisconnectListener();
		try {
			client = new MqttAsyncClient(broker, clientId, persistence);
		} catch (MqttException e) {
			LOG.error(e.toString());
		}
		client.setCallback(new ProxyMqttMessageCallback());

		// Device Proxies
		deviceProxies = new HashMap<>();
	}

	public IPConnection getIpcon() {
		return ipcon;
	}

	public Map<String, DeviceProxy> getDeviceProxies() {
		return deviceProxies;
	}

	public void setDeviceProxies(Map<String, DeviceProxy> deviceProxies) {
		this.deviceProxies = deviceProxies;
	}

	public Map<Integer, Class<? extends DeviceProxy>> getDeviceProxyClasses() {
		return deviceProxyClasses;
	}

	public void connectBrickDaemon() {
		try {
			ipcon.connect(brickdHost, brickdPort);
			LOG.info("Brick Daemon Connect: " + new Date());
		} catch (AlreadyConnectedException | IOException e) {
			e.printStackTrace();
			System.exit(-2);
		}
	}

	/**
	 * Connects to MQTT Broker.
	 */
	private void connect() {
		try {
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			client.connect(connOpts, mqttConnectListener);
			LOG.info("MqttClient Connect: " + new Date());
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		enumerator.startDeviceEnumeration(ipcon, ENUMERATE_INTERVAL);
	}

	/**
	 * Disconnects from MQTT Broker.
	 */
	private void shutdown() {
		enumerator.stopDeviceEnumeration();

		try {
			if (this.client.isConnected()) {
				this.client.disconnect(QUIESCE_TIMEOUT * 1000, mqttDisconnectListener);
			}
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	// def publish_as_json(self, topic, payload, *args, **kwargs):
	private void publishAsJson(String topic, Map<String, Object> payload, boolean retained) {
		String jsonPayload = JsonSupport.json(payload);
		if (!Objects.isNull(jsonPayload)) {
			MqttSupport.publishMqttMessage(this.client, MqttProxyConstants.GLOBAL_TOPIC_PREFIX_TINKERFORGE + topic, jsonPayload.getBytes(), retained);
		}
	}

	// for uid, device_proxy in self.device_proxies.items():
	// if not connected and uid == changed_uid or device_proxy.TOPIC_PREFIX != topic_prefix:
	// continue
	//
	// enumerate_entries.append(device_proxy.get_enumerate_entry())
	//
	// self.publish_as_json('enumerate/available/' + topic_prefix, enumerate_entries, retain=True)

	private void publishEnumerate(String changedUid, boolean connected) {
		DeviceProxy deviceProxy = deviceProxies.get(changedUid);
		String topicPrefix = deviceProxy.getTopicPrefix();
		String topic = "";
		if (connected) {
			topic = "enumerate/connected/" + topicPrefix;
		} else {
			topic = "enumerate/disconnected/" + topicPrefix;
		}

		LOG.info(String.format("UID: %s, target topic: %s", changedUid, topic));

		// self.publish_as_json(topic, device_proxy.get_enumerate_entry())
		this.publishAsJson(topic, deviceProxy.getEnumerateEntry(), false);

		Map<String, Object> enumerateEntries = new LinkedHashMap<>();

		for (Map.Entry<String, DeviceProxy> entry : deviceProxies.entrySet()) {
			deviceProxy = entry.getValue();
			if (!connected && entry.getKey().equals(changedUid) || !deviceProxy.getTopicPrefix().equals(topicPrefix)) {
				continue;
			}
			// TODO no, build a more complex map of maps!
			enumerateEntries.putAll(deviceProxy.getEnumerateEntry());
		}

		// TODO
		// self.publish_as_json('enumerate/available/' + topic_prefix, enumerate_entries, retain=True)
		this.publishAsJson("enumerate/available/" + topicPrefix, enumerateEntries, true);

	}

	/**
	 * Adds a ConnectListener to the TinkerForge IPConnection.
	 */
	private void addIpconConnectCallback() {
		ipcon.addConnectedListener((short connectReason) -> {
			switch (connectReason) {
				case IPConnection.CONNECT_REASON_REQUEST:
					LOG.info("Connected by request");
					break;

				case IPConnection.CONNECT_REASON_AUTO_RECONNECT:
					LOG.info("Auto-Reconnect");
					break;
			}

			// authenticate
			// try {
			// ipcon.authenticate(SECRET);
			// LOG.info("Authentication succeeded");
			// } catch (TinkerforgeException e) {
			// LOG.info("Could not authenticate: " + e.getMessage());
			// return;
			// }

			// enumerate
			try {
				ipcon.enumerate();
			} catch (TinkerforgeException e) {
			}
		});
	}

	/**
	 * Adds a EnumerateListener to the TinkerForge IPConnection.
	 */
	private void addIpconEnumerateCallback() {
		ipcon.addEnumerateListener((String uid, String connectedUid, char position, short[] hardwareVersion,
									short[] firmwareVersion, int deviceIdentifier, short enumerationType) -> {
			LOG.info(String.format("UID: %s, Enumeration Type: %s", uid, enumerationType));

			if (enumerationType == IPConnection.ENUMERATION_TYPE_DISCONNECTED) {
				if (deviceProxies.containsKey(uid)) {
					publishEnumerate(uid, false);
					deviceProxies.get(uid).destroy();
					deviceProxies.remove(uid);
				} else if (deviceProxyClasses.containsKey(deviceIdentifier)) {
					// TODO
					String hwVersion = hardwareVersion.toString();
					String fwVersion = firmwareVersion.toString();
					DeviceProxy deviceProxy = new DeviceProxy(uid, connectedUid, String.valueOf(position), hwVersion, fwVersion,
							ipcon, client, updateInterval);
					deviceProxies.put(uid, deviceProxy);
					publishEnumerate(uid, true);
				}
			}
		});
	}

}
