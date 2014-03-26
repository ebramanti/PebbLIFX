package com.jadengore.pebblifx;

//import java.io.IOException;

import com.getpebble.android.kit.PebbleKit;
import com.jadengore.pebblifx.service.PebbLIFXService;

import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (serviceRunning()) {
			stopService(new Intent(getApplicationContext(), PebbLIFXService.class));
			Toast.makeText(getApplicationContext(), "PebbLIFXService stopped.", Toast.LENGTH_SHORT).show();
		} else {
			Intent bindIntent = new Intent(getApplicationContext(), PebbLIFXService.class);
			startService(bindIntent);
			boolean connected = PebbleKit.isWatchConnected(getApplicationContext());
			String pebbleStatus = (connected ? "connected" : "not connected");
			Toast.makeText(getApplicationContext(), "PebbLIFXService started, Pebble " + pebbleStatus , Toast.LENGTH_SHORT).show();
			Log.i(getLocalClassName(), "Pebble is " + (connected ? "connected" : "not connected"));
		}
		
		super.onCreate(savedInstanceState);
		finish();
	}
	
	private boolean serviceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (PebbLIFXService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

}
