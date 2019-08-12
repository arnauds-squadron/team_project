package com.arnauds_squadron.eatup.event_details;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.login.LoginActivity;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.rbrooks.indefinitepagerindicator.IndefinitePagerIndicator;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.arnauds_squadron.eatup.utils.FormatHelper.formatDateWithFullMonth;
import static com.arnauds_squadron.eatup.utils.FormatHelper.formatTime;


public class EventDetailsActivity extends AppCompatActivity {
    @BindView(R.id.tvEventTitle)
    TextView tvEventTitle;
    @BindView(R.id.tvDistance)
    TextView tvDistance;
    @BindView(R.id.rvEventDetails)
    RecyclerView rvEventDetails;
    @BindView(R.id.btRequest)
    Button btRequest;
    @BindView(R.id.btRequested)
    Button btRequested;
    @BindView(R.id.tvNumGuests)
    TextView tvNumGuests;
    @BindView(R.id.tvDay)
    TextView tvDate;

    private Event currentEvent;
    private EventDetailsAdapter eventDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        ButterKnife.bind(this);

        if (Constants.CURRENT_USER == null) {
            gotoLoginActivity();
        }

        String eventId = getIntent().getStringExtra("event_id");
        String distanceString = getIntent().getStringExtra("distance");

        tvDistance.setText(distanceString);

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
                    tvDate.setText(String.format(Locale.getDefault(),
                            "%s on %s",
                            formatTime(event.getDate(), EventDetailsActivity.this),
                            formatDateWithFullMonth(event.getDate())));

                    int numGuests;
                    List<ParseUser> accepted = event.getAcceptedGuestsList();
                    if(accepted != null) {
                        numGuests = accepted.size();
                    } else {
                        numGuests = 0;
                    }
                    tvNumGuests.setText(String.format(Locale.getDefault(), "%s/%s slots filled", numGuests, event.getMaxGuests()));

                    if (currentEvent.checkRequest(ParseUser.getCurrentUser())) {
                        btRequest.setVisibility(View.GONE);
                        btRequested.setVisibility(View.VISIBLE);
                    } else {
                        btRequested.setVisibility(View.GONE);
                        btRequest.setVisibility(View.VISIBLE);
                    }

                    btRequest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // create request/add to "allRequests"
                            currentEvent.createRequest(Constants.CURRENT_USER, currentEvent);
                            btRequest.setVisibility(View.GONE);
                            btRequested.setVisibility(View.VISIBLE);
                        }
                    });

                    btRequested.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(), "RSVP already requested", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void gotoLoginActivity() {
        Intent i = new Intent(EventDetailsActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}
