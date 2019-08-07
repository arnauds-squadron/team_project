package com.arnauds_squadron.eatup;

import android.content.Context;
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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.Rating;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rbrooks.indefinitepagerindicator.IndefinitePagerIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.arnauds_squadron.eatup.utils.Constants.AVG_RATINGS_GUEST;
import static com.arnauds_squadron.eatup.utils.Constants.AVG_RATING_HOST;
import static com.arnauds_squadron.eatup.utils.Constants.GUEST;
import static com.arnauds_squadron.eatup.utils.Constants.HOST;
import static com.arnauds_squadron.eatup.utils.Constants.NUM_RATINGS_GUEST;
import static com.arnauds_squadron.eatup.utils.Constants.NUM_RATINGS_HOST;


public class RateUserActivity extends AppCompatActivity {

    @BindView(R.id.rvUsers)
    RecyclerView rvUsers;
    @BindView(R.id.btSubmit)
    Button btSubmit;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_user);
        ButterKnife.bind(this);

        event = getIntent().getParcelableExtra("event");
        final String ratingType = getIntent().getStringExtra("ratingType");
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
        final RateUserAdapter rateUserAdapter = new RateUserAdapter(RateUserActivity.this, users, rvUsers, ratingType);

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

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int childCount = rvUsers.getChildCount(), i = 0; i < childCount; ++i) {
                    final RateUserAdapter.VH holder = (RateUserAdapter.VH) rvUsers.getChildViewHolder(rvUsers.getChildAt(i));
                    submitRating((ParseUser) holder.rootView.getTag(), holder.userRatingBar.getRating(), ratingType);
                }

                btSubmit.setText("    Rating submitted    ");
                btSubmit.setOnClickListener(null);
                finish();
            }
        });
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

    void submitRating(final ParseUser user, final float newRating, final String ratingType) {
        ParseQuery<Rating> query = new Rating.Query();
        query.whereEqualTo("user", user);
        query.findInBackground(new FindCallback<Rating>() {
            public void done(List<Rating> ratings, ParseException e) {
                if (e == null) {
                    if (ratings.size() != 0) {
                        Rating rating = ratings.get(0);
                        if (ratingType.equals(HOST)) {
                            float averageRating = rating.getAvgRatingHost().floatValue();
                            int numRatings = rating.getNumRatingsHost().intValue();

                            rating.put(AVG_RATING_HOST, calculateRating(averageRating, numRatings, newRating));
                            rating.increment(NUM_RATINGS_HOST);
                        } else {
                            float averageRating = rating.getAvgRatingGuest().floatValue();
                            int numRatings = rating.getNumRatingsGuest().intValue();

                            rating.put(AVG_RATINGS_GUEST, calculateRating(averageRating, numRatings, newRating));
                            rating.increment(NUM_RATINGS_GUEST);
                        }
                        rating.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.d("RateUserActivity", "Users successfully rated");
                                } else {
                                    Log.d("RateUserActivity", "Error while saving");
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        createRating(newRating, user, ratingType);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Query for rating not successful", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private float calculateRating(float averageRating, int numRatings, float newRating) {
        float currentTotal = averageRating * numRatings;
        return (currentTotal + newRating) / (numRatings + 1);
    }

    private void createRating(float rating, ParseUser user, String ratingType) {
        final Rating newRating = new Rating();
        newRating.setUser(user);
        if(ratingType.equals(HOST)) {
            newRating.setAvgRatingHost(rating);
            newRating.setNumRatingsHost(1);
        } else {
            newRating.setAvgRatingGuest(rating);
            newRating.setNumRatingsGuest(1);
        }
        newRating.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("RateUserActivity", "Create new rating successful");
                } else {
                    Log.d("RateUserActivity", "Error: unable to make new rating");
                    e.printStackTrace();
                }
            }
        });
    }
}
