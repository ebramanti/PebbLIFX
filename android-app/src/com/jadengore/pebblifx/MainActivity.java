package com.jadengore.pebblifx;

//import java.io.IOException;

import com.getpebble.android.kit.PebbleKit;
import com.jadengore.pebblifx.service.pebblifxservice;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
//import me.akrs.AndroidLIFX.network.BulbNetwork;
import me.akrs.AndroidLIFX.utils.android.Discoverer;
//import me.akrs.AndroidLIFX.utils.android.Logger;

public class MainActivity extends Activity {
	
	Discoverer d;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*d = new Discoverer(getApplicationContext());
		d.startSearch();
		BulbNetwork net = d.getBulbNetwork();
		//	This is bad. But necessary for now.
		while (d.getBulbNetwork().getNumberOfBulbs() != 2) {
			continue;
		}
		Logger.log("We got a network." + net.toString(), Logger.DEBUG);
		d.stopSearch();
		Logger.log("Search Complete.", Logger.DEBUG);
		try {
			net.off();
		} catch (IOException e) {
			Logger.log("Off command unsuccessful.", e);
		}
		try {
			net.on();
		} catch (IOException e) {
			Logger.log("Off command unsuccessful.", e);
		} */
		
		//Intent i = new Intent();
		
		startService(new Intent(getApplicationContext(), pebblifxservice.class));
		
		
		//PebbLIFXService.onStart(i);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		boolean connected = PebbleKit.isWatchConnected(getApplicationContext());
		Log.i(getLocalClassName(), "Pebble is " + (connected ? "connected" : "not connected"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
