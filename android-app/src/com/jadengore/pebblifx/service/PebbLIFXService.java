package com.jadengore.pebblifx.service;

//import com.jadengore.pebblifx.utils.Converters;

import java.util.ArrayList;
import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import lifx.java.android.client.LFXClient;
import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes.LFXPowerState;
import lifx.java.android.light.LFXLight;
import lifx.java.android.network_context.LFXNetworkContext;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class PebbLIFXService extends Service {
	
	private final static UUID PEBBLE_APP_UUID = UUID.fromString("0079C607-A1AF-4308-A743-3C1AFBC7387D");
	private ArrayList<LFXLight> bulbList;
	private LFXNetworkContext localNetworkContext;
	private int transactionId;
	
	
	
	public PebbLIFXService() {
		// Nothing to do here.
	}
	
	public void onCreate() {
		//startService(new Intent(getApplicationContext(),PebbLIFXService.class));
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
		      if (data.getUnsignedInteger(0) == 1 && LFXClient.getSharedInstance(getApplicationContext()).getLocalNetworkContext().isConnected() == false) {
		    	  // do nothing
		      } else {
		    	  receiveMessage(data, transactionId);
		      }
		    }
		});
		boolean connected = PebbleKit.isWatchConnected(getApplicationContext());
		String pebbleStatus = (connected ? "connected" : "not connected");
		Toast.makeText(getApplicationContext(), "PebbLIFXService started, Pebble " + pebbleStatus , Toast.LENGTH_SHORT).show();
		Log.i("PebbleLIFXService", "Pebble is " + (connected ? "connected" : "not connected"));
		// http://stackoverflow.com/questions/15758980/android-service-need-to-run-alwaysnever-pause-or-stop
		return START_STICKY;
	}
	
	//	Helper method for converting signed numbers in Java.
	//private short convertSigned (float value) {
	//	if (value > Short.MAX_VALUE) {
	//		return (short)(value + Integer.MIN_VALUE);
	//	} else {
	//		return (short)value;
	//	}
	//}
	
	public static long getUnsignedInt(float x) {
	    return (int)x & 0x00000000ffffffffL;
	}
	

	public void sendMessage (int type) {
		switch (type) {
		case 0: // No network was found.
			Log.e("PebbLIFXService", "Error Sent: No network found.");
			noNetworkFound();
			break;
		case 1:
			Log.i("PebbLIFXService", "Responding: Bulb List Requested");
			discover();
			break;
		case 2:
			Log.i("PebbLIFXService", "Responding: Change Bulb State Requested");
			//bulbState(); 	// TODO passing values
			break;
		case 3:
			Log.e("PebbLIFXService", "Error Sent: Lost Connection to Bulbs.");
			lostConnection();
			break;
		default:
			Log.e("PebbLIFXService", "Received unexpected value/message for Pebble: " + type);
			break;
		}
	}
	
	
	public void noNetworkFound () {
		if (PebbleKit.areAppMessagesSupported(getApplicationContext())) {
			PebbleDictionary networkFail = new PebbleDictionary();
			networkFail.addUint8(0, (byte) 0);
			networkFail.addUint8(1, (byte) 0); // Filler value
			networkFail.addString(2, "LIFX Network not found.");
			Log.i("Dictionary", networkFail.toJsonString());
			PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, networkFail);
			Log.i("", "Data sent.");
		}
		PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
	}
	
	public void discover() {
		localNetworkContext = LFXClient.getSharedInstance(getApplicationContext()).getLocalNetworkContext();
	    localNetworkContext.connect();
	    bulbList = localNetworkContext.getAllLightsCollection().getLights();
	    Log.i("PebbLIFXService", "Bulb List: " + bulbList.toString());
		if (bulbList == null) {
			Log.e("PebbLIFXService","Nothing returned. No network found.");
			sendMessage(0);
		} else {
			Log.i("PebbLIFXService", "Search has completed.");
			int numberOfBulbs = bulbList.size();
			Log.i("PebbLIFXService", "Bulb List Size:" + numberOfBulbs);
			if (PebbleKit.areAppMessagesSupported(getApplicationContext())) {
				PebbleDictionary bulbData = new PebbleDictionary();
				bulbData.addUint8(0, (byte) 1);
				bulbData.addUint8(1, (byte) numberOfBulbs); // Will only allow 255 bulbs to be passed.
				int j = 2;
				for (int i = 0; i < numberOfBulbs; i++) {
					// First get the bulb name.
					bulbData.addString(j++, bulbList.get(i).getLabel());
					// Find out whether bulbs are on or off.
					LFXPowerState a = bulbList.get(i).getPowerState();
					if (a == LFXPowerState.ON) {
						bulbData.addUint8(j++, (byte)1);
					} else {
						bulbData.addUint8(j++, (byte)0);
					}
					//	TODO Fix brightness and hue getting. Returning nothing.
					bulbData.addUint16(j++, (short)getUnsignedInt(bulbList.get(i).getColor().getBrightness()));
					bulbData.addUint16(j++, (short)getUnsignedInt(bulbList.get(i).getColor().getHue()));
				}
				Log.i("PebbLIFXService", "Dictionary -> " + bulbData.toJsonString());
				Log.i("PebbLIFXService", "Dictionary Size = " + bulbData.size());
				PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, bulbData);
				Log.i("PebbLIFXService", "Data sent.");
			}
		}
	}
	
	public void bulbState (int target, int state, int brightness, int color) { //brightness and color are uint16
		if (PebbleKit.areAppMessagesSupported(getApplicationContext())) {
			PebbleDictionary bulbState = new PebbleDictionary();
			bulbState.addUint8(0, (byte) target); // Target
			bulbState.addUint8(1, (byte) state); // State
			bulbState.addUint16(2, (byte) brightness); // Brightness
			bulbState.addUint16(3, (byte) color); // Color
			Log.i("Dictionary", bulbState.toJsonString());
			PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, bulbState);
			Log.i("PebbLIFXService", "Data sent.");
		}
	}

	public void lostConnection () {
		if (PebbleKit.areAppMessagesSupported(getApplicationContext())) {
			PebbleDictionary lostConnection = new PebbleDictionary();
			lostConnection.addUint8(0, (byte) 0);
			lostConnection.addUint8(1, (byte) 0); // Filler value
			lostConnection.addString(2, "Network Connection Lost."); // See if this is valid in C.
			Log.i("Dictionary", lostConnection.toJsonString());
			PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, lostConnection);
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
	    case 1: //Pebble app close.
	    	localNetworkContext.disconnect();
	    	Log.i("PebbLIFXService", "Network closed.");
	    	break;
	    case 2: // Turns bulbs on or off.
	    	Log.i("PebbLIFXService", "On/off command received.");
	    	onOff(dictionary.getUnsignedInteger(1).intValue(), dictionary.getUnsignedInteger(2).intValue());
	    	break;
	    case 3: // Adjusts brightness of bulbs.
	    	Log.i("PebbLIFXService", "Brightness adjustment command received.");
	    	//brightness(dictionary.getUnsignedInteger(1).intValue(), convertSigned(dictionary.getUnsignedInteger(2).intValue()));
	    	break;
	    case 4: // Adjusts color of bulbs.
	    	Log.i("PebbLIFXService", "Color adjustment command received.");
	    	//color(dictionary.getUnsignedInteger(1).intValue(), convertSigned(dictionary.getUnsignedInteger(2).intValue()));
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
				localNetworkContext.getAllLightsCollection().setPowerState(LFXPowerState.OFF);
			} else {
				bulbList.get(target-1).setPowerState(LFXPowerState.OFF);
			}	
		} else {
			if (target == 0) {
				localNetworkContext.getAllLightsCollection().setPowerState( LFXPowerState.ON);
			} else {
				bulbList.get(target-1).setPowerState(LFXPowerState.ON);
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
			} */
		} else {
			//bulbList.get(target - 1).setBrightness(null);
		}	
		ack();
	}
	
	//public void color (int target, short color) {
	public void color (int target, int r, int g, int b) {
		if (target == 0) {
			// TODO HSBK COLOR
			LFXHSBKColor color = localNetworkContext.getAllLightsCollection().getColor();
			//int kelvin = color.getKelvin();
			float[] currentHSB = new float[3];
			currentHSB[0] = color.getHue();
			currentHSB[1] = color.getSaturation();
			currentHSB[2] = color.getBrightness();
			Log.i("PebbLIFXService", "HSBKColor" + color.toString());
			//float[] colorArray = Converters.RGBtoHSB(r,g,b,currentHSB);
			//LFXHSBKColor result = new LFXHSBKColor(currentHSB[0],currentHSB[1],currentHSB[2],kelvin)
			localNetworkContext.getAllLightsCollection().setColor(color);
		} else {
			bulbList.get(target - 1).setColor(null);
		}
		ack();
	}
	
	public void bulbStatus(int target) {
		// This will check that individual bulbs are being targeted.
		if (target > 0) {
			bulbList.get(target - 1).getPowerState();
		} else {
			Log.e("Error: ", "Can only get status for individual bulb.");
		}
		ack();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(getApplicationContext(), "PebbLIFXService stopped.", Toast.LENGTH_SHORT).show();
	}

}
