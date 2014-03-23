package com.jadengore.pebblifx.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import me.akrs.AndroidLIFX.network.Bulb;
import me.akrs.AndroidLIFX.network.BulbNetwork;
import me.akrs.AndroidLIFX.utils.android.Discoverer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PebbLIFXService extends Service {
	
	private final static UUID PEBBLE_APP_UUID = UUID.fromString("0079C607-A1AF-4308-A743-3C1AFBC7387D");
	private List<Bulb> bulbList;
	private BulbNetwork net;
	private int transactionId;
	
	
	public PebbLIFXService() {
		// F.
	}
	
	public void onCreate() {
		super.onCreate();
		Log.d(getPackageName(), "Created PebbLIFX Service");
	}
	
	private void ack() {
		PebbleKit.sendAckToPebble(getApplicationContext(), this.transactionId);
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
		    @Override
		    public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
		      Log.i("PebbLIFXService", "Received value = " + data.getUnsignedInteger(0) + " for key: 0");
		      receiveMessage(data, transactionId);
		    }
		});
		return Service.START_STICKY;
	}
	
	//	Helper method for converting signed numbers in Java.
	private short convertSigned (int value) {
		if (value > Short.MAX_VALUE) {
			return (short)(value + Integer.MIN_VALUE);
		} else {
			return (short)value;
		}
	}
	
	public void sendMessage (int type) {
		switch (type) {
		case 0: // No network was found.
			noNetworkFound();
			Log.e("Error Sent: ", "No network found.");
			break;
		case 1:
			bulbList();
			Log.i("Responding: ", "Bulb List Requested");
			break;
		case 2:
			bulbState();
			Log.i("Responding: ", "Change Bulb State Requested");
			break;
		case 3:
			lostConnection();
			Log.e("Error Sent: ", "Lost Connection to Bulbs.");
			break;
		default:
			Log.e("PebbLIFXService", "Received unexpected value/message for Pebble: " + type);
			break;
		}
	}
	
	
	public void noNetworkFound () {
		//TODO no network found
	}
	
	public void bulbList () {
		//TODO bulbList (call in discovery)
	}
	
	public void bulbState () {
		//TODO bulbState
	}

	public void lostConnection () {
		//TODO lost connection
	}
	
	public void receiveMessage (PebbleDictionary dictionary, int transactionId) {
	    this.transactionId = transactionId; // make sure transactionId is set before calling (onStart)
	    int type = dictionary.getUnsignedInteger(0).intValue();
	    switch (type) {
	    case 0: // Discover bulbs.
	    	discover();
	    	Log.i("", "Discovery gg.");
	    	break;
	    case 1: // Turns bulbs on or off.
	    	onOff(dictionary.getUnsignedInteger(1).intValue(), dictionary.getUnsignedInteger(2).intValue());
	    	break;
	    case 2: // Adjusts brightness of bulbs.
	    	brightness(dictionary.getUnsignedInteger(1).intValue(), convertSigned(dictionary.getUnsignedInteger(2).intValue()));
	    	break;
	    case 3: // Adjusts color of bulbs.
	    	color(dictionary.getUnsignedInteger(1).intValue(), convertSigned(dictionary.getUnsignedInteger(2).intValue()));
	    	break;
	    default:
	    	Log.e("PebbLIFXService", "Received unexpected value/message from Pebble: " + type);
	    	break;
	    }
	}
	
	//@SuppressWarnings("static-access")
	public void discover() {
		Discoverer d = new Discoverer(getApplicationContext());
		d.startSearch();
		d.sleepThread(2500); // best way to wait for bulbs
		if (d.getBulbNetwork() == null) {
			Log.e("Nothing returned. ","No network found.");
			d.stopSearch();
			sendMessage(0);
		} else {
			net = d.getBulbNetwork();
			bulbList = net.getBulbList();
			d.stopSearch();
			Log.i("", "Search has completed.");
			int numberOfBulbs = bulbList.size();
			if (PebbleKit.areAppMessagesSupported(getApplicationContext())) {
				PebbleDictionary bulbData = new PebbleDictionary();
				bulbData.addUint8(0, (byte) 1);
				bulbData.addUint8(1, (byte) numberOfBulbs); // Will only allow 255 bulbs to be passed.
				for (int i = 2; i < numberOfBulbs + 2; i++) {
					bulbData.addString(i, bulbList.get(i - 2).getName());
				}
				Log.i("Dictionary", bulbData.toJsonString());
				PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, bulbData);
				Log.i("", "Data sent.");
			}
		}
	}
	
	public void onOff(int target, int state) {
		if (state == 0) {
			if (target == 0) {
				try {
					net.off();
				} catch (IOException e) {
					Log.e("PebbLIFXService", "Unable to turn off all bulbs.", e);
				}
			} else {
				try {
					bulbList.get(target).off();
				} catch (IOException e) {
					Log.e("PebbLIFXService", "Unable to turn off bulb " + target, e);
				}
			}	
		} else {
			if (target == 0) {
				try {
					net.on();
				} catch (IOException e) {
					Log.e("PebbLIFXService", "Unable to turn on all bulbs.", e);
				}
			} else {
				try {
					bulbList.get(target).on();
				} catch (IOException e) {
					Log.e("PebbLIFXService", "Unable to turn on bulb " + target, e);
				}
			}	
		}
		ack();
	}
	
	public void brightness (int target, short level) {
		// TODO BRIGHTNESS
		if (target == 0) {
			
		} else {
			
		}
		ack();
	}
	
	public void color (int target, short color) {
		// TODO COLOR
		if (target == 0) {
			
		} else {
			
		}
		ack();
	}
	

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
