package com.cohesionmit.cohesion;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;


public class LocationService extends Service implements LocationListener,
GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	
	private final static int UPDATE_FREQ = 1000 * 30;
	private final static int FASTEST_UPDATE_FREQ = 1000 * 5;
	private final static int NOTIFICATION_ID = 9001;

	private Notification mNotification;
	private NotificationManager mNotificationManager;
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
    private boolean mInProgress = false;
    
    @Override
	public void onCreate() {
        super.onCreate();
        
        if (!checkServices()) {
        	return;
        }
        
        NotificationCompat.Builder notificationBuilder =
    	        new NotificationCompat.Builder(this)
		    	        .setSmallIcon(R.drawable.notification_icon)
		    	        .setContentTitle(getString(R.string.notification_title))
		    	        .setContentText(getString(R.string.notification_text))
		    	        .setOngoing(true);
        
        Intent resultIntent = new Intent(this, NotificationHandler.class);
        resultIntent.putExtra("action", "disconnect");

    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	stackBuilder.addParentStack(MainActivity.class);
    	stackBuilder.addNextIntent(resultIntent);
    	PendingIntent resultPendingIntent =
    	        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    	notificationBuilder.setContentIntent(resultPendingIntent);
    	
    	mNotification = notificationBuilder.build();
        
        mNotificationManager =
        	    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
        
        mLocationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(UPDATE_FREQ)
				.setFastestInterval(FASTEST_UPDATE_FREQ)
				.setSmallestDisplacement(0);
    }
    
    public int onStartCommand(Intent intent, int flags, int startId) {
    	super.onStartCommand(intent, flags, startId);

    	if (!checkServices() || mGoogleApiClient.isConnected() || mInProgress) {
    		return START_STICKY;
    	}
    	
    	if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !mInProgress) {
    		mInProgress = true;
			mGoogleApiClient.connect();
		}

    	return START_STICKY;
    }
    
    @Override
    public void onConnected(Bundle bundle) {
    	LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);
    	
    	showNotification();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
    	mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    	mInProgress = false;
    	hideNotification();
    }
    
    @Override
    public void onDestroy(){
        mInProgress = false;
        hideNotification();
        
        if(checkServices() && mGoogleApiClient != null) {
        	LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        	mGoogleApiClient.disconnect();
	        mGoogleApiClient = null;
        }
        
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
    	return null;
    }
    
    @Override
	public void onLocationChanged(Location location) {
		// TODO
	}
    
    private boolean checkServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if (resultCode == ConnectionResult.SUCCESS) {
			return true;
		} else {
			// This service cannot function without Play Services
			stopSelf();
			return false;
		}
	}
    
    public void showNotification() {
    	if (mInProgress) {
    		mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    	}
    }
    
    public void hideNotification() {
    	mNotificationManager.cancel(NOTIFICATION_ID);
    }
}