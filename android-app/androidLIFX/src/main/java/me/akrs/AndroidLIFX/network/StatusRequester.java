package me.akrs.AndroidLIFX.network;

import java.io.IOException;
import java.util.TimerTask;

import me.akrs.AndroidLIFX.packets.request.StatusRequest;
import me.akrs.AndroidLIFX.utils.java.Logger;

class StatusRequester extends TimerTask {
	BulbNetwork net;

	public StatusRequester (BulbNetwork net) {
		this.net = net;
	}

	@Override
	public void run() {
    	if(net.gatewayAddress != null){
    		try {
				net.gatewayOutStream.write((new StatusRequest(net.gatewayMac)).getBytes());
//				Logger.log("Sent status request", Logger.DEBUG);
			} catch (IOException e) {
				Logger.log("Unable to send perodic status request", e);
			}
		}
	}
}