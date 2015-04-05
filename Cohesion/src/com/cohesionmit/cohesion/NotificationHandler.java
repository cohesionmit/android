package com.cohesionmit.cohesion;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class NotificationHandler extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String action = (String) getIntent().getExtras().get("action");
		if (action != null) {
			if (action.equals("disconnect")) {
				SharedPreferences prefs =
		        		PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				Editor editor = prefs.edit();
				editor.putBoolean(LocationService.ONLINE_KEY, false);
				editor.commit();
				
				stopService(new Intent(this, LocationService.class));
			}
		}

		finish();
	}
}