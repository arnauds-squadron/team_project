package com.arnauds_squadron.eatup.visitor;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.EndlessRecyclerViewScrollListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;

public class VisitorSearchActivity extends AppCompatActivity {

    // initialize adapter, views, scroll listener
    protected SearchEventAdapter eventAdapter;
    protected ArrayList<Event> mEvents;
    private RecyclerView rvEvents;
    protected SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;
    private ProgressBar progressBar;
    private ParseUser user;
    private TextView tvUsername;
    private ImageView ivProfileImage;
    private final String KEY_PROFILE_IMAGE = "profileImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_visitor_search);
        ButterKnife.bind(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        rvEvents = (RecyclerView) findViewById(R.id.rvSearchResults);

        // initialize data source
        mEvents = new ArrayList<>();
        // construct adapter from data source
        eventAdapter = new SearchEventAdapter(this, mEvents);
        // RecyclerView setup
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rvEvents.setLayoutManager(gridLayoutManager);
        rvEvents.setAdapter(eventAdapter);

        user = getIntent().getParcelableExtra("user");
        tvUsername.setText(user.getUsername());
        ParseFile profileImage = user.getParseFile(KEY_PROFILE_IMAGE);
        if (profileImage != null) {
            Glide.with(getApplicationContext())
                    .load(profileImage.getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileImage);
        }

        loadTopEvents(user, new Date(0));

        // retain instance so can call "resetStates" for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Date maxPostId = getMaxDate();
                Log.d("DATE", maxPostId.toString());
                loadTopEvents(user, getMaxDate());
            }
        };
        // add endless scroll listener to RecyclerView
        rvEvents.addOnScrollListener(scrollListener);

        // set up refresh listener that triggers new data loading
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTopEvents(user, new Date(0));
            }
        });
        // configure refreshing colors
        swipeContainer.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));
    }

    private void loadTopEvents(ParseUser user, Date maxDate) {
        progressBar.setVisibility(View.VISIBLE);
        final Event.Query eventsQuery = new Event.Query();
        // if opening app for the first time, get top 20 and clear old items
        // otherwise, query for events older than the oldest
        if (maxDate.equals(new Date(0))) {
            eventAdapter.clear();
            eventsQuery.getTop().withUser().whereEqualTo(Event.KEY_USER, user);
        } else {
            eventsQuery.getOlder(maxDate).getTop().withUser().whereEqualTo(Event.KEY_USER, user);
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
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    // get date of oldest event
    private Date getMaxDate() {
        int eventsSize = mEvents.size();
        if (eventsSize == 0) {
            return (new Date(0));
        } else {
            Event oldest = mEvents.get(mEvents.size() - 1);
            return oldest.getCreatedAt();
        }
    }
}
