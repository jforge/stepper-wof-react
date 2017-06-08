package io.eol.tinkerforge.device.sample;

import com.tinkerforge.BrickletLoadCell;
import com.tinkerforge.BrickletLoadCell.WeightListener;
import com.tinkerforge.IPConnection;

/**
 * https://www.tinkerforge.com/de/doc/Hardware/Bricklets/Load_Cell.html
 */
public class Main006LoadCellBricklet {
	private static final String UID = "uzS"; // Change to your UID

	// Note: To make the example code cleaner we do not handle exceptions. Exceptions you
	// might normally want to catch are described in the documentation
	public static void main(String args[]) throws Exception {
		IPConnection ipcon = new IPConnection(); // Create IP connection
		BrickletLoadCell bricklet = new BrickletLoadCell(UID, ipcon); // Create io.eol.tinkerforge.device object

		ipcon.connect(BrickDaemonHost.HOST, BrickDaemonHost.PORT); // Connect to brickd
		// Don't use io.eol.tinkerforge.device before ipcon is connected

		bricklet.setDebouncePeriod(1000);
		// bricklet.setMovingAverage(average);
		bricklet.setWeightCallbackPeriod(500);
		bricklet.setWeightCallbackThreshold('i', (short) 1, (short) 1000);

		// bricklet.addWeightReachedListener(listener);

		bricklet.addWeightListener(new WeightListener() {

			@Override
			public void weight(int weight) {
				System.out.println("Weight: " + weight);
			}
		});

		System.out.println("Press key to exit");
		System.in.read();

		ipcon.disconnect();
	}
}