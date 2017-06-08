package io.eol.tinkerforge.mqtt.router.enumerator;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduled Executor for periodical io.eol.tinkerforge.device enumeration.
 */
public class EnumerationExecutor {
	private final static Logger LOG = LoggerFactory.getLogger(EnumerationExecutor.class);

	private ScheduledExecutorService executorDeviceEnumeration;

	public EnumerationExecutor() {
		executorDeviceEnumeration = Executors.newScheduledThreadPool(1);
	}

	/**
	 * Eumerates connected Devices.
	 */
	public void startDeviceEnumeration(IPConnection ipcon, int enumerationInterval) {
		Runnable task = () -> {
			try {
				LOG.info("Device Enumeration: " + new Date());
				ipcon.enumerate();
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		};

		int initialDelay = 1;
		int period = enumerationInterval;
		executorDeviceEnumeration.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
	}

	public void stopDeviceEnumeration() {
		try {
			LOG.debug("Device Executor shutdown: " + new Date());
			executorDeviceEnumeration.shutdown();
			executorDeviceEnumeration.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOG.error("Tasks interrupted");
		} finally {
			if (!executorDeviceEnumeration.isTerminated()) {
				LOG.error("Cancel non-finished tasks");
			}
			executorDeviceEnumeration.shutdownNow();
			LOG.debug("Device Executor shutdown finished.");
		}
	}

}
