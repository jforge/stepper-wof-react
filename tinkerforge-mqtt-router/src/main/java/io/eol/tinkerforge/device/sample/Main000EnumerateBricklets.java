package io.eol.tinkerforge.device.sample;

import java.io.IOException;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import io.eol.tinkerforge.mqtt.router.enumerator.BrickletEnumerationSupport;

public class Main000EnumerateBricklets {
	public static void main(String[] args)
			throws AlreadyConnectedException, IOException, NotConnectedException, InterruptedException {

		final IPConnection ipConnection = new IPConnection();
		ipConnection.connect(BrickDaemonHost.HOST, BrickDaemonHost.PORT);
		BrickletEnumerationSupport.addTfIpConnectionEnumerateListener(ipConnection);
		ipConnection.enumerate();
		Thread.sleep(1_000);

		ipConnection.close();
	}

}