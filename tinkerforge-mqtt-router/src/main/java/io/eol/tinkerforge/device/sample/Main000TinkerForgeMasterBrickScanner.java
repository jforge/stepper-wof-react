package io.eol.tinkerforge.device.sample;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tinkerforge.IPConnectionBase.CONNECTION_STATE_CONNECTED;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import io.eol.tinkerforge.mqtt.router.enumerator.BrickletEnumerationSupport;
import io.eol.tinkerforge.mqtt.router.enumerator.DeviceClassScanner;
import io.eol.tinkerforge.mqtt.router.util.NetworkUtils;

public class Main000TinkerForgeMasterBrickScanner {

	// public static final String NETWORK = "192.168.0.";
	//ipConnection.connect(BrickDaemonHost.HOST_COLOR, BrickDaemonHost.PORT);

	public static void main(String[] args) throws Exception {
		DeviceClassScanner.getDeviceClassForId(0); // init io.eol.tinkerforge.device map

		String localSubnetHostSearchPrefix = NetworkUtils.getLocalSubnetHostSearchPrefix();

		IntStream.range(1, 254).boxed().parallel().filter(ip -> {
			try {
				final String host = localSubnetHostSearchPrefix + ip;
				// System.out.println("will try host = " + host);
				Socket so = new Socket();
				final InetSocketAddress endpoint = new InetSocketAddress(host, 4223);
				so.connect(endpoint, 100);
				final boolean connected = so.isConnected();
				so.close();
				return connected;
			} catch (IOException e) {
				// e.printStackTrace();
			}
			return false;
		}).map(v -> {
			IPConnection ipConnection = new IPConnection();
			ipConnection.setTimeout(250);
			try {
				final String host = localSubnetHostSearchPrefix + v;
				System.out.println("host = " + host);
				ipConnection.connect(host, 4223);
			} catch (IOException | AlreadyConnectedException e) {
				// e.printStackTrace();
			}
			System.out.println("ipConnection = " + ipConnection);
			return ipConnection;
		}).filter(c -> c.getConnectionState() == CONNECTION_STATE_CONNECTED).collect(Collectors.toList()).forEach(con -> {
			BrickletEnumerationSupport.addTfIpConnectionEnumerateListener(con);
			try {
				con.enumerate();
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		});


		System.out.println("Press key to exit");
		System.in.read();

	}
}