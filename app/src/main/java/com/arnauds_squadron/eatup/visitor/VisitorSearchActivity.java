package com.arnauds_squadron.eatup.visitor;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.os.Build;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;

import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.Constants;
import com.arnauds_squadron.eatup.utils.EndlessRecyclerViewScrollListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.CATEGORY_ALIAS;
import static com.arnauds_squadron.eatup.utils.Constants.CATEGORY_TITLE;
import static com.arnauds_squadron.eatup.utils.Constants.CUISINE_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.SEARCH_CATEGORY;

public class VisitorSearchActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // initialize adapter, views, scroll listener
    private SearchEventAdapter eventAdapter;
    private ArrayList<Event> mEvents;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    private String CURRENT_LOCATION_ID = "currentLocation";
    private String ALL_EVENTS_TAG = "allEvents";
    private String CURRENT_LOCATION_STRING = "Current location";
    private String ALL_EVENTS_STRING = "All events";

    // variables to keep track of current query
    private int searchCategory;

    // queriedCuisineTag used to query database, queriedCuisineString displayed to the user
    private String queriedCuisineTag = ALL_EVENTS_TAG;
    private String queriedCuisineString = ALL_EVENTS_STRING;
    private String queriedLocationString = CURRENT_LOCATION_STRING;
    private ParseGeoPoint queriedGeoPoint;

    private final static Double DEFAULT_COORD = 0.0;

    String[] LOCATION_SEARCH_SUGGEST_COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA
    };

    String[] CATEGORY_SEARCH_SUGGEST_COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA
    };

    @BindView(R.id.rvSearchResults)
    RecyclerView rvEvents;
    @BindView(R.id.svCuisine)
    SearchView svCuisine;
    @BindView(R.id.svLocation)
    SearchView svLocation;
    @BindView(R.id.tvSearchQuery)
    TextView tvSearchQuery;
    @BindView(R.id.tvSearchResultsTitle)
    TextView tvResultsTitle;

    // adapter for location search suggestions
    private PlacesClient placesClient;

    /* Current user location using Google API Client: initialize variables
     * Source: https://medium.com/@ssaurel/getting-gps-location-on-android-with-fused-location-provider-api-1001eb549089
     */
    private Location currentLocation;
    private ParseGeoPoint currentGeoPoint;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds
    // lists to store location permissions
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    // code for permissions results request
    private static final int ALL_PERMISSIONS_RESULT = 1011;

    private Date currentDate;
    private Boolean useCurrentLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_search);
        ButterKnife.bind(this);

        currentDate = Calendar.getInstance(TimeZone.getDefault()).getTime();

        /* Current user location: manage permissions
         * Source: https://medium.com/@ssaurel/getting-gps-location-on-android-with-fused-location-provider-api-1001eb549089
         */
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = permissionsToRequest(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        // Build google api client
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        // Initialize Google Places client for location-based searches
        Places.initialize(this, getString(R.string.google_api_key));
        placesClient = Places.createClient(this);

        // Initialize data source for events recyclerview
        mEvents = new ArrayList<>();
        // RecyclerView setup
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        rvEvents.setLayoutManager(gridLayoutManager);

        Double currentLatitude = getIntent().getDoubleExtra("latitude", 0);
        Double currentLongitude = getIntent().getDoubleExtra("longitude", 0);
        currentGeoPoint = new ParseGeoPoint(currentLatitude, currentLongitude);
        queriedGeoPoint = currentGeoPoint;

        eventAdapter = new SearchEventAdapter(this, mEvents, currentGeoPoint);
        rvEvents.setAdapter(eventAdapter);

        // initialize search services
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        svCuisine.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        svCuisine.setIconifiedByDefault(false); // expand search when clicking anywhere on the searchview
        svCuisine.setQueryHint("Mexican, pizza, etc.");
        final AutoCompleteTextView cuisineTextView = (AutoCompleteTextView) svCuisine.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        cuisineTextView.setDropDownAnchor(R.id.svCuisine);

        svLocation.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        svLocation.setIconifiedByDefault(false);
        svLocation.setQueryHint("Address or street name");

        final AutoCompleteTextView locationTextView = (AutoCompleteTextView) svLocation.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        locationTextView.setDropDownAnchor(R.id.svLocation);

        useCurrentLocation = true;

        setCategorySuggestions(svCuisine);
        setLocationSuggestions(svLocation);

        // handle search intent
        handleIntent(getIntent());

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
        rvEvents.addOnScrollListener(scrollListener);

        // set up refresh listener that triggers new data loading
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTopEvents(new Date(0));
            }
        });
        // configure refreshing colors
        swipeContainer.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));
        tvSearchQuery.setVisibility(View.INVISIBLE);
        tvResultsTitle.setVisibility(View.INVISIBLE);
    }


    /* Current user location: functions to manage user permissions
    Source: https://medium.com/@ssaurel/getting-gps-location-on-android-with-fused-location-provider-api-1001eb549089
     */

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            Toast.makeText(this, "You need to install Google Play Services to use the App properly", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        // if user didn't specify a location, display event distance using current location
        if (useCurrentLocation) {
            if (currentLocation != null) {
                currentGeoPoint = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                eventAdapter.updateCurrentLocation(currentGeoPoint);
            }
        }
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissions necessary for EatUp to use your location", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d("VisitorSearch locChange", "Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(VisitorSearchActivity.this).
                                    setMessage("Permission must be granted for EatUp to use your location.").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.
                                                        toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).setNegativeButton("Cancel", null).create().show();

                            return;
                        }
                    }
                } else {
                    if (googleApiClient != null) {
                        googleApiClient.connect();
                    }
                }

                break;
        }
    }

    // Get the intent, verify the action and get the query
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
            int newSearchCategory = intent.getIntExtra(SEARCH_CATEGORY, 0);
            Log.d("VisitorSearchActivity", "spinner position: " + newSearchCategory);
            loadTopEvents(new Date(0));
        }
        // otherwise called by a click on something in VisitorFragment
        else {
            searchCategory = intent.getIntExtra(SEARCH_CATEGORY, 0);
            // either cuisine or location search
            if(searchCategory == CUISINE_SEARCH) {
                // set default location to current location from visitor fragment
                Double latitude = intent.getDoubleExtra("latitude", DEFAULT_COORD);
                Double longitude = intent.getDoubleExtra("longitude", DEFAULT_COORD);
                queriedGeoPoint = new ParseGeoPoint(latitude, longitude);
                svLocation.setQuery("Current location", false);
                svCuisine.onActionViewExpanded();
            } else {
                svCuisine.setQuery("All events", false);
                svLocation.onActionViewExpanded();
            }
        }
    }


    // Methods to query parse server
    private void locationSearch(ParseGeoPoint geoPoint, Date maxDate) {
        final Event.Query eventsQuery = new Event.Query();
        if (maxDate.equals(new Date(0))) {
            eventAdapter.clear();
            eventsQuery.getAvailable(currentDate).getClosest(geoPoint).getTopAscending().withHost().notOwnEvent(ParseUser.getCurrentUser()).notFilled().getPrevious(mEvents.size());
        } else {
            eventsQuery.getOlder(maxDate).getAvailable(currentDate).getClosest(geoPoint).getTopAscending().withHost().notOwnEvent(ParseUser.getCurrentUser()).notFilled().getPrevious(mEvents.size());
        }

        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() != 0) {
                        for (int i = 0; i < objects.size(); ++i) {
                            mEvents.add(objects.get(i));
                            eventAdapter.notifyItemInserted(mEvents.size() - 1);
                            // on successful reload, signal that refresh has completed
                        }
                    } else {
                        // only notify user if no events to show
                        if (mEvents.size() == 0) {
                            Toast.makeText(getApplicationContext(), "No events found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    swipeContainer.setRefreshing(false);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void loadTopEvents(Date maxDate) {
        // get all events in the area
        if(queriedCuisineTag.equals(ALL_EVENTS_TAG)) {
            locationSearch(queriedGeoPoint, new Date(0));
        }
        // get events according to tag
        else {
            final Event.Query eventsQuery = new Event.Query();
            // if opening app for the first time, get top 20 and clear old items
            // otherwise, query for events older than the oldest
            if (maxDate.equals(new Date(0))) {
                eventAdapter.clear();
                eventsQuery.getAvailable(currentDate).getTopAscending().withHost().getClosest(queriedGeoPoint).notOwnEvent(Constants.CURRENT_USER).notFilled().getPrevious(mEvents.size()).whereEqualTo("tags", queriedCuisineTag);
            } else {
                eventsQuery.getOlder(maxDate).getAvailable(currentDate).getTopAscending().withHost().getClosest(queriedGeoPoint).notOwnEvent(Constants.CURRENT_USER).notFilled().getPrevious(mEvents.size()).whereEqualTo("tags", queriedCuisineTag);
            }
            eventsQuery.findInBackground(new FindCallback<Event>() {
                @Override
                public void done(List<Event> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() != 0) {
                            for (int i = 0; i < objects.size(); ++i) {
                                mEvents.add(objects.get(i));
                                eventAdapter.notifyItemInserted(mEvents.size() - 1);
                            }
                        } else {
                            // only notify user if no events to show
                            if (mEvents.size() == 0) {
                                Toast.makeText(getApplicationContext(), "No events found.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        swipeContainer.setRefreshing(false);
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    // get date of oldest post
    protected Date getMaxDate() {
        int eventsSize = mEvents.size();
        if (eventsSize == 0) {
            return (new Date(0));
        } else {
            Event oldest = mEvents.get(mEvents.size() - 1);
            return oldest.getCreatedAt();
        }
    }

    private void setLocationSuggestions(final SearchView searchView) {
        // adapter for search suggestions
        final CursorAdapter locationSuggestionAdapter = new SimpleCursorAdapter(this,
                R.layout.location_search_suggestion_item,
                null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_DATA},
                new int[]{R.id.tvLocationPrimary, R.id.tvLocationSecondary},
                0);

        searchView.setSuggestionsAdapter(locationSuggestionAdapter);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                // if user scrolls through results using some sort of trackpad
                Cursor cursor = (Cursor) locationSuggestionAdapter.getItem(position);
                String queryText = String.format(Locale.getDefault(),
                        "%s, %s",
                        cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                        cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2)));
                searchView.setQuery(queryText, false);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) locationSuggestionAdapter.getItem(position);
                String locationQueryId = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA));
                String queryText;
                if (locationQueryId.equals(CURRENT_LOCATION_ID)) {
                    useCurrentLocation = true;
                    eventAdapter.updateCurrentLocation(currentGeoPoint);
                    queryText = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                    queriedLocationString = queryText;
                    queriedGeoPoint = currentGeoPoint;
                    loadTopEvents(new Date(0));
                    tvSearchQuery.setText(String.format(Locale.getDefault(), "\'%s\' at \'%s\'", queriedCuisineString, queriedLocationString));
                    tvSearchQuery.setVisibility(View.VISIBLE);
                    tvResultsTitle.setVisibility(View.VISIBLE);
                } else {
                    useCurrentLocation = false;
                    queryText = String.format(Locale.getDefault(),
                            "%s, %s",
                            cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                            cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2)));
                    queriedLocationString = queryText;
                    List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);
                    FetchPlaceRequest request = FetchPlaceRequest.builder(locationQueryId, placeFields).build();
                    placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @Override
                        public void onSuccess(FetchPlaceResponse response) {
                            Place place = response.getPlace();
                            Double latitude = place.getLatLng().latitude;
                            Double longitude = place.getLatLng().longitude;
                            Log.i("Fetch Place Request", "Successful call");
                            eventAdapter.updateCurrentLocation(new ParseGeoPoint(latitude, longitude));
                            queriedGeoPoint = new ParseGeoPoint(latitude, longitude);
                            loadTopEvents(new Date(0));
                            tvSearchQuery.setText(String.format(Locale.getDefault(), "\'%s\' at \'%s\'", queriedCuisineString, queriedLocationString));
                            tvSearchQuery.setVisibility(View.VISIBLE);
                            tvResultsTitle.setVisibility(View.VISIBLE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                                int statusCode = apiException.getStatusCode();
                                // Handle error with given status code.
                                Log.e("Fetch Place Request", "Place not found: " + exception.getMessage());
                            }
                        }
                    });
                }
                searchView.setQuery(queryText, false);
                searchView.clearFocus();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(final String incompleteQuery) {

                AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

                // Use the builder to create a FindAutocompletePredictionsRequest.
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        //.setLocationRestriction(bounds)
                        .setCountry("us")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(incompleteQuery)
                        .build();

                placesClient.findAutocompletePredictions(request).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onSuccess(FindAutocompletePredictionsResponse response) {
                        MatrixCursor cursor = new MatrixCursor(LOCATION_SEARCH_SUGGEST_COLUMNS);
                        cursor.addRow(new String[]{
                                "1",
                                "Current location",
                                "Use my current location",
                                CURRENT_LOCATION_ID
                        });

                        int predictionCounter = 2;
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                            Log.i("findPlaceSuggestions", prediction.getPlaceId());
                            Log.i("findPlaceSuggestions", prediction.getPrimaryText(null).toString());
                            cursor.addRow(new String[]{
                                    Integer.toString(predictionCounter),
                                    prediction.getPrimaryText(null).toString(),
                                    prediction.getSecondaryText(null).toString(),
                                    prediction.getPlaceId()
                            });
                            predictionCounter++;
                        }
                        locationSuggestionAdapter.swapCursor(cursor);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        MatrixCursor cursor = new MatrixCursor(LOCATION_SEARCH_SUGGEST_COLUMNS);
                        cursor.addRow(new String[]{
                                "2",
                                String.format(Locale.getDefault(), "No results found for \'%s\'", incompleteQuery),
                                "Try searching again",
                                "No results"
                        });

                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e("findPlaceSuggestions", "Place not found: " + apiException.getStatusCode());
                        }
                        locationSuggestionAdapter.swapCursor(cursor);
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.toLowerCase().equals("current location")) {
                    queriedLocationString = CURRENT_LOCATION_STRING;
                    mEvents.clear();
                    eventAdapter.notifyDataSetChanged();
                    Intent searchIntent = new Intent(getApplicationContext(), VisitorSearchActivity.class);
                    searchIntent.putExtra(SearchManager.QUERY, query);
                    searchIntent.setAction(Intent.ACTION_SEARCH);
                    startActivity(searchIntent);
                    // clear focus so search doesn't fire twice
                    searchView.clearFocus();
                    searchView.setQuery(query, false);
                    tvSearchQuery.setText(String.format(Locale.getDefault(), "\'%s\' at \'%s\'", queriedCuisineString, queriedLocationString));
                    tvSearchQuery.setVisibility(View.VISIBLE);
                    tvResultsTitle.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Select a location from the drop-down menu", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    private void setCategorySuggestions(final SearchView searchView) {
        // adapter for search suggestions
        final CursorAdapter categorySuggestionAdapter = new SimpleCursorAdapter(this,
                R.layout.category_search_suggestion_item,
                null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA},
                new int[]{R.id.tvCategory},
                0);

        searchView.setSuggestionsAdapter(categorySuggestionAdapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                // if user scrolls through results using some sort of trackpad
                Cursor cursor = (Cursor) categorySuggestionAdapter.getItem(position);
                String queryText = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                searchView.setQuery(queryText, false);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                // set default parameters for location: use current location
                useCurrentLocation = true;
                eventAdapter.updateCurrentLocation(currentGeoPoint);
                queriedGeoPoint = currentGeoPoint;

                Cursor cursor = (Cursor) categorySuggestionAdapter.getItem(position);
                String categoryQueryId = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA));
                String queryText = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                searchView.setQuery(queryText, false);

                queriedCuisineString = queryText;
                queriedCuisineTag = categoryQueryId;
                loadTopEvents(new Date(0));
                searchView.clearFocus();
                tvSearchQuery.setText(String.format(Locale.getDefault(), "\'%s\' at \'%s\'", queriedCuisineString, queriedLocationString));
                tvSearchQuery.setVisibility(View.VISIBLE);
                tvResultsTitle.setVisibility(View.VISIBLE);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String incompleteQuery) {

                if (incompleteQuery.equals("")) {
                    MatrixCursor cursor = new MatrixCursor(CATEGORY_SEARCH_SUGGEST_COLUMNS);
                    cursor.addRow(new String[]{
                            "1",
                            "All events",
                            ALL_EVENTS_TAG
                    });
                    categorySuggestionAdapter.swapCursor(cursor);
                } else {
                    MatrixCursor cursor = new MatrixCursor(CATEGORY_SEARCH_SUGGEST_COLUMNS);
                    cursor.addRow(new String[]{
                            "1",
                            "All events",
                            ALL_EVENTS_TAG
                    });
                    int predictionCounter = 2;
                    for (int i = 0; (i < CATEGORY_TITLE.length); i++) {
                        if (CATEGORY_TITLE[i].toLowerCase().startsWith(incompleteQuery.toLowerCase())) {
                            Log.i("findCategorySuggestions", CATEGORY_TITLE[i] + CATEGORY_ALIAS[i]);
                            if(predictionCounter < 7) {
                                cursor.addRow(new String[]{
                                        Integer.toString(predictionCounter),
                                        CATEGORY_TITLE[i],
                                        CATEGORY_ALIAS[i]
                                });
                                predictionCounter++;
                            }
                        }
                    }
                    if (predictionCounter == 2) {
                        if(!ALL_EVENTS_STRING.toLowerCase().startsWith(incompleteQuery.toLowerCase())) {
                            cursor.addRow(new String[]{
                                    "2",
                                    String.format(Locale.getDefault(), "No categories found for \'%s\'", incompleteQuery),
                                    "No results"
                            });
                        }
                    }
                    categorySuggestionAdapter.swapCursor(cursor);
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.toLowerCase().equals("all events")) {
                    queriedCuisineString = ALL_EVENTS_STRING;
                    mEvents.clear();
                    eventAdapter.notifyDataSetChanged();
                    Intent searchIntent = new Intent(getApplicationContext(), VisitorSearchActivity.class);
                    searchIntent.putExtra(SearchManager.QUERY, query);
                    searchIntent.setAction(Intent.ACTION_SEARCH);
                    startActivity(searchIntent);
                    // clear focus so search doesn't fire twice
                    searchView.clearFocus();
                    searchView.setQuery(query, false);
                    tvSearchQuery.setText(String.format(Locale.getDefault(), "\'%s\' at \'%s\'", queriedCuisineString, queriedLocationString));
                    tvSearchQuery.setVisibility(View.VISIBLE);
                    tvResultsTitle.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), "Select a cuisine from the drop-down menu", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }
}
