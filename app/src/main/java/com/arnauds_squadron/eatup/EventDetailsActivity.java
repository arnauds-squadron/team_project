package com.arnauds_squadron.eatup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.Constants;
import com.bumptech.glide.Glide;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventDetailsActivity extends AppCompatActivity {

    // TODO allow user to click through details about the host
    // TODO sliding view to let user view details of the restaurant from the yelp API
    // TODO styling - add progress bar for background network tasks

    @BindView(R.id.tvEventTitle)
    TextView tvEventTitle;
    @BindView(R.id.tvCuisine)
    TextView tvCuisine;
    @BindView(R.id.tvDistance)
    TextView tvDistance;
    @BindView(R.id.ivEventImage)
    ImageView ivEventImage;
    @BindView(R.id.tvHostName)
    TextView tvHostName;
    @BindView(R.id.hostRating)
    RatingBar hostRating;
    @BindView(R.id.tvHostDescription)
    TextView tvHostDescription;
    @BindView(R.id.btRequest)
    Button btRequest;

    private String eventId;

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
                    tvEventTitle.setText(event.getTitle());
                    tvCuisine.setText(event.getCuisine());
                    // TODO add calculation for distance from the user
                    // tvDistance.setText();
                    // TODO add text
                    tvHostName.setText(event.getHost().getString(Constants.DISPLAYNAME));
                    tvHostDescription.setText(event.getHost().getString(Constants.HOST_BIO));
                    // TODO add recyclerview for multiple event images
                    ParseFile eventImage = event.getEventImage();
                    if (eventImage != null) {
                        Glide.with(getApplicationContext())
                                .load(event.getEventImage().getUrl())
                                .into(ivEventImage);
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
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

