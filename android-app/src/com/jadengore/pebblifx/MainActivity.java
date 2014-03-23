package com.jadengore.pebblifx;

//import java.io.IOException;

import com.getpebble.android.kit.PebbleKit;
import com.jadengore.pebblifx.service.PebbLIFXService;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		startService(new Intent(getApplicationContext(), PebbLIFXService.class));
		
		
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
