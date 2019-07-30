package com.arnauds_squadron.eatup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import com.arnauds_squadron.eatup.models.Event;
import com.parse.ParseUser;
import com.rbrooks.indefinitepagerindicator.IndefinitePagerIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.GUEST;


public class RateUserActivity extends AppCompatActivity {

    @BindView(R.id.rvUsers)
    RecyclerView rvUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_user);
        ButterKnife.bind(this);

        Event event = getIntent().getParcelableExtra("event");
        String ratingType = getIntent().getStringExtra("ratingType");
        ParseUser eventHost = event.getHost();

        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(rvUsers);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1, GridLayoutManager.HORIZONTAL, false);
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
}
