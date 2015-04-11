package com.cohesionmit.cohesion;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

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
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;


public class LocationService extends Service implements LocationListener,
GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    
    public final static String ONLINE_KEY = "online";
    
    private final static int UPDATE_FREQ = 1000 * 30;
    private final static int FASTEST_UPDATE_FREQ = 1000 * 5;
    private final static int MIN_DISPLACEMENT = 10;
    private final static int NOTIFICATION_ID = 9001;
    private final static int KEEPALIVE_INTERVAL = 1000 * 60 * 15;
    
    private WakeLock mWakeLock;
    private Handler mKeepaliveHandler;
    private Notification mNotification;
    private NotificationManager mNotificationManager;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mInProgress = false;
    
    private final IBinder mBinder = new LocalBinder();
    private final Timer mTimer = new Timer();
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        if (!checkServices()) {
            return;
        }
        
        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeLock");
        
        mKeepaliveHandler = new KeepaliveHandler(this);
        
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
                .setSmallestDisplacement(MIN_DISPLACEMENT);
    }
    
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        
        if (mNotification == null || mNotificationManager == null
                || mGoogleApiClient == null || mLocationRequest == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (mGoogleApiClient.isConnected() || mInProgress) {
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
        
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
        mTimer.scheduleAtFixedRate(mKeepaliveTask, 0, KEEPALIVE_INTERVAL);
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;
        hideNotification();
        
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }
    
    @Override
    public void onDestroy() {
        mInProgress = false;
        hideNotification();
        
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
        
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    @Override
    public void onLocationChanged(Location location) {
        sendLocation(location);
    }
    
    public void refreshLocation() {
        if (mGoogleApiClient != null) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            sendLocation(location);
        }
    }
    
    private void sendLocation(Location location) {
        if (location != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String link = prefs.getString(Utils.URL_KEY, null);
            
            if (link != null) {
                Utils.location(link, location, null);
            }
        }
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
        if (mInProgress && mNotificationManager != null && mNotification != null) {
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }
    }
    
    public void hideNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }
    
    public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }
    
    private static class KeepaliveHandler extends Handler {
        private final WeakReference<LocationService> mService;
        
        public KeepaliveHandler(LocationService service) {
            mService = new WeakReference<LocationService>(service);
        }
        
        @Override
        public void handleMessage(Message msg) {
            LocationService service = mService.get();
            if (service != null) {
                service.refreshLocation();
            }
        }
    }
    
    private final TimerTask mKeepaliveTask = new TimerTask() {
        public void run() {
            if (mKeepaliveHandler != null) {
                mKeepaliveHandler.sendEmptyMessage(0);
            }
        }
    };
}