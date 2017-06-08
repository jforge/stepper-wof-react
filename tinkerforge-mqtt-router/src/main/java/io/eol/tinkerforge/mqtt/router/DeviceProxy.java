package io.eol.tinkerforge.mqtt.router;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import io.eol.tinkerforge.mqtt.router.mqttproxy.MqttProxyConstants;
import io.eol.tinkerforge.mqtt.router.mqttproxy.MqttSupport;
import io.eol.tinkerforge.mqtt.router.util.JsonSupport;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DeviceProxy {
	private final static Logger LOG = LoggerFactory.getLogger(DeviceProxy.class);

	private long timestamp;
	private String uid;
	private String connectedUid;

	private String position;
	private String hardwareVersion;
	private String firmwareVersion;

	private IPConnection ipcon;

	private IMqttAsyncClient client;

	private Device device;

	private String topicPrefix;

	private double updateInterval = 0.0; // seconds
	private Long updateTimer = null;
	private Object updateLock = new Object();

	public DeviceProxy(String uid, String connectedUid, String position, String hardwareVersion, String firmwareVersion,
					   IPConnection ipConnection, IMqttAsyncClient client, int updateInterval) {

		this.timestamp = System.currentTimeMillis();
		this.uid = uid;
		this.connectedUid = connectedUid;
		this.position = position;
		this.hardwareVersion = hardwareVersion;
		this.firmwareVersion = firmwareVersion;
		this.ipcon = ipConnection;
		this.client = client;

		this.device = createDevice(uid, ipcon);

		this.topicPrefix = String.format("%s/%s/", getTopicPrefix(), uid);
		this.updateInterval = 0; // seconds;
		this.updateTimer = null;

		subscribe(this.topicPrefix + "_update_interval/set");

		setUpdateInterval(updateInterval);

		// self.update_locked()

		setupCallbacks();
	}

	public void handleMessage(String topicSuffix, Map<String, String> payload) {
		if (topicSuffix.equals("_update_interval/set")) {
			try {
				this.setUpdateInterval(Double.parseDouble(payload.get("_update_interval")));
			} catch (NumberFormatException e) {
				LOG.debug("payload update interval entry could not be parsed to double");
			}
		}

		updateLocked();
	}

	// def publish_as_json(self, topic, payload, *args, **kwargs):
	private void publishAsJson(String topic, Map<String, Object> payload, boolean retained) {
		String jsonPayload = JsonSupport.json(payload);
		if (!Objects.isNull(jsonPayload)) {
			MqttSupport.publishMqttMessage(this.client, MqttProxyConstants.GLOBAL_TOPIC_PREFIX_TINKERFORGE + topic, jsonPayload.getBytes(), retained);
		}
	}

	public void publishValues(String topicSuffix, Map<String, Object> __payload) {
		// payload = {'_timestamp': time.time()}
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("_timestamp", String.valueOf(System.currentTimeMillis()));

		for (Map.Entry<String, Object> entry : __payload.entrySet()) {
			// FIXME weird mappings, or review **kwargs aka __payload
			payload.put(entry.getKey(), entry.getValue());
		}

		publishAsJson(this.topicPrefix + topicSuffix, payload, true);
	}

	/**
	 * Sets updateInterval in seconds.
	 */
	public void setUpdateInterval(double updateInterval) {
		// double! self.update_interval != update_interval:
		if (Math.abs(this.updateInterval - updateInterval) > 1E-8) {
			// if difference > epsilon, publish update
			// TODO publishValues("_update_interval", __payload);
			// self.publish_values('_update_interval', _update_interval=float(update_interval))
		}

		this.updateInterval = updateInterval;

		if (this.updateInterval > 0 && this.updateTimer == null) {
			// TODO handle update thread
			// self.update_timer = threading.Timer(self.update_interval, self.update)
			// self.update_timer.start()
		}
	}

	protected void updateExtra() {
		throw new UnsupportedOperationException("to be implemented by subclass.");
	}

	public void updateGetters() {
	}

	public void updateLocked() {
		synchronized (updateLock) {
			this.updateGetters();
			this.updateExtra();
		}
	}

	public void update() {
		this.updateTimer = null;
		if (updateInterval < 1) {
			return;
		}

		this.updateLocked();

		if (updateInterval > 0) {
			// TODO handle update thread
			// self.update_timer = threading.Timer(self.update_interval, self.update)
			// self.update_timer.start()
		}
	}

	protected void setupCallbacks() {
		throw new UnsupportedOperationException("to be implemented by subclass.");
	}

	public Map<String, Object> getEnumerateEntry() {
		Map<String, Object> deviceProperties = new HashMap<>();
		deviceProperties.put("_timestamp", String.valueOf(timestamp));
		deviceProperties.put("uid", uid);
		deviceProperties.put("connectedUid", connectedUid);
		deviceProperties.put("position", position);
		deviceProperties.put("hardwareVersion", hardwareVersion);
		deviceProperties.put("firmwareVersion", firmwareVersion);
		deviceProperties.put("deviceIdentifier", String.valueOf(getDeviceIdentifier()));
		return deviceProperties;
	}

	public void subscribe(String topicSuffix) {
		String topic = MqttProxyConstants.GLOBAL_TOPIC_PREFIX_TINKERFORGE + topicSuffix;
		LOG.debug("Subscribing to " + topic);
		int qosLevel = 0;
		try {
			client.subscribe(topic, qosLevel);
		} catch (MqttException e) {
			LOG.error(e.toString());
		}
	}

	public void unsubscribe(String topicSuffix) {
		String topic = MqttProxyConstants.GLOBAL_TOPIC_PREFIX_TINKERFORGE + topicSuffix;
		LOG.debug("Unsubscribing from " + topic);
		try {
			client.unsubscribe(topic);
		} catch (MqttException e) {
			LOG.error(e.toString());
		}
	}

	public void destroy() {
		setUpdateInterval(0);
		this.unsubscribe(this.topicPrefix + "_update_interval/set");
	}

	public Device getDevice() {
		return device;
	}

	private Device createDevice(String uid, IPConnection ipcon) {
		Device device = null;
		try {
			device = (Device) getDeviceClass().getConstructor(String.class, IPConnection.class).newInstance(uid, ipcon);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			LOG.error("Error creating a io.eol.tinkerforge.device instance", e);
		}
		return device;
	}

	protected Class<?> getDeviceClass() {
		throw new UnsupportedOperationException("to be implemented by subclass.");
	}

	protected int getDeviceIdentifier() {
		throw new UnsupportedOperationException("to be implemented by subclass.");
	}

	public String getTopicPrefix() {
		throw new UnsupportedOperationException("to be implemented by subclass.");
	}

}
