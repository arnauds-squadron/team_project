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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
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

    // initialize adapter, views, scroll listener
    private SearchEventAdapter eventAdapter;
    private ArrayList<Event> mEvents;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;
    private ProgressBar progressBar;
    private ParseUser user;
    private final static Double DEFAULT_COORD = 0.0;
    private int searchCategory;


    @BindView(R.id.rvSearchResults)
    RecyclerView rvEvents;
    @BindView(R.id.resultsSearchView)
    SearchView resultsSearchView;
    @BindView(R.id.searchSpinner)
    Spinner searchSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                int spinnerPosition = searchSpinner.getSelectedItemPosition();
                if(spinnerPosition != NO_SEARCH) {
                    Intent searchIntent = new Intent(getApplicationContext(), VisitorSearchActivity.class);
                    searchIntent.putExtra(SearchManager.QUERY, query);
                    searchIntent.putExtra(SEARCH_CATEGORY, searchSpinner.getSelectedItemPosition());
                    searchIntent.setAction(Intent.ACTION_SEARCH);
                    startActivity(searchIntent);
                    // clear focus so search doesn't fire twice
                    resultsSearchView.clearFocus();
                    resultsSearchView.setQuery(query, false);
                    return true;
                    // TODO figure out how to manipulate soft input keyboard state for easier user input
                }
                else {
                    // prevent submission if no category selected
                    resultsSearchView.setQuery(query, false);
                    Toast.makeText(getApplicationContext(), "Select a search category.", Toast.LENGTH_SHORT).show();
                    return false;
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

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
            int newSearchCategory = intent.getIntExtra(SEARCH_CATEGORY, 0);
            searchSpinner.setSelection(newSearchCategory);
            // TODO find out how to stop the keyboard from popping up, keep search term in the search bar
            Log.d("VisitorSearchActivity", "spinner position: " + newSearchCategory);
            switch(newSearchCategory) {
                case USER_SEARCH:
                    userSearch(query);
                    break;
                case CUISINE_SEARCH:
                    loadTopEvents(query);
                    // TODO get search suggestions of cuisines from the Yelp Search API
                    break;
                case LOCATION_SEARCH:
                    Log.d("VisitorSearchActivity", "location search:" + query);
                    //locationSearch(address);
                    // TODO location searches - use the location bar in the events creation screen?
                    break;
            }
        }
        // otherwise called by a click on something in VisitorFragment
        else {
            searchCategory = intent.getIntExtra(SEARCH_CATEGORY, 0);
            // display user's choice in the spinner
            searchSpinner.setSelection(searchCategory);
            // if not any of the categories, user clicked on current/previous location
            if(searchCategory == 0) {
                Double latitude = intent.getDoubleExtra("latitude", DEFAULT_COORD);
                Double longitude = intent.getDoubleExtra("longitude", DEFAULT_COORD);
                ParseGeoPoint location = new ParseGeoPoint(latitude, longitude);
                locationSearch(location);
            }
        }

/*
        // load data entries
        // retain instance so can call "resetStates" for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Date maxEventId = getMaxDate();
                Log.d("DATE", maxEventId.toString());
                loadTopEvents(getMaxDate());
            }
        };
        // add endless scroll listener to RecyclerView and load items
        rvEvents.addOnScrollListener(scrollListener);

        // TODO add the sort functionality by distance (can later add more "sort by criteria")
        // loadTopEvents(new Date(0));

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
*/
    }


    // methods for the search category spinner
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        searchCategory = pos;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // TODO what does this entail
    }

    // search methods
    private void userSearch(String userQuery) {
        // query for user, then query for events containing user
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("displayName", userQuery);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    if(objects.size() != 0) {
                        Log.d("VisitorSearchActivity", "found user");
                        ParseUser foundUser = objects.get(0);
                        loadTopEvents(foundUser);
                    } else {
                        Toast.makeText(getApplicationContext(), "No users found with that username.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void locationSearch(ParseGeoPoint geoPoint) {
        final Event.Query eventsQuery = new Event.Query();
        eventsQuery.getClosest(geoPoint).getTop().withHost();

        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        mEvents.add(objects.get(i));
                        eventAdapter.notifyItemInserted(mEvents.size() - 1);
                        // on successful reload, signal that refresh has completed
                        // swipeContainer.setRefreshing(false);
                    }
                } else {
                    e.printStackTrace();
                }
//                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    protected void loadTopEvents(ParseUser user) {
        final Event.Query eventsQuery = new Event.Query();
        // if opening app for the first time, get top 20 and clear old items
        // otherwise, query for posts older than the oldest
        eventsQuery.getTop().withHost().whereEqualTo("host", user);
        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        mEvents.add(objects.get(i));
                        eventAdapter.notifyItemInserted(mEvents.size() - 1);
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void loadTopEvents(String cuisineQuery) {
        final Event.Query eventsQuery = new Event.Query();
        // if opening app for the first time, get top 20 and clear old items
        // otherwise, query for posts older than the oldest
        eventsQuery.getTop().withHost().whereEqualTo("foodType", cuisineQuery);
        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        mEvents.add(objects.get(i));
                        eventAdapter.notifyItemInserted(mEvents.size() - 1);
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

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
}
