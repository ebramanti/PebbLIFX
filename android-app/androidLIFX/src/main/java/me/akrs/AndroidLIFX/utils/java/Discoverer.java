package me.akrs.AndroidLIFX.utils.java;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import me.akrs.AndroidLIFX.network.BulbNetwork;
import me.akrs.AndroidLIFX.packets.request.DiscoveryRequest;
import me.akrs.AndroidLIFX.packets.responses.StandardResponse;

public class Discoverer {
	private FinderThread finder;
	private BulbNetwork network;

	public Discoverer (InetAddress broadcast) {
		try {
			this.finder = new FinderThread(broadcast);
		} catch (IOException e) {
			Logger.log("Unable to create FinderThread", e);
		}
		
		this.network = new BulbNetwork();
	}

	public void startSearch () {
		this.finder.start();
	}

	public void stopSearch () {
		this.finder.stopDiscovery();
	}
	
	public BulbNetwork getNetwork () {
		return this.network;
    }
	private class FinderThread extends Thread {
		private boolean discovering;
		private InetAddress broadcast;

		public FinderThread (InetAddress broadcast) throws IOException {
			this.broadcast = broadcast;
		}

		public void run () {
			Logger.log("Attempting discovery, address: " + this.broadcast.toString(), Logger.DEBUG);

			DatagramSocket serverSocket = null;
			try {
				serverSocket = new DatagramSocket(56700);
			} catch (SocketException e) {
				Logger.log("Failed to open discovery socket", e);
			}

			try {
				serverSocket.setBroadcast(true);
				Logger.log("Set broadcast to true.", Logger.DEBUG);
			} catch (SocketException e) {
				Logger.log("Failed to set broadcast", e);
			}
			
			DiscoveryRequest req = new DiscoveryRequest();
			DatagramPacket pack = new DatagramPacket(req.getBytes(), req.getBytes().length, broadcast, 56700);
			try {
				serverSocket.send(pack);
				Logger.log("Sent discovery request", Logger.DEBUG);
			} catch (IOException e1) {
				Logger.log("Failed to send broadcast", e1);
			}


			byte[] receiveData = new byte[0xFF];
			byte[] data;

			this.discovering = true;
			while (this.discovering) {
				DatagramPacket receivePacket =
						new DatagramPacket(receiveData, receiveData.length);
				try {
					serverSocket.receive(receivePacket);
				} catch (IOException e) {
					Logger.log("Failed to recieve discovery response", e);
				}

				data = receivePacket.getData();

				InetAddress ip = receivePacket.getAddress();

				StandardResponse res = new StandardResponse(data);

				switch(res.getPacketType()){
				case DISCOVER_RESPONSE:
					network.addBulb(res.getTargetBulb(),res.getGatewayBulb(),ip);
					Logger.log("Got Response", Logger.DEBUG);
					break;
				default:
					break;
				}
			}
			
			serverSocket.close();
		}

		public void stopDiscovery () {
			this.discovering = false;
		}

		@SuppressWarnings("unused")
		public boolean isRunning () {
			return this.discovering;
		}
	}
}
