package com.jadengore.pebblifx.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PebbLIFXBroadcastReceiver extends BroadcastReceiver {

	public PebbLIFXBroadcastReceiver() {
		// TODO Auto-generated constructor stub
	}
	
	public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, PebbLIFXService.class);
        context.startService(startServiceIntent);
    }

}
