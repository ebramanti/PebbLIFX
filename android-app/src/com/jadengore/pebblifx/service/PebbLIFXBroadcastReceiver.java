package com.jadengore.pebblifx.service;

/*import java.io.IOException;
import java.util.List;
import java.util.UUID;

import me.akrs.AndroidLIFX.network.Bulb;
import me.akrs.AndroidLIFX.network.BulbNetwork;
import me.akrs.AndroidLIFX.utils.BulbStatus;
import me.akrs.AndroidLIFX.utils.android.Discoverer;

import org.json.JSONException;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PebbLIFXBroadcastReceiver extends PebbleDataReceiver {
	private final static UUID PEBBLE_APP_UUID = UUID.fromString("0079C607-A1AF-4308-A743-3C1AFBC7387D");
	private List<Bulb> bulbList;
	private BulbNetwork net;
	private int transactionId;
	private Context applicationContext;
	
	public PebbLIFXBroadcastReceiver() {
		super(PEBBLE_APP_UUID);
	}
	
	private void ack() {
		PebbleKit.sendAckToPebble(applicationContext, this.transactionId);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		this.applicationContext = context;
		if (intent.getAction().equals(Constants.INTENT_APP_RECEIVE)) {
	        // Pebble-enabled apps are expected to be good citizens and only inspect broadcasts containing their UUID
	        if (!PEBBLE_APP_UUID.equals(PEBBLE_APP_UUID)) {
	            Log.e("PebbLIFXBroadcastReciver", "Not my UUID");
	            return;
	        }
	            
	        final int transactionId = intent.getIntExtra(Constants.TRANSACTION_ID, -1);
	        final String jsonData = intent.getStringExtra(Constants.MSG_DATA);
	        if (jsonData == null || jsonData.isEmpty()) {
	            Log.e("PebbLIFXBroadcastReciver", "JSONData returns null.");
	            return;
	        }
	            
	        try {
	            final PebbleDictionary data = PebbleDictionary.fromJson(jsonData);
	            receiveData(applicationContext, transactionId, data);
	            // do what you need with the data
	            PebbleKit.sendAckToPebble(context, transactionId);
	            Log.i("DataSniffer", "Data received.");
	        } catch (JSONException e) {
	            Log.e("PebbLIFXBroadcastReciver", "failed reived -> dict" + e);
	            return;
	        }
	    }
		
	}

	@Override
	public void receiveData(Context context, int transactionId, PebbleDictionary data) {
	      Log.i("PebbLIFXService", "Received value = " + data.getUnsignedInteger(0) + " for key: 0");
	      receiveMessage(data, transactionId);
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
			Log.e("Error Sent: ", "No network found.");
			noNetworkFound();
			break;
		case 1:
			Log.i("Responding: ", "Bulb List Requested");
			discover();
			break;
		case 2:
			Log.i("Responding: ", "Change Bulb State Requested");
			//bulbState(); 	// TODO passing values
			break;
		case 3:
			Log.e("Error Sent: ", "Lost Connection to Bulbs.");
			lostConnection();
			break;
		default:
			Log.e("PebbLIFXService", "Received unexpected value/message for Pebble: " + type);
			break;
		}
	}
	
	
	public void noNetworkFound () {
		if (PebbleKit.areAppMessagesSupported(applicationContext)) {
			PebbleDictionary networkFail = new PebbleDictionary();
			networkFail.addUint8(0, (byte) 0);
			networkFail.addUint8(1, (byte) 0); // Filler value
			networkFail.addString(2, "LIFX Network not found.");
			Log.i("Dictionary", networkFail.toJsonString());
			PebbleKit.sendDataToPebble(applicationContext, PEBBLE_APP_UUID, networkFail);
			Log.i("", "Data sent.");
		}
		PebbleKit.closeAppOnPebble(applicationContext, PEBBLE_APP_UUID);
	}
	
	//@SuppressWarnings("static-access")
	public void discover() {
		Discoverer d = new Discoverer(applicationContext);
		d.startSearch();
		d.sleepThread(2500); // best way to wait for bulbs
		if (d.getBulbNetwork() == null) {
			Log.e("PebbLIFXService","Nothing returned. No network found.");
			d.stopSearch();
			sendMessage(0);
		} else {
			net = d.getBulbNetwork();
			bulbList = net.getBulbList();
			d.stopSearch();
			Log.i("PebbLIFXService", "Search has completed.");
			int numberOfBulbs = bulbList.size();
			if (PebbleKit.areAppMessagesSupported(applicationContext)) {
				PebbleDictionary bulbData = new PebbleDictionary();
				bulbData.addUint8(0, (byte) 1);
				bulbData.addUint8(1, (byte) numberOfBulbs); // Will only allow 255 bulbs to be passed.
				int j = 2;
				for (int i = 0; i < numberOfBulbs; i++) {
					// First get the bulb name.
					bulbData.addString(j++, bulbList.get(i).getName());
					// Find out whether bulbs are on or off.
					BulbStatus a = bulbList.get(i).getStatus();
					if (a == BulbStatus.ON) {
						bulbData.addUint8(j++, (byte)1);
					} else {
						bulbData.addUint8(j++, (byte)0);
					}
					bulbData.addUint16(j++, bulbList.get(i).getLuminance());
					bulbData.addUint16(j++, bulbList.get(i).getHue());
				}
				Log.i("PebbLIFXService", "Dictionary -> " + bulbData.toJsonString());
				PebbleKit.sendDataToPebble(applicationContext, PEBBLE_APP_UUID, bulbData);
				Log.i("PebbLIFXService", "Data sent.");
			}
		}
	}
	
	public void bulbState (int target, int state, int brightness, int color) { //brightness and color are uint16
		if (PebbleKit.areAppMessagesSupported(applicationContext)) {
			PebbleDictionary bulbState = new PebbleDictionary();
			bulbState.addUint8(0, (byte) target); // Target
			bulbState.addUint8(1, (byte) state); // State
			bulbState.addUint16(2, (byte) brightness); // Brightness
			bulbState.addUint16(3, (byte) color); // Color
			Log.i("Dictionary", bulbState.toJsonString());
			PebbleKit.sendDataToPebble(applicationContext, PEBBLE_APP_UUID, bulbState);
			Log.i("PebbLIFXService", "Data sent.");
		}
	}

	public void lostConnection () {
		if (PebbleKit.areAppMessagesSupported(applicationContext)) {
			PebbleDictionary lostConnection = new PebbleDictionary();
			lostConnection.addUint8(0, (byte) 0);
			lostConnection.addUint8(1, (byte) 0); // Filler value
			lostConnection.addString(2, "Network Connection \nLost."); // See if this is valid in C.
			Log.i("Dictionary", lostConnection.toJsonString());
			PebbleKit.sendDataToPebble(applicationContext, PEBBLE_APP_UUID, lostConnection);
			Log.i("PebbLIFXService", "Data sent.");
		}
	}
	
	public void receiveMessage (PebbleDictionary dictionary, int transactionId) {
	    this.transactionId = transactionId; // make sure transactionId is set before calling (onStart)
	    int type = dictionary.getUnsignedInteger(0).intValue();
	    switch (type) {
	    case 0: // Discover bulbs.
	    	discover();
	    	Log.i("PebbLIFXService", "Discovery complete.");
	    	break;
	    case 1: //Pebble app on phone close.
	    	try {
				if (net != null) {
					net.close();
					Log.i("PebbLIFXService", "Network closed.");
				}
			} catch (IOException e) {
				Log.e("PebbLIFXService", "Problem detected on net.close()");
			}
	    	break;
	    case 2: // Turns bulbs on or off.
	    	Log.i("PebbLIFXService", "On/off command received.");
	    	onOff(dictionary.getUnsignedInteger(1).intValue(), dictionary.getUnsignedInteger(2).intValue());
	    	break;
	    case 3: // Adjusts brightness of bulbs.
	    	Log.i("PebbLIFXService", "Brightness adjustment command received.");
	    	brightness(dictionary.getUnsignedInteger(1).intValue(), convertSigned(dictionary.getUnsignedInteger(2).intValue()));
	    	break;
	    case 4: // Adjusts color of bulbs.
	    	Log.i("PebbLIFXService", "Color adjustment command received.");
	    	color(dictionary.getUnsignedInteger(1).intValue(), convertSigned(dictionary.getUnsignedInteger(2).intValue()));
	    	break;
	    case 5: // Get bulb status. INDIVIDUAL BULBS ONLY.
	    	Log.i("PebbLIFXService", "Bulb status command received.");
	    	bulbStatus(dictionary.getUnsignedInteger(1).intValue());
	    	break;
	    default:
	    	Log.e("PebbLIFXService", "Received unexpected value/message from Pebble: " + type);
	    	break;
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
					bulbList.get(target-1).off();
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
					bulbList.get(target-1).on();
				} catch (IOException e) {
					Log.e("PebbLIFXService", "Unable to turn on bulb " + target, e);
				}
			}	
		}
		ack();
	}
	
	public void brightness (int target, short level) {
		// TODO BRIGHTNESS for all
		if (target == 0) {
			/*try {
				//net.brightness(level); or setState somehow
			} catch (IOException e) {
				Log.e("PebbLIFXService", "Unable to set brightness for all bulbs.", e);
			}
		} else {
			try {
				bulbList.get(target).setBrightness(level);
			} catch (IOException e) {
				Log.e("PebbLIFXService", "Unable to set brightness for bulb " + target, e);
			}
		}	
		ack();
	}
	
	public void color (int target, short color) {
		// TODO COLOR for all
		if (target == 0) {
			/*try {
				//net.color(color); or setState somehow
			} catch (IOException e) {
				Log.e("PebbLIFXService", "Unable to set color for all bulbs.", e);
			} 
		} else {
			try {
				bulbList.get(target).setHue(color);
			} catch (IOException e) {
				Log.e("PebbLIFXService", "Unable to set color for bulb " + target, e);
			}
		}
		ack();
	}
	
	public void bulbStatus(int target) {
		// This will check that individual bulbs are being targeted.
		if (target > 0) {
			bulbList.get(target).getStatus();
		} else {
			Log.e("Error: ", "Can only get status for individual bulb.");
		}
		ack();
	}


} */