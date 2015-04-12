package com.cohesionmit.cohesion;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

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
    private Activity activity;
    private Resources resources;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = getApplicationContext();
        activity = this;
        resources = context.getResources();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        FacebookSdk.sdkInitialize(context);
        
        if (prefs.getString(Utils.URL_KEY, null) != null) {
            goToHome();
        }
        
        setContentView(R.layout.activity_main);
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("public_profile");
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                loginButton.setVisibility(View.GONE);
                activity.findViewById(R.id.status_message).setVisibility(View.VISIBLE);
                
                GraphRequest request =
                        GraphRequest.newMeRequest(loginResult.getAccessToken(), mRegisterCallback);
                Bundle parameters = new Bundle();
                parameters.putString("fields", "first_name,last_name,link");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // TODO
            }

            @Override
            public void onError(FacebookException exception) {
                // TODO
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
            Editor editor = prefs.edit();
            editor.putBoolean(LocationService.ONLINE_KEY, true);
            editor.commit();
            
            startService(new Intent(this, LocationService.class));
        }
        
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        
        finish();
    }
    
    private void showRegError() {
        new AlertDialog.Builder(context)
        .setTitle(resources.getString(R.string.registration_error_title))
        .setMessage(resources.getString(R.string.registration_error_message))
        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { 
                activity.finish();
            }
        })
        .show();
    }
    
    private void handleDuplicateReg(final String link) {
        Utils.getClasses(link, new Utils.ResponseHandler() {
            @Override
            public void onSuccess(JSONObject json) {
                Editor editor = prefs.edit();
                editor.putString(Utils.URL_KEY, link);
                editor.commit();
                
                try {
                    JSONArray list = (JSONArray) json.get("classes");
                    int numClasses = list.length();
                    if (numClasses > 0) {
                        Map<String, String> classMap = new HashMap<String, String>();
                        for (int i = 0; i < numClasses; i++) {
                            JSONObject classEntry = list.getJSONObject(i);
                            classMap.put(classEntry.getString("name"),
                                    classEntry.getString("status"));
                        }
                        
                        Utils.updateLocalClasses(context, classMap);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                goToHome();
            }
            
            @Override
            public void onError(int errorCode) {
                showRegError();
            }
        });
    }
    
    private final GraphRequest.GraphJSONObjectCallback mRegisterCallback =
            new GraphRequest.GraphJSONObjectCallback() {
        @Override
        public void onCompleted(JSONObject object, GraphResponse response) {
            try {
                String firstName = object.get("first_name").toString();
                String lastName = object.get("last_name").toString();
                final String link = object.get("link").toString();
                
                Utils.register(firstName, lastName, link, new Utils.ResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject json) {
                        Editor editor = prefs.edit();
                        editor.putString(Utils.URL_KEY, link);
                        editor.commit();
                        
                        goToHome();
                    }
                    
                    @Override
                    public void onError(int errorCode) {
                        // 400 error means already registered
                        if (errorCode == 400) {
                            handleDuplicateReg(link);
                        } else {
                            showRegError();
                        }
                    }
                });
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };
}
