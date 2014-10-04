package com.jadengore.pebblifx;

import com.jadengore.pebblifx.service.PebbLIFXService;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (serviceRunning()) {
			stopService(new Intent(getApplicationContext(), PebbLIFXService.class));
		} else {
			Intent bindIntent = new Intent(getApplicationContext(), PebbLIFXService.class);
			startService(bindIntent);
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
