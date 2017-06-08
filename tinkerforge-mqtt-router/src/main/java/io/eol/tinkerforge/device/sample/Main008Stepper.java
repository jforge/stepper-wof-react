package io.eol.tinkerforge.device.sample;

import java.util.Random;

import com.tinkerforge.BrickStepper;
import com.tinkerforge.IPConnection;
import com.tinkerforge.TinkerforgeException;

/**
 * https://www.tinkerforge.com/de/doc/Hardware/Bricks/Stepper_Brick.html
 */
public class Main008Stepper {
	private final String UID = "62Ydqh"; // Change to your UID

	private IPConnection ipcon;

	private BrickStepper stepper;

	public Main008Stepper() throws Exception {

		ipcon = new IPConnection(); // Create IP connection
		stepper = new BrickStepper(UID, ipcon);

		ipcon.connect(BrickDaemonHost.HOST, BrickDaemonHost.PORT); // Connect to brickd
		// Don't use io.eol.tinkerforge.device before ipcon is connected
	}

	// Note: To make the example code cleaner we do not handle exceptions.
	// Exceptions you might normally want to catch are described in the documentation
	public static void main(String args[]) throws Exception {
		Main008Stepper stepper = new Main008Stepper();

		//stepper.doItSimple();
		// stepper.doItWithCallback(ipcon, stepper);
		stepper.previewWithCallback();

		System.out.println("Press key to exit");
		System.in.read();

		stepper.shutdown();
	}

	public void shutdown() throws Exception {
		stepper.disable();
		ipcon.disconnect();
	}

	public void doItSimple() throws Exception {
		System.out.println("alldata: " + stepper.getAllData());

		// Set Period for callback to 1s (1000ms)
		// Note: The callback is only called every second if the
		// sample has changed since the last call!
		long period = 1000L;

		stepper.setAllDataPeriod(period);
		stepper.addAllDataListener((cv, cp, rs, sv, ev, cc) -> {
			// allData(int currentVelocity, int currentPosition, int remainingSteps, int stackVoltage, int externalVoltage, int currentConsumption);
			System.out.println("alldata changed: " + cp);
		});

		stepper.addUnderVoltageListener((v) -> {
			System.out.println("voltage too low: " + v);
		});

		stepper.addPositionReachedListener((p) -> {
			System.out.println("position reached: " + p);
		});

		stepper.setMotorCurrent(800); // 800mA
		stepper.setStepMode((short) 8); // 1/8 step mode
		stepper.setMaxVelocity(1000); // Velocity 2000 steps/s
		stepper.setCurrentPosition(0);

		// Slow acceleration (500 steps/s^2),
		// Fast deacceleration (5000 steps/s^2)
		stepper.setSpeedRamping(500, 5000);

		stepper.enable(); // Enable motor powe
//        stepper.setTargetPosition(500);
		stepper.setSteps(8000); // Drive 60000 steps forward
		//        stepper.driveForward();
	}

	private void doItWithCallback(IPConnection ipcon, BrickStepper stepper) throws Exception {

		// Use position reached callback to program random movement
		stepper.addPositionReachedListener(new BrickStepper.PositionReachedListener() {
			Random random = new Random();

			public void positionReached(int position) {
				int steps = 0;
				if (random.nextInt(2) == 1) {
					steps = random.nextInt(4001) + 1000; // steps (forward)
				} else {
					steps = random.nextInt(5001) - 6000; // steps (backward)
				}

				int vel = random.nextInt(1801) + 200; // steps/s
				int acc = random.nextInt(901) + 100; // steps/s^2
				int dec = random.nextInt(901) + 100; // steps/s^2
				System.out.println("Configuration (vel, acc, dec): (" +
						vel + ", " + acc + ",  " + dec + ")");

				try {
					stepper.setSpeedRamping(acc, dec);
					stepper.setMaxVelocity(vel);
					stepper.setSteps(steps);
				} catch (TinkerforgeException e) {
				}
			}
		});

		stepper.enable(); // Enable motor power
		stepper.setSteps(1); // Drive one step forward to get things going

	}

	public void previewWithCallback() throws Exception {

		// Use position reached callback to program random movement
		stepper.addPositionReachedListener(new BrickStepper.PositionReachedListener() {
			public void positionReached(int position) {
				try {
					shutdown();
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});

		stepper.setMotorCurrent(800); // 800mA
		stepper.setStepMode((short) 8); // 1/8 step mode
		stepper.setMaxVelocity(1000); // Velocity 2000 steps/s
		stepper.setCurrentPosition(0);
		stepper.setSpeedRamping(500, 5000);

		stepper.enable(); // Enable motor power
		stepper.setSteps(8000); // Drive 60000 steps forward

	}
}