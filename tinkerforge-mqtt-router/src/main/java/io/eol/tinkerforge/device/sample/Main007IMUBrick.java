package io.eol.tinkerforge.device.sample;

import com.tinkerforge.BrickIMU;
import com.tinkerforge.IPConnection;

/**
 * http://www.tinkerforge.com/de/doc/Hardware/Bricks/IMU_Brick.html
 */
public class Main007IMUBrick {
	private static final String UID = "6DdNWN"; // Change to your UID

	// Note: To make the example code cleaner we do not handle exceptions.
	// Exceptions you might normally want to catch are described in the documentation
	public static void main(String args[]) throws Exception {
		IPConnection ipcon = new IPConnection(); // Create IP connection
		BrickIMU imu = new BrickIMU(UID, ipcon);

		ipcon.connect(BrickDaemonHost.HOST, BrickDaemonHost.PORT); // Connect to brickd
		// Don't use io.eol.tinkerforge.device before ipcon is connected

		// Get current quaternion
		BrickIMU.Quaternion quaternion = imu.getQuaternion(); // Can throw com.tinkerforge.TimeoutException

		System.out.println("Quaternion[X]: " + quaternion.x);
		System.out.println("Quaternion[Y]: " + quaternion.y);
		System.out.println("Quaternion[Z]: " + quaternion.z);
		System.out.println("Quaternion[W]: " + quaternion.w);

		// Set Period for callback to 1s (1000ms)
		// Note: The callback is only called every second if the
		// sample has changed since the last call!
		long period = 1000L;

		imu.setAngularVelocityPeriod(period);
		imu.addAngularVelocityListener((x, y, z) -> {

			// Gibt die kalibrierten Winkelgeschwindigkeiten des Gyroskops
			// für die X-, Y- und Z-Achse in °/14,375s zurück.
			// (Um den Wert in °/s zu erhalten ist es notwendig durch 14,375 zu teilen)
			double q = 14.375;
			System.out.println(String.format("Winkelgeschwindigkeit x,y,z in °/s = %s, %s, %s", x / q, y / q, z / q));
		});

		System.out.println("Press key to exit");
		System.in.read();

		ipcon.disconnect();
	}

}