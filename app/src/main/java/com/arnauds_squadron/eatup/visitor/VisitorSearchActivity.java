package com.arnauds_squadron.eatup.visitor;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.EndlessRecyclerViewScrollListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
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
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.NO_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.USER_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.CUISINE_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.LOCATION_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.SEARCH_CATEGORY;


public class VisitorSearchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // initialize adapter, views, scroll listener
    private SearchEventAdapter eventAdapter;
    private ArrayList<Event> mEvents;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;
    private ProgressBar progressBar;
    private ParseUser currentUser;

    // variables to keep track of current query
    private int searchCategory;
    private ParseUser queriedUser;
    private String queriedCuisine;
    private ParseGeoPoint queriedGeoPoint;

    private final static Double DEFAULT_COORD = 0.0;

    String[] SEARCH_SUGGEST_COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
                                SearchManager.SUGGEST_COLUMN_INTENT_DATA
    };
    private String CURRENT_LOCATION_ID;

    @BindView(R.id.rvSearchResults)
    RecyclerView rvEvents;
    @BindView(R.id.resultsSearchView)
    SearchView resultsSearchView;
    @BindView(R.id.searchSpinner)
    Spinner searchSpinner;

    // adapter for search suggestions
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_visitor_search);
        ButterKnife.bind(this);

        Places.initialize(this, getString(R.string.google_api_key));
        placesClient = Places.createClient(this);

        // adapter for search suggestions
        final CursorAdapter suggestionAdapter = new SimpleCursorAdapter(this,
                R.layout.location_search_suggestion_item,
                null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_DATA},
                new int[]{R.id.tvLocationPrimary, R.id.tvLocationSecondary},
                0);

        // TODO use this for clicking on a suggestion
        // final List<String> locationSuggestions = new ArrayList<>();

//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        // initialize data source
        mEvents = new ArrayList<>();
        // construct adapter from data source
        eventAdapter = new SearchEventAdapter(this, mEvents);
        // RecyclerView setup
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvEvents.setLayoutManager(linearLayoutManager);
        rvEvents.setAdapter(eventAdapter);

        // initialize search services
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        resultsSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        resultsSearchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        resultsSearchView.setSuggestionsAdapter(suggestionAdapter);
        resultsSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                // if user scrolls through results using some sort of trackpad
                Cursor cursor = (Cursor) suggestionAdapter.getItem(position);
                String queryText = String.format(Locale.getDefault(),
                        "%s, %s",
                        cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                        cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2)));
                resultsSearchView.setQuery(queryText, false);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) suggestionAdapter.getItem(position);
                String queryText = String.format(Locale.getDefault(),
                        "%s, %s",
                        cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                        cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2)));
                resultsSearchView.setQuery(queryText, false);

                String locationQueryId = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA));
                if(locationQueryId.equals(CURRENT_LOCATION_ID)) {
                    // TODO get users current location again
                }

                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);
                FetchPlaceRequest request = FetchPlaceRequest.builder(locationQueryId, placeFields).build();
                placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse response) {
                        Place place = response.getPlace();
                        Double latitude = place.getLatLng().latitude;
                        Double longitude = place.getLatLng().longitude;
                        Log.i("Fetch Place Request", "Successful call");
                        locationSearch(new ParseGeoPoint(latitude, longitude), new Date(0));
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
                return true;
            }
        });

        resultsSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String incompleteQuery) {
                // TODO set different search suggestions for cuisine and location

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
                        MatrixCursor cursor = new MatrixCursor(SEARCH_SUGGEST_COLUMNS);
                        cursor.addRow(new String[] {
                                "1",
                                "Current Location",
                                "Use my current location",
                                CURRENT_LOCATION_ID
                        });

                        int predictionCounter = 2;
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                            Log.i("findPlaceSuggestions", prediction.getPlaceId());
                            Log.i("findPlaceSuggestions", prediction.getPrimaryText(null).toString());
                            cursor.addRow(new String[] {
                                    Integer.toString(predictionCounter),
                                    prediction.getPrimaryText(null).toString(),
                                    prediction.getSecondaryText(null).toString(),
                                    prediction.getPlaceId()
                            });
                            predictionCounter++;
                        }
                        suggestionAdapter.swapCursor(cursor);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        MatrixCursor cursor = new MatrixCursor(SEARCH_SUGGEST_COLUMNS);
                        cursor.addRow(new String[] {
                                "2",
                                "No places found",
                                "Try searching again",
                                "No results"
                        });

                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e("findPlaceSuggestions", "Place not found: " + apiException.getStatusCode());
                        }
                        suggestionAdapter.swapCursor(cursor);
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                mEvents.clear();
                eventAdapter.notifyDataSetChanged();
                searchCategory = searchSpinner.getSelectedItemPosition();
                if(searchCategory != NO_SEARCH) {
                    Intent searchIntent = new Intent(getApplicationContext(), VisitorSearchActivity.class);
                    searchIntent.putExtra(SearchManager.QUERY, query);
                    searchIntent.putExtra(SEARCH_CATEGORY, searchSpinner.getSelectedItemPosition());
                    searchIntent.setAction(Intent.ACTION_SEARCH);
                    startActivity(searchIntent);
                    // clear focus so search doesn't fire twice
                    resultsSearchView.clearFocus();
                    resultsSearchView.setQuery(query, false);
                    return true;
                }
                else {
                    // prevent submission if no category selected
                    resultsSearchView.setQuery(query, false);
                    Toast.makeText(getApplicationContext(), "Select a search category.", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        });

        // initialize spinner for search filtering
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.search_categories, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //c Apply the adapter to the spinner
        searchSpinner.setAdapter(adapter);
        searchSpinner.setOnItemSelectedListener(this);

        // handle search intent
        handleIntent(getIntent());

        // load data entries
        // retain instance so can call "resetStates" for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Date maxEventId = getMaxDate();
                Log.d("DATE", maxEventId.toString());
                handleRecyclerEvents(getMaxDate());
            }
        };
        // add endless scroll listener to RecyclerView and load items
        rvEvents.addOnScrollListener(scrollListener);

        // set up refresh listener that triggers new data loading
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handleRecyclerEvents(new Date(0));
            }
        });
        // configure refreshing colors
        swipeContainer.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));

    }

    private void handleRecyclerEvents(Date maxDate) {
        switch(searchCategory) {
            case USER_SEARCH:
                loadTopEvents(queriedUser, maxDate);
                break;
            case CUISINE_SEARCH:
                loadTopEvents(queriedCuisine, maxDate);
                break;
            case LOCATION_SEARCH:
                // TODO load top events for a refreshed location search
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
            switch(newSearchCategory) {
                case USER_SEARCH:
                    userSearch(query);
                    break;
                case CUISINE_SEARCH:
                    queriedCuisine = query;
                    loadTopEvents(queriedCuisine, new Date(0));
                    // TODO get search suggestions of cuisines from the Yelp Search API
                    break;
                case LOCATION_SEARCH:
                    // TODO implement location search
            }
        }
        // otherwise called by a click on something in VisitorFragment
        else {
            searchCategory = intent.getIntExtra(SEARCH_CATEGORY, 0);
            // if not any of the categories, user clicked on current/previous location
            if (searchCategory == NO_SEARCH) {
                Double latitude = intent.getDoubleExtra("latitude", DEFAULT_COORD);
                Double longitude = intent.getDoubleExtra("longitude", DEFAULT_COORD);
                queriedGeoPoint = new ParseGeoPoint(latitude, longitude);
                locationSearch(queriedGeoPoint, new Date(0));
            } else {
                // display user's choice in the spinner
                searchSpinner.setSelection(searchCategory);
            }
        }
    }

    // Methods for search category spinner
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        mEvents.clear();
        eventAdapter.notifyDataSetChanged();
        searchCategory = pos;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // TODO what does this entail
    }

    // Methods to query parse server
    private void userSearch(String userQuery) {
        // query for user, then query for events containing user
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("displayName", userQuery);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    if(objects.size() != 0) {
                        Log.d("VisitorSearchActivity", "found user");
                        queriedUser = objects.get(0);
                        loadTopEvents(queriedUser, new Date(0));
                    } else {
                        Toast.makeText(getApplicationContext(), "No users found with that username.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void locationSearch(ParseGeoPoint geoPoint, Date maxDate) {
        final Event.Query eventsQuery = new Event.Query();
        if (maxDate.equals(new Date(0))) {
            eventAdapter.clear();
            eventsQuery.getClosest(geoPoint).getTop().withHost();
        } else {
            eventsQuery.getOlder(maxDate).getClosest(geoPoint).getTop().withHost();
        }

        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    if(objects.size() != 0) {
                        for (int i = 0; i < objects.size(); ++i) {
                            mEvents.add(objects.get(i));
                            eventAdapter.notifyItemInserted(mEvents.size() - 1);
                            // on successful reload, signal that refresh has completed
                            swipeContainer.setRefreshing(false);
                        }
                    }
                    else {
                        // only notify user if no events to show
                        if(mEvents.size() == 0) {
                            Toast.makeText(getApplicationContext(), "No events found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    e.printStackTrace();
                }
//                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    protected void loadTopEvents(ParseUser user, Date maxDate) {
        final Event.Query eventsQuery = new Event.Query();
        // if opening app for the first time, get top 20 and clear old items
        // otherwise, query for posts older than the oldest
        if (maxDate.equals(new Date(0))) {
            eventAdapter.clear();
            eventsQuery.getTop().withHost().whereEqualTo("host", user);
        } else {
            eventsQuery.getOlder(maxDate).getTop().withHost().whereEqualTo("host", user);
        }
        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    if(objects.size() != 0) {
                        for (int i = 0; i < objects.size(); ++i) {
                            mEvents.add(objects.get(i));
                            eventAdapter.notifyItemInserted(mEvents.size() - 1);
                            // on successful reload, signal that refresh has completed
                            swipeContainer.setRefreshing(false);
                        }
                    }
                    else {
                        // only notify user if no events to show
                        if(mEvents.size() == 0) {
                            Toast.makeText(getApplicationContext(), "No events found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void loadTopEvents(String cuisineQuery, Date maxDate) {
        final Event.Query eventsQuery = new Event.Query();
        // if opening app for the first time, get top 20 and clear old items
        // otherwise, query for events older than the oldest
        if (maxDate.equals(new Date(0))) {
            eventAdapter.clear();
            eventsQuery.getTop().withHost().whereEqualTo("foodType", cuisineQuery);
        } else {
            eventsQuery.getOlder(maxDate).getTop().withHost().whereEqualTo("foodType", cuisineQuery);
        }
        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    if(objects.size() != 0) {
                        for (int i = 0; i < objects.size(); ++i) {
                            mEvents.add(objects.get(i));
                            eventAdapter.notifyItemInserted(mEvents.size() - 1);
                            // on successful reload, signal that refresh has completed
                            swipeContainer.setRefreshing(false);
                        }
                    }
                    else {
                        // only notify user if no events to show
                        if(mEvents.size() == 0) {
                            Toast.makeText(getApplicationContext(), "No events found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
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
}
