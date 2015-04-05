package com.cohesionmit.cohesion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class KeepaliveReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			if (prefs.getBoolean(LocationService.ONLINE_KEY, true)) {
				Intent pushIntent = new Intent(context, LocationService.class);
				context.startService(pushIntent);
			}
		}
	}
}