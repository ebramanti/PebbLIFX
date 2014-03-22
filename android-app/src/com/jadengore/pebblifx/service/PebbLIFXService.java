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
	
	protected final static UUID PEBBLE_APP_UUID = UUID.fromString("0079c607-a1af-4308-a743-3c1afbc7387d");
	private List<Bulb> bulbList;
	private BulbNetwork net;
	private int transactionId;
	
	
	public PebbLIFXService() {
		// TODO
	}
	
	private void ack() {
		PebbleKit.sendAckToPebble(getApplicationContext(), this.transactionId);
	}
	
	public int onStart(Intent intent, int flags, int startId) {
		PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
		    @Override
		    public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
		      Log.i("PebbLIFXService", "Received value=" + data.getUnsignedInteger(0) + " for key: 0");
		      receiveMessage(data, transactionId);
		    }
		});
		return Service.START_NOT_STICKY;
	}
	
	//	Helper method for converting signed numbers in Java.
	private short convertSigned (int value) {
		if (value > Short.MAX_VALUE) {
			return (short)(value + Integer.MIN_VALUE);
		} else {
			return (short)value;
		}
	}
	
	public void receiveMessage (PebbleDictionary dictionary, int transactionId) {
	    this.transactionId = transactionId; // make sure transactionId is set before calling (onStart)
	    int type = dictionary.getInteger(0).intValue();
	    switch (type) {
	    case 0:
	    	discover();
	    	break;
	    case 1:
	    	onOff(dictionary.getInteger(1).intValue(), dictionary.getInteger(2).intValue());
	    	break;
	    case 2:
	    	brightness(dictionary.getInteger(1).intValue(), convertSigned(dictionary.getInteger(2).intValue()));
	    	break;
	    case 3:
	    	color(dictionary.getInteger(1).intValue(), convertSigned(dictionary.getInteger(2).intValue()));
	    	break;
	    default:
	    	Log.e("PebbLIFXService", "Received unexpected value/message from Pebble: " + type);
	    	break;
	    }
	}
	
	public void discover() {
		Discoverer d = new Discoverer(getApplicationContext());
		d.startSearch();
		while (d.getBulbNetwork() == null) {
			continue; //TODO there's a better way
		}
		net = d.getBulbNetwork();
		bulbList = net.getBulbList();
		d.stopSearch();
		int numberOfBulbs = bulbList.size();
		PebbleDictionary bulbData = new PebbleDictionary();
		bulbData.addUint8(0, (byte) numberOfBulbs); // Will only allow 255 bulbs to be passed.
		for (int i = 1; i < numberOfBulbs + 1; i++) {
			bulbData.addString(i, bulbList.get(i - 1).toString());
		}
		PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, bulbData);
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
		ack();
	}
	
	public void color (int target, short color) {
		// TODO COLOR
		ack();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
