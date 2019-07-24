package com.arnauds_squadron.eatup.visitor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.BuildConfig;
import com.arnauds_squadron.eatup.ProfileActivity;
import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.EndlessRecyclerViewScrollListener;
import com.arnauds_squadron.eatup.utils.FetchAddressIntentService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.arnauds_squadron.eatup.utils.Constants.LOCATION_DATA_EXTRA;
import static com.arnauds_squadron.eatup.utils.Constants.RECEIVER;
import static com.arnauds_squadron.eatup.utils.Constants.RESULT_DATA_KEY;
import static com.arnauds_squadron.eatup.utils.Constants.SEARCH_CUISINE;
import static com.arnauds_squadron.eatup.utils.Constants.SEARCH_LOCATION;
import static com.arnauds_squadron.eatup.utils.Constants.SEARCH_USER;


public class VisitorFragment extends Fragment {

    // TODO browsing nearby events - account for scenario in which user doesn't enable current location, use last remembered location or display a random array of events

    @BindView(R.id.tvDisplayName)
    TextView tvDisplayName;
    @BindView(R.id.tvBrowseTitle)
    TextView tvBrowseTitle;
    @BindView(R.id.rvBrowse)
    RecyclerView rvBrowse;
    @BindView(R.id.resultsSearchView)
    SearchView searchView;
    @BindView(R.id.tvCurrentLocation)
    TextView tvCurrentLocation;
    @BindView(R.id.tvPrevLocation1)
    TextView tvPrevLocation1;
    @BindView(R.id.tvPrevLocation2)
    TextView tvPrevLocation2;
    @BindView(R.id.searchSpinner)
    Spinner searchSpinner;


    private Unbinder unbinder;
    private EndlessRecyclerViewScrollListener scrollListener;
    private BrowseEventAdapter eventAdapter;
    private ArrayList<Event> mEvents;


    //Define fields for Google API Client
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LocationCallback mLocationCallback;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 14;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private AddressResultReceiver resultReceiver;
    private String addressOutput;

    private String searchCategory;

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
        // TODO uncomment so can set name of current user
        // tvDisplayName.setText(ParseUser.getCurrentUser().getString(DISPLAY_NAME));
        // initialize data source
        mEvents = new ArrayList<>();
        // construct adapter from data source
        eventAdapter = new BrowseEventAdapter(getContext(), mEvents);
        // RecyclerView setup
        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(rvBrowse);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false);
        rvBrowse.setLayoutManager(gridLayoutManager);
        rvBrowse.setAdapter(eventAdapter);

        resultReceiver = new AddressResultReceiver(new Handler());

        // load data entries
        // retain instance so can call "resetStates" for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Date maxEventId = getMaxDate();
                Log.d("DATE", maxEventId.toString());
                loadTopEvents(getMaxDate());
            }
        };
        // add endless scroll listener to RecyclerView and load items
        rvBrowse.addOnScrollListener(scrollListener);
        loadTopEvents(new Date(0));

        // initialize current location services
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {

                    // TODO - tag current location with coordinates? or just store this information in the Parse database

                    // geocoder for translating coordinates to address
                    if (!Geocoder.isPresent()) {
                        Toast.makeText(getActivity(),
                                R.string.no_geocoder_available,
                                Toast.LENGTH_LONG).show();
                        tvCurrentLocation.setText(String.format(Locale.getDefault(), "%f, %f", location.getLatitude(), location.getLongitude()));

                    } else {
                        // Start geocoder service and update UI to reflect the new address
                        startIntentService(location);
                    }
//                    tvCurrentLocation.setTag(String.format(Locale.getDefault(), "%f, %f", location.getLatitude(), location.getLongitude()));
                    tvCurrentLocation.setTag(R.id.latitude, location.getLatitude());
                    tvCurrentLocation.setTag(R.id.longitude, location.getLongitude());
                }
            }
        };

        // initialize search services
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        // TODO tag previous locations with latitude and longitude. default (0, 0)
        tvPrevLocation1.setTag(String.format(Locale.getDefault(), "%f, %f", 0.0, 0.0));
        tvPrevLocation2.setTag(String.format(Locale.getDefault(), "%f, %f", 0.0, 0.0));

        // initialize spinner for search filtering
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.search_categories, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        searchSpinner.setAdapter(adapter);
        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // when item selected, bring user to the new search activity with search bar and search category packaged as intent extra
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    // search category hint
                    case 0:
                        break;
                    // user
                    case 1:
                        searchCategory = SEARCH_USER;
                        break;
                    // cuisine
                    case 2:
                        searchCategory = SEARCH_CUISINE;
                        break;
                    // location
                    case 3:
                        searchCategory = SEARCH_LOCATION;
                        break;
                }
                if(searchCategory != null) {
//                    Intent i = new Intent(getContext(), VisitorSearchActivity.class);
//                    i.putExtra(SEARCH_CATEGORY, searchCategory);
//                    getContext().startActivity(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO what does this entail
            }
        });
    }


    @OnClick({R.id.tvCurrentLocation, R.id.tvPrevLocation1, R.id.tvPrevLocation2})
    public void searchLocation(TextView tvLocation) {
        // TODO search the event database by current location. currently sends the lat/long data to SearchActivity
        Intent i = new Intent(getActivity(), VisitorSearchActivity.class);
        i.putExtra("latitude", (Double) tvLocation.getTag(R.id.latitude));
        i.putExtra("longitude", (Double) tvLocation.getTag(R.id.longitude));
        startActivity(i);
    }

    @OnClick(R.id.tvDisplayName)
    public void viewUserProfile() {
        Intent i = new Intent(getActivity(), ProfileActivity.class);
//        ParseUser user = ParseUser.getCurrentUser();
//        i.putExtra("user", user);
        getActivity().startActivity(i);
    }

    // methods to load posts into the recyclerview based on location
    protected void loadTopEvents(Date maxDate) {
//        progressBar.setVisibility(View.VISIBLE);
        final Event.Query eventsQuery = new Event.Query();
        // if opening app for the first time, get top 20 and clear old items
        // otherwise, query for posts older than the oldest
        if (maxDate.equals(new Date(0))) {
            eventAdapter.clear();
            eventsQuery.getTop().withHost();
        } else {
            eventsQuery.getOlder(maxDate).getTop().withHost();
        }

        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        mEvents.add(objects.get(i));
                        eventAdapter.notifyItemInserted(mEvents.size() - 1);
                        // on successful reload, signal that refresh has completed
//                        swipeContainer.setRefreshing(false);
                    }
                } else {
                    e.printStackTrace();
                }
//                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    // get date of oldest post
    protected Date getMaxDate() {
        int postsSize = mEvents.size();
        if (postsSize == 0) {
            return (new Date(0));
        } else {
            Event oldest = mEvents.get(mEvents.size() - 1);
            return oldest.getCreatedAt();
        }
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
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    // TODO account for case when device policy or previous settings set permission
    private void requestPermissions() {
        boolean shouldProvideRationale =
                shouldShowRequestPermissionRationale(
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
                // Permission denied.
                // Notify the user that GPS is necessary to use the current location component of the app.
                // Permission might have been rejected without asking the user for permission
                // device policy or "Never ask again" prompts).
                // TODO add ignore functionality so user can continue without inputting current location
                showSnackbar("EatUp needs your current location to find hosts near you.", "Settings",
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

                            // TODO - tag current location with coordinates? or just store this information in the Parse database

                            // geocoder for translating coordinates to address
                            if (!Geocoder.isPresent()) {
                                Toast.makeText(getActivity(),
                                        R.string.no_geocoder_available,
                                        Toast.LENGTH_LONG).show();
                                tvCurrentLocation.setText(String.format(Locale.getDefault(), "%f, %f", lastLocation.getLatitude(), lastLocation.getLongitude()));
                            } else {
                                // Start geocoder service and update UI to reflect the new address
                                startIntentService(lastLocation);
                            }
//                            tvCurrentLocation.setTag(String.format(Locale.getDefault(), "%f, %f", lastLocation.getLatitude(), lastLocation.getLongitude()));
                            tvCurrentLocation.setTag(R.id.latitude, lastLocation.getLatitude());
                            tvCurrentLocation.setTag(R.id.longitude, lastLocation.getLongitude());
                        } else {
                            // TODO add edge cases for nonsuccessful calls to getLastLocation
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    // start Intent to get address from lat/long coordinates
    protected void startIntentService(Location newLocation) {
        Intent intent = new Intent(getContext(), FetchAddressIntentService.class);
        intent.putExtra(RECEIVER, resultReceiver);
        intent.putExtra(LOCATION_DATA_EXTRA, newLocation);
        getActivity().startService(intent);
    }


    // ResultReceiver to set current location field based on address of lat/long
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            addressOutput = resultData.getString(RESULT_DATA_KEY);
            if (addressOutput == null) {
                addressOutput = "";
            }

//            // display current address to user if found.
//            if (resultCode == SUCCESS_RESULT) {
//                tvCurrentLocation.setText(addressOutput);
//            }

        }
    }
}
