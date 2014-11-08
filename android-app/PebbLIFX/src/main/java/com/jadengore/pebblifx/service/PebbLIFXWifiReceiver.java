package com.jadengore.pebblifx.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class PebbLIFXWifiReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean connectedToWifi = mWifi.isConnected();

        if (connectedToWifi) {
            Log.i("PebbLIFXWifiReceiver", "Wifi State Changed, PebbLIFXService refreshed.");
            context.startService(new Intent(context, PebbLIFXService.class));
        } else {
            Log.i("PebbLIFXWifiReceiver", "Wifi DC'd, PebbLIFXService stopped.");
            context.stopService(new Intent(context, PebbLIFXService.class));
        }
    }
}
