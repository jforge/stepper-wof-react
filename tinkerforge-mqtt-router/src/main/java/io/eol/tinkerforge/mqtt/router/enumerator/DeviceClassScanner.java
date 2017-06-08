package io.eol.tinkerforge.mqtt.router.enumerator;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tinkerforge.Device;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceClassScanner {
	private final static Logger LOG = LoggerFactory.getLogger(DeviceClassScanner.class);

	private static Map<Integer, Class<? extends Device>> deviceMap = new HashMap<>();

	static {
		List<Class<? extends Device>> deviceClasses = new ArrayList<>();
		new FastClasspathScanner("com.tinkerforge").matchSubclassesOf(Device.class, deviceClasses::add).scan();
		for (Class<? extends Device> deviceSubClass : deviceClasses) {
			try {
				Field deviceIdentifierField = deviceSubClass.getDeclaredField("DEVICE_IDENTIFIER");
				int deviceIdentifier = deviceIdentifierField.getInt(null);
				deviceMap.put(deviceIdentifier, deviceSubClass);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			}
		}
	}

	public static void main(String[] args) {
		Instant t1 = Instant.now();
		System.out.println(getDeviceClassForId(219));
		Duration d = Duration.between(t1, Instant.now());
		System.out.println(d);
	}

	public static Class<? extends Device> getDeviceClassForId(int deviceIdentifier) {
		return deviceMap.get(deviceIdentifier);
	}

}
