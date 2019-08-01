package com.arnauds_squadron.eatup;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rbrooks.indefinitepagerindicator.IndefinitePagerIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.GUEST;


public class RateUserActivity extends AppCompatActivity {

    @BindView(R.id.rvUsers)
    RecyclerView rvUsers;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_user);
        ButterKnife.bind(this);

        event = getIntent().getParcelableExtra("event");
        String ratingType = getIntent().getStringExtra("ratingType");
        ParseUser eventHost = event.getHost();

        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(rvUsers);


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1, GridLayoutManager.HORIZONTAL, false) {
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(getApplicationContext()) {

                    private static final float SPEED = 200f;// Change this value (default=25f)

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return SPEED / displayMetrics.densityDpi;
                    }

                };
                smoothScroller.setTargetPosition(position);
                startSmoothScroll(smoothScroller);
            }
        };

        rvUsers.setLayoutManager(gridLayoutManager);
        IndefinitePagerIndicator indefinitePagerIndicator = findViewById(R.id.recyclerview_pager_indicator);
        indefinitePagerIndicator.attachToRecyclerView(rvUsers);

        List<ParseUser> users = new ArrayList<>();
        RateUserAdapter rateUserAdapter = new RateUserAdapter(RateUserActivity.this, users, rvUsers, ratingType);

        // if the current user is the host, switch to guest view
        if(ratingType.equals(GUEST)) {
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

    @Override
    protected void onStop() {
        super.onStop();
        event.ratingSubmitted(Constants.CURRENT_USER);
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "Ratings complete.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error saving ratings.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
