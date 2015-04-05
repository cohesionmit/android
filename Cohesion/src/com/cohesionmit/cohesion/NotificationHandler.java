package com.cohesionmit.cohesion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class NotificationHandler extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String action = (String) getIntent().getExtras().get("action");
		if (action != null) {
			if (action.equals("disconnect")) {
				stopService(new Intent(this, LocationService.class));
			}
		}

		finish();
	}
}