package com.arnauds_squadron.eatup;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.event_details.EventDetailsActivity;
import com.arnauds_squadron.eatup.event_details.EventDetailsAdapter;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.Rating;
import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rbrooks.indefinitepagerindicator.IndefinitePagerIndicator;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.AVERAGE_RATING;
import static com.arnauds_squadron.eatup.utils.Constants.DISPLAY_NAME;
import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.arnauds_squadron.eatup.utils.Constants.NUM_RATINGS;

public class RateUserActivity extends AppCompatActivity {

    @BindView(R.id.rvUsers)
    RecyclerView rvUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_user);
        ButterKnife.bind(this);

        Event event = getIntent().getParcelableExtra("event");
        ParseUser eventHost = event.getHost();

        ParseUser currentUser = ParseUser.getCurrentUser();

        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(rvUsers);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1, GridLayoutManager.HORIZONTAL, false);
        rvUsers.setLayoutManager(gridLayoutManager);
        IndefinitePagerIndicator indefinitePagerIndicator = findViewById(R.id.recyclerview_pager_indicator);
        indefinitePagerIndicator.attachToRecyclerView(rvUsers);

        List<ParseUser> users = new ArrayList<>();
        RateUserAdapter rateUserAdapter = new RateUserAdapter(RateUserActivity.this, users, rvUsers);

        // if the current user is the host, switch to guest view
        if(currentUser.getObjectId().equals(event.getHost().getObjectId())) {
            // TODO allow host to rate the guest
            List<ParseUser> accepted = event.getAcceptedGuestsList();
            users.addAll(accepted);
        }
        // otherwise, rate the host
        else {
            users.add(eventHost);
        }
        rateUserAdapter.notifyDataSetChanged();
        rvUsers.setAdapter(rateUserAdapter);
    }
}
