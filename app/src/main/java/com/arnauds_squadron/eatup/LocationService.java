package com.arnauds_squadron.eatup;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LocationService extends Service {

    public final static String LOCATION_UPDATE = "LOCATION_UPDATE";

    //Define fields for Google API Client
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback mLocationCallback;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // initialize current location services
        Log.i("service starting", "service starting");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        getLastLocation();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Log.i("result reecive", "reasult received");
                Intent intent = new Intent(LOCATION_UPDATE);
                intent.putExtra("location", result.getLastLocation());
                sendBroadcast(intent);
            }
        };
        return Service.START_STICKY;
    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     * <p>
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(getApplicationContext().getMainExecutor(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Intent intent = new Intent(LOCATION_UPDATE);
                            intent.putExtra("location", task.getResult());
                            sendBroadcast(intent);
                        } else {
                            // TODO add edge cases for non-successful calls to getLastLocation
                            startLocationUpdates();
//                            Log.d("LocationFragment", "getLastLocation:exception", task.getException());
//                            showSnackbar("Could not obtain precise location.", "Try again", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    startLocationUpdates();
//                                    Log.d("Do something clicked", "start location updates");
//                                }
//                            });
                        }
                    }
                });
    }


    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
