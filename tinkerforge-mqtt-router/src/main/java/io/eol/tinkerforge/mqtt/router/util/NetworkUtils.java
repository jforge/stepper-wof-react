package io.eol.tinkerforge.mqtt.router.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class NetworkUtils {

//    Any address in the range 127.xxx.xxx.xxx is a "loopback" address. It is only visible to "this" host.
//    Any address in the range 192.168.xxx.xxx is a private (aka site local) IP address.
//       These are reserved for use within an organization.
//       The same applies to 10.xxx.xxx.xxx addresses,
//       and 172.16.xxx.xxx through 172.31.xxx.xxx.
//    Addresses in the range 169.254.xxx.xxx are link local IP addresses.
//       These are reserved for use on a single network segment.
//    Addresses in the range 224.xxx.xxx.xxx through 239.xxx.xxx.xxx are multicast addresses.
//    The address 255.255.255.255 is the broadcast address.
//    Anything else should be a valid public point-to-point IPv4 address.

	private static final Pattern IPv4RegexPattern = Pattern.compile(
			"^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	public static void main(String[] args) throws SocketException, UnknownHostException {
		Pair<InetAddress, Short> subnetTupel = NetworkUtils.getIPv4AddressInfo();
		InetAddress sub = getIPv4LocalNetMask(subnetTupel.getLeft(), subnetTupel.getRight());
		System.out.println(sub);
		System.out.println(getLocalSubnetMask());
		System.out.println(getLocalSubnetHostSearchPrefix());
	}

	public static String getLocalSubnetMask() {
		Pair<InetAddress, Short> ipv4AddressInfo = NetworkUtils.getIPv4AddressInfo();
		InetAddress sub = getIPv4LocalNetMask(ipv4AddressInfo.getLeft(), ipv4AddressInfo.getRight());
		return sub != null ? sub.getHostAddress().replaceFirst("^/", "") : null;
	}

	public static String getLocalSubnetHostSearchPrefix() {
		Pair<InetAddress, Short> ipv4AddressInfo = NetworkUtils.getIPv4AddressInfo();
		String getLocalSubnetMask = getLocalSubnetMask();

		String ipv4 = ipv4AddressInfo.getLeft().getHostAddress().replaceFirst("^/", "");
		String[] parts = ipv4.split("\\.");
		String[] subnetParts = getLocalSubnetMask != null ? getLocalSubnetMask.split("\\.") : new String[0];
		StringBuilder prefix = new StringBuilder();
		int c = 0;
		for (String subnetPart : subnetParts) {
			prefix.append("0".equals(subnetPart) ? "" : parts[c] + ".");
			c++;
		}
		return prefix.toString();
	}

	private static Pair<InetAddress, Short> getIPv4AddressInfo() {
		Pair<InetAddress, Short> subnetTupel = null;
		try {
			InetAddress localHost = Inet4Address.getLocalHost();
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
			for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
				if (verifyIPv4(address.getAddress().getHostAddress())) {
					subnetTupel = new ImmutablePair<>(address.getAddress(), address.getNetworkPrefixLength());
					break;
				}
			}
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
		}

		// System.out.println(subnetTupel);

		return subnetTupel;
	}

	/**
	 * Gets network mask for the IP address and network prefix specified.
	 * The network mask will be returned has an IP, use .getHostAddress() for string representation..
	 */
	private static InetAddress getIPv4LocalNetMask(InetAddress ip, int netPrefix) {
		try {
			// since this is for IPv4, it's 32 bits, so set the sign value of the int to "negative".
			int shiftby = (1 << 31);
			// for the number of bits of the prefix -1 (we already set the sign bit)
			for (int i = netPrefix - 1; i > 0; i--) {
				// shift the sign right; Java makes the sign bit sticky on a shift.
				// so no need to "set it back up"...
				shiftby = (shiftby >> 1);
			}
			// transform the resulting value in xxx.xxx.xxx.xxx format, like if it was a standard address.
			String maskString = Integer.toString((shiftby >> 24) & 255) + "." + Integer.toString((shiftby >> 16) & 255) + "." + Integer.toString((shiftby >> 8) & 255) + "." + Integer.toString(shiftby & 255);
			// return the address
			return InetAddress.getByName(maskString);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static boolean verifyIPv4(final String ip) {
		return IPv4RegexPattern.matcher(ip).matches();
	}
}
