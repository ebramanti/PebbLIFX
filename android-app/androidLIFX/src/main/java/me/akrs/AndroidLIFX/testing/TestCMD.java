package me.akrs.AndroidLIFX.testing;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import me.akrs.AndroidLIFX.network.Bulb;
import me.akrs.AndroidLIFX.network.BulbNetwork;
import me.akrs.AndroidLIFX.utils.java.Discoverer;
import me.akrs.AndroidLIFX.utils.java.Logger;

public class TestCMD {
	public static void main (String[] args) throws IOException {
		InetAddress broadcast = findBroadcastAddress();
		
		Discoverer d = new Discoverer(broadcast);
		
		d.startSearch();
		
		BulbNetwork net = d.getNetwork();
		
		while (d.getNetwork().getNumberOfBulbs() != 2) {
			continue;
		}
		
		System.out.print("We got a network.\n" + net.toString() + "\nChoose a bulb, or 0 for network:\n");
		d.stopSearch();
		
		int i1 = 0;
		List<Bulb> bulbs = net.getBulbList();
		for (Bulb b : bulbs) {
			System.out.println(++i1 +") " + b.toString());
		}
		
		Scanner s = new Scanner(System.in);
		int j = 0;
		int index = s.nextInt() - 1;
		if (index == -1) {
			boolean bob = true;
			while (bob) {
				System.out.println("Choose a command:\n1) Off\n2) On\n3) Hue");
				
				j = s.nextInt();
				switch (j) {
				case 0:
					bob = false;
				case 1:
					net.off();
					break;
				case 2:
					net.on();
					break;
				default:
					bob = false;
					break;
				}
			}
		} else {
			Bulb b = net.getBulbById(index);
			boolean bob = true;
			while (bob) {
				System.out.println("Choose a command:\n1) Off\n2) On\n3) Lum\n4) Hue\n5)Brightness");
				
				j = s.nextInt();
				switch (j) {
				case 0:
					bob = false;
				case 1:
					b.off();
					break;
				case 2:
					b.on();
					break;
				case 3:
					System.out.print("Enter a lum: ");
					b.setLuminance(s.nextShort());
					break;
				case 4:
					System.out.println("Enter a hue: ");
					b.setHue(s.nextShort());
					break;
				case 5:
					System.out.println("Enter a brightness: ");
					b.setBrightness(s.nextShort());
					break;
				default:
					break;
				}
			}
		}
		s.close();
		net.close();
		return;
	}

	private static InetAddress findBroadcastAddress() {
		try {
			ArrayList<NetworkInterface> ifaces = Collections.list(NetworkInterface.getNetworkInterfaces());
	
			for (NetworkInterface i : ifaces) {
				if (!i.isLoopback()) {
					List<InterfaceAddress> addresses = i.getInterfaceAddresses();
					for (InterfaceAddress a : addresses) {
						if (a.getAddress() instanceof Inet4Address) {
							return a.getBroadcast();
						}
					}
				}
			}
		} catch (SocketException e) {
			Logger.log("unable to do network stuff.", e);
		}
		
		return null;
	}
}
