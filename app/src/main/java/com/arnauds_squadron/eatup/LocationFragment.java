package com.arnauds_squadron.eatup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationFragment extends Fragment {
    private TextView txtLatitude, txtLongitude, txtAddress;

    //Define fields for Google API Client
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LocationCallback mLocationCallback;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 14;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    public static LocationFragment newInstance() {
        Bundle args = new Bundle();
        LocationFragment fragment = new LocationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        txtLatitude = (TextView) view.findViewById(R.id.txtLatitude);
        txtLongitude = (TextView) view.findViewById(R.id.txtLongitude);
        txtAddress = (TextView) view.findViewById(R.id.txtAddress);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (txtAddress.getText().toString().equals(""))
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        else
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    txtLatitude.setText(String.valueOf(location.getLatitude()));
                    txtLongitude.setText(String.valueOf(location.getLongitude()));
                    Log.d("LocationFragment", "Setting latitude and longitude");
                }
            }
        };
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        stopLocationUpdates();
        super.onPause();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    // TODO account for case when device policy or previous settings set permission
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i("LocationFragment", "Displaying permission rationale to provide additional context.");
            showSnackbar("EatUp needs your current location to find hosts near you.", "Grant permission",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            // Request permission. Can be auto answered if device policy sets the permission
            // or the user denied permission previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i("LocationFragment", "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i("LocationFragment", "User interaction was cancelled.");
            }
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
                startLocationUpdates();
            } else {
                Log.i("LocationFragment", "User permission was denied");
                Toast.makeText(getActivity(), "Test text", Toast.LENGTH_SHORT).show();
                // Permission denied.
                // Notify the user that GPS is necessary to use the current location component of the app.
                // Permission might have been rejected without asking the user for permission
                // device policy or "Never ask again" prompts).
                showSnackbar("Permission was denied", "Go to settings",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
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
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            lastLocation = task.getResult();
                            txtLatitude.setText(String.valueOf(lastLocation.getLatitude()));
                            txtLongitude.setText(String.valueOf(lastLocation.getLongitude()));
                        } else {
                            // TODO fix this, this isn't the best solution
                            Log.d("LocationFragment", "getLastLocation:exception", task.getException());
                            showSnackbar("Could not obtain precise location.", "Do something.", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startLocationUpdates();
                                    Log.d("Do something clicked", "start location updates");
                                }
                            });
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

//    private void showSnackbar(final int mainTextStringId, final int actionStringId,
//                               View.OnClickListener listener) {
//        Snackbar.make(getActivity().findViewById(android.R.id.content),
//                getString(mainTextStringId),
//                Snackbar.LENGTH_INDEFINITE)
//                .setAction(getString(actionStringId), listener).show();
//    }

    // rewrite above method to avoid int errors
    private void showSnackbar(String mainString, String actionString,
                              View.OnClickListener listener) {
        Snackbar.make(getActivity().findViewById(android.R.id.content),
                mainString,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(actionString, listener).show();
    }
}