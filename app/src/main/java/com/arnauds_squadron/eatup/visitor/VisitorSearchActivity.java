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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.EndlessRecyclerViewScrollListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VisitorSearchActivity extends AppCompatActivity {

    // initialize adapter, views, scroll listener
    private SearchEventAdapter eventAdapter;
    private ArrayList<Event> mEvents;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;
    private ProgressBar progressBar;
    private ParseUser user;
    private final static Double DEFAULT_COORD = 0.0;

    @BindView(R.id.rvSearchResults)
    RecyclerView rvEvents;
    @BindView(R.id.resultsSearchView)
    SearchView resultsSearchView;

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

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
            // TODO implement additional search functionality
            // doMySearch(query);
        }
        // otherwise called by a click on the location in VisitorFragment
        else {
            Double latitude = intent.getDoubleExtra("latitude", DEFAULT_COORD);
            Double longitude = intent.getDoubleExtra("longitude", DEFAULT_COORD);
            ParseGeoPoint location = new ParseGeoPoint(latitude, longitude);
            searchByDistance(location);
            // TODO implement additional search functionality
            // doMySearch(query);
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

    private void searchByDistance(ParseGeoPoint geoPoint) {
        Toast.makeText(this, "geoPoint successfully created", Toast.LENGTH_SHORT).show();
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
                        swipeContainer.setRefreshing(false);
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
