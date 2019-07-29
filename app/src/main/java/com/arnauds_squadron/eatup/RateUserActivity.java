package com.arnauds_squadron.eatup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.Rating;
import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.w3c.dom.Comment;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.AVERAGE_RATING;
import static com.arnauds_squadron.eatup.utils.Constants.DISPLAY_NAME;
import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.arnauds_squadron.eatup.utils.Constants.NUM_RATINGS;

public class RateUserActivity extends AppCompatActivity {

    private Event event;
    private ParseUser currentUser;
    private ParseUser eventHost;

    @BindView(R.id.btSubmitRating)
    Button btSubmitRating;
    @BindView(R.id.tvUserToRate)
    TextView tvUserToRate;
    @BindView(R.id.ivUserToRate)
    ImageView ivUserToRate;
    @BindView(R.id.tvUserName)
    TextView tvUserName;
    @BindView(R.id.userRatingBar)
    RatingBar userRatingBar;
    @BindView(R.id.tvNoShow)
    TextView tvNoShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_user);
        ButterKnife.bind(this);

        event = getIntent().getParcelableExtra("event");
        eventHost = event.getHost();

        currentUser = ParseUser.getCurrentUser();

        // if the current user is the host, switch to guest view
        if(currentUser == event.getHost()) {
            // TODO allow host to rate the guest
        }
        // otherwise, rate the host
        else {
            btSubmitRating.setTag(eventHost.getObjectId());
            tvUserToRate.setText(eventHost.getString(DISPLAY_NAME));
            tvUserName.setText(eventHost.getString(DISPLAY_NAME));

            // load user profileImage
            ParseFile profileImage = eventHost.getParseFile(KEY_PROFILE_PICTURE);
            if (profileImage != null) {
                Glide.with(getApplicationContext())
                        .load(profileImage.getUrl())
                        .centerCrop()
                        .into(ivUserToRate);
            }

            btSubmitRating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitRating(eventHost);
                }
            });
        }
    }

    private void submitRating(final ParseUser user) {
        final float newRating = userRatingBar.getRating();

        ParseQuery<Rating> query = new Rating.Query();
        query.whereEqualTo("user", user);
        query.findInBackground(new FindCallback<Rating>() {
            public void done(List<Rating> ratings, ParseException e) {
                if (e == null) {
                    if(ratings.size() != 0) {
                        Rating rating = ratings.get(0);
                        float averageRating = rating.getAvgRating().floatValue();
                        int numRatings = rating.getNumRatings().intValue();

                        rating.put(AVERAGE_RATING, calculateRating(averageRating, numRatings, newRating));
                        rating.increment(NUM_RATINGS);

                        rating.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(getApplicationContext(), "User successfully rated.", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Log.d("RateUserActivity", "Error while saving");
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else {
                        createRating(newRating, user);
                    }

                } else {
                    Toast.makeText(getApplicationContext(),"Query for rating not successful",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private float calculateRating(float averageRating, int numRatings, float newRating) {
        float currentTotal = averageRating * numRatings;
        return (currentTotal + newRating) / (numRatings + 1);
    }

    private void createRating(float rating, ParseUser user) {
        final Rating newRating = new Rating();
        newRating.setAvgRating(rating);
        newRating.setUser(user);
        newRating.setNumRatings(1);

        newRating.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("RateUserActivity", "Create new rating successful");
                    Toast.makeText(getApplicationContext(), "User successfully rated.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.d("PostDetailsActivity", "Error: unable to make new rating");
                    e.printStackTrace();
                }
            }
        });
    }
}
