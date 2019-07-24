package com.arnauds_squadron.eatup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.models.Event;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.rbrooks.indefinitepagerindicator.IndefinitePagerIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class EventDetailsActivity extends AppCompatActivity {

    // TODO sliding view to let user view details of the restaurant from the yelp API
    // TODO add calculation for distance from the user

    @BindView(R.id.tvEventTitle)
    TextView tvEventTitle;
    @BindView(R.id.tvCuisine)
    TextView tvCuisine;
    @BindView(R.id.tvDistance)
    TextView tvDistance;
    @BindView(R.id.rvEventDetails)
    RecyclerView rvEventDetails;
    @BindView(R.id.btRequest)
    Button btRequest;

    private String eventId;
    private Event currentEvent;
    private EventDetailsAdapter eventDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        ButterKnife.bind(this);

        eventId = getIntent().getStringExtra("event_id");

        ParseQuery<Event> query = ParseQuery.getQuery(Event.class);
        // try to find item from cache, otherwise go to network
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK); // or CACHE_ONLY
        // query for post and include user info
        final Event.Query eventQuery = new Event.Query();
        eventQuery.withHost().getInBackground(eventId, new GetCallback<Event>() {
            @Override
            public void done(final Event event, ParseException e) {
                if (e == null) {
                    currentEvent = event;
                    eventDetailsAdapter = new EventDetailsAdapter(EventDetailsActivity.this, event, rvEventDetails);
                    SnapHelper pagerSnapHelper = new PagerSnapHelper();
                    pagerSnapHelper.attachToRecyclerView(rvEventDetails);
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1, GridLayoutManager.HORIZONTAL, false);
                    rvEventDetails.setLayoutManager(gridLayoutManager);
                    rvEventDetails.setAdapter(eventDetailsAdapter);

                    IndefinitePagerIndicator indefinitePagerIndicator = findViewById(R.id.recyclerview_pager_indicator);
                    indefinitePagerIndicator.attachToRecyclerView(rvEventDetails);

                    tvEventTitle.setText(event.getTitle());
                    tvCuisine.setText(event.getCuisine());

                    }
                else {
                    e.printStackTrace();
                }
            }
        });
    }

    @OnClick(R.id.btRequest)
    public void eventRSVP(Button btRequest) {
        // TODO send request to Parse server to RSVP to the event
        Toast.makeText(this, "Execute RSVP to the event", Toast.LENGTH_SHORT).show();
        // if user has already requested in the past or is already RSVP'd to the event, prevent user from clicking button
        // otherwise, create request/add to "allRequests" and send back to home screen
        if (currentEvent.checkRequest(ParseUser.getCurrentUser(), currentEvent)) {
            Toast.makeText(this, "RSVP already requested", Toast.LENGTH_SHORT).show();
        } else {
            currentEvent.createRequest(ParseUser.getCurrentUser(), currentEvent);
            Toast.makeText(this, "RSVP requested", Toast.LENGTH_SHORT).show();
        }
    }

    //    private void logoutUser() {
//        ParseUser.logOut();
//    }
//
//    private void gotoLoginActivity() {
//        Intent i = new Intent(EventDetailsActivity.this, LoginActivity.class);
//        startActivity(i);
//        finish();
//    }
}
