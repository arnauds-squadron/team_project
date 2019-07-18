package com.arnauds_squadron.eatup.visitor;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.EndlessRecyclerViewScrollListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class VisitorFragment extends Fragment {

    @BindView(R.id.tvUserName)
    TextView tvUserName;
    @BindView(R.id.tvBrowseTitle)
    TextView tvBrowseTitle;
    @BindView(R.id.rvBrowse)
    RecyclerView rvBrowse;
    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.tvCurrentLocation)
    TextView tvCurrentLocation;
    private Unbinder unbinder;
    private EndlessRecyclerViewScrollListener scrollListener;
    private BrowseEventAdapter postAdapter;
    private ArrayList<Event> mEvents;

    // variables for obtaining current user location
    private static final int REQUEST_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationClient;


    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    public static VisitorFragment newInstance() {
        Bundle args = new Bundle();
        VisitorFragment fragment = new VisitorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visitor, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // initialize data source
        mEvents = new ArrayList<>();
        // construct adapter from data source
        postAdapter = new BrowseEventAdapter(getContext(), mEvents);
        // RecyclerView setup
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false);
        rvBrowse.setLayoutManager(gridLayoutManager);
        rvBrowse.setAdapter(postAdapter);

        // initialize location client and get current user location
        fusedLocationClient = getFusedLocationProviderClient(getActivity());
        getCurrentLocation();

        startLocationUpdates();

        // load data entries

        // retain instance so can call "resetStates" for fresh searches
//        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//                Date maxPostId = getMaxDate();
//                Log.d("DATE", maxPostId.toString());
//                loadTopPosts(getMaxDate());
//            }
//        };
        // add endless scroll listener to RecyclerView
//        rvPosts.addOnScrollListener(scrollListener);
    }

    // TODO figure out whether this is also updating in the actual device. works on the emulator but not on device.
    // Trigger new location updates at interval
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            // do work here
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    },
                    Looper.myLooper());
            }
        }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }


    // TODO figure out why this method is being called in the home fragment
    // get current user location
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // location can be null if GPS switched off
                            if (location != null) {
                                Log.d("VisitorFragment", "location: "  + location.getLongitude() + location.getLatitude());
                                Toast.makeText(getContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getContext(), "Error: could not find last GPS location. 1", Toast.LENGTH_SHORT).show();
                                Log.d("VisitorFragment", "error 1");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("VisitorFragment", "get current location error 2");
//                            if (e instanceof ResolvableApiException) {
//                                // Location settings are not satisfied, but this can be fixed
//                                // by showing the user a dialog.
//                                try {
//                                    // Show the dialog by calling startResolutionForResult(),
//                                    // and check the result in onActivityResult().
//                                    ResolvableApiException resolvable = (ResolvableApiException) e;
//                                    resolvable.startResolutionForResult(getActivity(),
//                                            REQUEST_CHECK_SETTINGS);
//                                } catch (IntentSender.SendIntentException sendEx) {
//                                    Toast.makeText(getContext(), "Error: could not find last GPS location.", Toast.LENGTH_SHORT).show();
//                                    e.printStackTrace();
//                                }
//                            }
                        }
                    });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION:
                getCurrentLocation();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    // TODO fix query for loading the event into the recyclerview
// methods to load posts into the recyclerview based on location
//    protected void loadTopPosts(Date maxDate) {
//        progressBar.setVisibility(View.VISIBLE);
//        final Post.Query postsQuery = new Post.Query();
//        // if opening app for the first time, get top 20 and clear old items
//        // otherwise, query for posts older than the oldest
//        if (maxDate.equals(new Date(0))) {
//            eventAdapter.clear();
//            postsQuery.getTop().withUser();
//        } else {
//            postsQuery.getOlder(maxDate).getTop().withUser();
//        }
//
//        postsQuery.findInBackground(new FindCallback<Post>() {
//            @Override
//            public void done(List<Post> objects, ParseException e) {
//                if (e == null) {
//                    for (int i = 0; i < objects.size(); ++i) {
//                        mEvents.add(objects.get(i));
//                        eventAdapter.notifyItemInserted(mEvents.size() - 1);
//                        // on successful reload, signal that refresh has completed
//                        swipeContainer.setRefreshing(false);
//                    }
//                } else {
//                    e.printStackTrace();
//                }
//                progressBar.setVisibility(View.INVISIBLE);
//            }
//        });
//    }
//
//    // get date of oldest post
//    protected Date getMaxDate() {
//        int postsSize = mEvents.size();
//        if (postsSize == 0) {
//            return (new Date(0));
//        } else {
//            Post oldest = mEvents.get(mEvents.size() - 1);
//            return oldest.getCreatedAt();
//        }
//    }

}
