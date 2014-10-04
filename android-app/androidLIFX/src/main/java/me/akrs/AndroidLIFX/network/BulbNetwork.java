package me.akrs.AndroidLIFX.network;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import me.akrs.AndroidLIFX.packets.request.OnOffRequest;
import me.akrs.AndroidLIFX.packets.request.SetStateRequest;
import me.akrs.AndroidLIFX.utils.BulbStatus;
import me.akrs.AndroidLIFX.utils.MacAddress;
import me.akrs.AndroidLIFX.utils.java.Logger;

public class BulbNetwork implements Closeable {

	protected InetAddress gatewayAddress;
	protected MacAddress gatewayMac;
	private List<Bulb> bulbList = Collections.synchronizedList(new ArrayList<Bulb>());

	private Socket gatewaySocket;
	protected DataOutputStream gatewayOutStream;
	private DataInputStream gatewayInStream;

	private GatewayInputThread gatewayThread;

	private Timer statusRequesterTimer;
	private StatusRequester statusRequester;

	public BulbNetwork ()  {
		
	}

	public void addBulb (MacAddress mac, MacAddress gatewayMac, InetAddress ip) {
		if (!bulbExist(mac)) {
			if (gatewayAddress == null) {
				this.gatewayAddress = ip;
				this.gatewayMac = gatewayMac;
				this.initiateConnection();
			}
			this.bulbList.add(new Bulb(mac, this));
		}
	}

	private void initiateConnection () {
		try {
			this.gatewaySocket = new Socket(gatewayAddress, 56700);
			gatewayOutStream = new DataOutputStream(gatewaySocket.getOutputStream());
			gatewayInStream = new DataInputStream(gatewaySocket.getInputStream());

			//New thread for listening of the TCP connection
			gatewayThread = new GatewayInputThread(gatewayInStream, this);
			gatewayThread.start();

			//Send status request with fixed interval
			statusRequesterTimer = new Timer();
			statusRequester = new StatusRequester(this);
			statusRequesterTimer.schedule(statusRequester, 0, 1000);

		} catch (IOException e) {
			Logger.log("Failed to initiate connection", e);
		}
	}

	public Iterator<Bulb> getBulbIterator () {
		return bulbList.iterator();
	}

	public boolean bulbExist (MacAddress mac) {
		Iterator<Bulb> itr = bulbList.iterator();

		while(itr.hasNext()){
			if(itr.next().getMacAddress().equal(mac)){
				return true;
			}
		}

		return false;
	}

	public int getNumberOfBulbs () {
		return bulbList.size();
	}

	public Bulb getBulbById (int id) {
		return bulbList.get(id);
	}

	public Bulb getBulb (MacAddress mac) {
		Iterator<Bulb> itr = bulbList.iterator();
		while(itr.hasNext()){
			Bulb bulb = itr.next();
			if(bulb.getMacAddress().equal(mac)){
				return bulb;
			}
		}
		return null;
	}

	public List<Bulb> getBulbList () {
		return bulbList;
	}
	
	public void on () throws IOException {
		if(gatewayAddress != null){
			gatewayOutStream.write((new OnOffRequest(null, gatewayMac, BulbStatus.ON)).getBytes());
		}
	}
	
	public void off () throws IOException {
		if(gatewayAddress != null){
			gatewayOutStream.write((new OnOffRequest(null, gatewayMac, BulbStatus.OFF)).getBytes());
		}
	}


	// Commands
	// Set bulb state
	public void setState (short hue, short saturation, short luminance, short whiteColor, short fadeTime) throws IOException{
		if(gatewayAddress != null){
			gatewayOutStream.write((new SetStateRequest(null, gatewayMac, hue, saturation, luminance, whiteColor, fadeTime)).getBytes());
		}
	}

	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append("LIFXNetwork:\nGateway IP: ");
		sb.append(gatewayAddress);
		sb.append("\nGateway MAC: ");
		sb.append(gatewayMac);
		sb.append("\n\nBulbs:\n");

		Iterator<Bulb> itr = this.getBulbIterator();
		int i = 0;
		while(itr.hasNext()){
			sb.append(i++);
			sb.append(") ");
			sb.append(itr.next());
			sb.append("\n");
		}

		return sb.toString();
	}

	public void close() throws IOException {
		gatewaySocket.close();
		gatewayThread.cease();
	}

	public void reconnect() {
		try {
			gatewaySocket.close();
			gatewayThread.cease();
			statusRequesterTimer.cancel();
			
			this.gatewaySocket = new Socket(gatewayAddress, 56700);
			gatewayOutStream = new DataOutputStream(gatewaySocket.getOutputStream());
			gatewayInStream = new DataInputStream(gatewaySocket.getInputStream());

			//New thread for listening of the TCP connection
			gatewayThread = new GatewayInputThread(gatewayInStream, this);
			gatewayThread.start();

			//Send status request with fixed interval
			statusRequesterTimer = new Timer();
			statusRequester = new StatusRequester(this);
			statusRequesterTimer.schedule(statusRequester, 0, 1000);
		} catch (IOException e) {
			Logger.log("unable to reconnect", e);
		}

		
		
	}


}
