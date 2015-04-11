package com.cohesionmit.cohesion;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class MainActivity extends Activity {
	
	private CallbackManager callbackManager;
	private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = getApplicationContext();
        FacebookSdk.sdkInitialize(context);
        
        SharedPreferences prefs =
        		PreferenceManager.getDefaultSharedPreferences(context);
    	if (prefs.getString(Utils.URL_KEY, null) != null) {
    		goToHome();
    	}
        
        setContentView(R.layout.activity_main);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("public_profile");
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
            	GraphRequest request =
            			GraphRequest.newMeRequest(loginResult.getAccessToken(), mRegisterCallback);
            	Bundle parameters = new Bundle();
            	parameters.putString("fields", "first_name,last_name,link");
            	request.setParameters(parameters);
            	request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }
    
    @Override
    protected void onActivityResult(final int reqCode, final int resCode, final Intent data) {
    	super.onActivityResult(reqCode, resCode, data);
    	callbackManager.onActivityResult(reqCode, resCode, data);
    }

    private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if (resultCode == ConnectionResult.SUCCESS) {
			return true;
		} else {
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
			return false;
		}
	}
    
    private void goToHome() {
    	if (checkPlayServices()) {
    		SharedPreferences prefs =
            		PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = prefs.edit();
			editor.putBoolean(LocationService.ONLINE_KEY, true);
			editor.commit();
			
        	startService(new Intent(this, LocationService.class));
        }
    	
    	Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        
        finish();
    }
    
    private final GraphRequest.GraphJSONObjectCallback mRegisterCallback =
    		new GraphRequest.GraphJSONObjectCallback() {
        @Override
        public void onCompleted(JSONObject object, GraphResponse response) {
            try {
            	SharedPreferences prefs =
    	        		PreferenceManager.getDefaultSharedPreferences(context);
            	if (prefs.getString(Utils.URL_KEY, null) != null) {
            		return;
            	}
            	
            	String firstName = object.get("first_name").toString();
            	String lastName = object.get("last_name").toString();
            	String link = object.get("link").toString();
            	
    			Editor editor = prefs.edit();
    			editor.putString(Utils.URL_KEY, link);
    			editor.commit();
    			
            	Utils.register(firstName, lastName, link, null);
            	
            	goToHome();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    };
}
