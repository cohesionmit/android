package com.cohesionmit.cohesion;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.AccessToken;
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


public class MainActivity extends ActionBarActivity {
	
	private CallbackManager callbackManager;
	private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        FacebookSdk.sdkInitialize(context);
        setContentView(R.layout.activity_main);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        final TextView username = (TextView) findViewById(R.id.username);
        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("public_profile");
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
            	GraphRequest request = GraphRequest.newMeRequest(
            	        loginResult.getAccessToken(),
            	        new GraphRequest.GraphJSONObjectCallback() {
            	            @Override
            	            public void onCompleted(
            	                   JSONObject object,
            	                   GraphResponse response) {
            	                try {
            	                	String firstName = object.get("first_name").toString();
            	                	String lastName = object.get("last_name").toString();
            	                	String link = object.get("link").toString();
									username.setText(link);
									
									Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
									startActivity(browserIntent);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
            	            }
            	        });
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
        
        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token == null) {
        	username.setText("No login");
        } else {
        	username.setText("Logged in");
        }
        
        if (checkPlayServices()) {
        	startService(new Intent(this, LocationService.class));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	callbackManager.onActivityResult(requestCode, resultCode, data);
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
}
