package com.jadengore.pebblifx.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class PebbLIFXWifiReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            boolean connectedToWifi = info.isConnected();

            if (connectedToWifi) {
                Log.i("PebbLIFXWifiReceiver", "Wifi Connected, PebbLIFXService started.");
                context.startService(new Intent(context, PebbLIFXService.class));
            } else {
                Log.i("PebbLIFXWifiReceiver", "Wifi DC'd, PebbLIFXService stopped.");
                context.stopService(new Intent(context, PebbLIFXService.class));
            }
        }
    }
}
