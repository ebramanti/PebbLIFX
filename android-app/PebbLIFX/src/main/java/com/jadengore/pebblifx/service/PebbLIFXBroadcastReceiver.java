package com.jadengore.pebblifx.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PebbLIFXBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("PebbLIFXBroadcastService", "Boot complete, PebbLIFXService started.");
        context.startService(new Intent(context, PebbLIFXService.class));
    }
}