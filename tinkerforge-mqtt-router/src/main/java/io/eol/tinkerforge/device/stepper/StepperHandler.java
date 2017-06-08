package io.eol.tinkerforge.device.stepper;

import javax.annotation.PreDestroy;

import com.tinkerforge.BrickStepper;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import io.eol.tinkerforge.device.DeviceHandler;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StepperHandler implements DeviceHandler {
	private static final Logger LOG = Logger.getLogger(StepperHandler.class);

	@Value(value = "${brickdaemon.host}")
	public String brickdHost;

	@Value(value = "${brickdaemon.port}")
	public int brickdPort;

	private IPConnection ipcon;

	private BrickStepper stepper;


	@Value(value = "${tinkerforge.actuator.stepper.uid}")
	private String uid;

	@Value(value = "${tinkerforge.actuator.stepper.max_speed:1000}")
	private int maxSpeed;

	public void connectBrick() throws Exception {

		ipcon = new IPConnection(); // Create IP connection
		stepper = new BrickStepper(uid, ipcon);

		ipcon.connect(brickdHost, brickdPort);
	}

	@PreDestroy
	public void shutdown() {
		try {
			stepper.disable();
			ipcon.disconnect();
		} catch (NotConnectedException e) {
			LOG.trace("Device not connected anymore.");
		} catch (TimeoutException e) {
			LOG.error("Timeout while shutting down.", e);
		}
	}

	public void startMotor(boolean forward, int steps, int speed) throws Exception {

		int selectedSpeed = Math.abs(speed) > Math.abs(maxSpeed) ? Math.abs(maxSpeed) : Math.abs(speed);
		int selectedSteps = Math.abs(steps) > 0 ? Math.abs(steps) : 0;

		connectBrick();

		stepper.addPositionReachedListener(new BrickStepper.PositionReachedListener() {
			public void positionReached(int position) {
				LOG.info("Position reached. Disconnect device.");
				shutdown();
			}
		});

		stepper.stop();
		stepper.disable();

		stepper.setMotorCurrent(800); // 800mA
		stepper.setStepMode((short) 8); // 1/8 step mode
		stepper.setSpeedRamping(500, 5000);
		stepper.setMaxVelocity(selectedSpeed);
		stepper.setCurrentPosition(0);

		stepper.enable(); // Enable motor power
		stepper.setSteps(selectedSteps);
	}
}
