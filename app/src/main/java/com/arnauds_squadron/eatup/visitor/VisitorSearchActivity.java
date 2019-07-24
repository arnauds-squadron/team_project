package com.arnauds_squadron.eatup.visitor;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.EndlessRecyclerViewScrollListener;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
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

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.NO_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.USER_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.CUISINE_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.LOCATION_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.SEARCH_CATEGORY;


public class VisitorSearchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // TODO styling for location search activity - how to get rid of the action bar?

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
    private int AUTOCOMPLETE_REQUEST_CODE = 17;

    @BindView(R.id.rvSearchResults)
    RecyclerView rvEvents;
    @BindView(R.id.resultsSearchView)
    SearchView resultsSearchView;
    @BindView(R.id.searchSpinner)
    Spinner searchSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // TODO make searches case insensitive
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_visitor_search);
        ButterKnife.bind(this);

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

        resultsSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String arg0) {
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
                    finish();
                    return true;
                }
                else {
                    // TODO prevent submission if no category selected
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
            } else if (searchCategory == LOCATION_SEARCH) {
                startLocationSearchActivity();
            }
            else {
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
        if(searchCategory == LOCATION_SEARCH) {
            startLocationSearchActivity();
        }
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

    // handle results from a location search
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Double latitude = place.getLatLng().latitude;
                Double longitude = place.getLatLng().longitude;
                ParseGeoPoint location = new ParseGeoPoint(latitude, longitude);
                locationSearch(location, new Date(0));
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Toast.makeText(this, "Location search error. Try again.", Toast.LENGTH_SHORT).show();
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("VisitorSearchActivity", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Toast.makeText(this, "Location search canceled", Toast.LENGTH_SHORT).show();
            }
            searchSpinner.setSelection(NO_SEARCH);
        }
    }

    private void startLocationSearchActivity() {
        // for location searches, return latlng place data after user makes a selection
        List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG);
        // Start the location autocomplete intent.
        Intent locationIntent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(locationIntent, AUTOCOMPLETE_REQUEST_CODE);
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
