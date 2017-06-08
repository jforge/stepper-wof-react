package io.eol.tinkerforge.mqtt.router.enumerator;

import com.tinkerforge.IPConnection;

public class BrickletEnumerationSupport {
	public static void addTfIpConnectionEnumerateListener(IPConnection ipConnection) {
		ipConnection.addEnumerateListener(
				(uid, connectedUid, position, hardwareVersion, firmwareVersion, deviceIdentifier, enumerationType) -> {
					System.out.println("UID:               " + uid);
					System.out.println("Enumeration Type:  " + enumerationType);
					System.out.println("Connected UID:     " + connectedUid);
					System.out.println("Position:          " + position);
					System.out.println(
							"Hardware Version:  " + hardwareVersion[0] + "." + hardwareVersion[1] + "." + hardwareVersion[2]);
					System.out.println(
							"Firmware Version:  " + firmwareVersion[0] + "." + firmwareVersion[1] + "." + firmwareVersion[2]);
					System.out.println("Device Identifier: " + deviceIdentifier);
					System.out.println("Device Class: " + DeviceClassScanner.getDeviceClassForId(deviceIdentifier));
					System.out.println("");
				});
	}

}
