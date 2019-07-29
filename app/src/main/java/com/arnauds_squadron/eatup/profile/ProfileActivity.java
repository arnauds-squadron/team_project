package com.arnauds_squadron.eatup.profile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.Rating;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.io.File;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.AVERAGE_RATING;
import static com.arnauds_squadron.eatup.utils.Constants.BIO;
import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.arnauds_squadron.eatup.utils.Constants.NO_RATING;
import static com.arnauds_squadron.eatup.utils.Constants.NUM_RATINGS;

public class ProfileActivity extends AppCompatActivity {
    ParseUser user;
    @BindView(R.id.ivImage)
    ImageView ivProfile;
    @BindView(R.id.tvUsername)
    TextView tvUsername;
    @BindView(R.id.tvUsername2)
    TextView tvUsername2;
    @BindView(R.id.tvBio)
    TextView tvBio;
    @BindView(R.id.tvRatings)
    TextView tvRatings;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        user = getIntent().getParcelableExtra("user");

        // load user rating
        ParseQuery<Rating> query = new Rating.Query();
        query.whereEqualTo("user", user);
        query.findInBackground(new FindCallback<Rating>() {
            public void done(List<Rating> ratings, ParseException e) {
                if (e == null) {
                    if(ratings.size() != 0) {
                        Rating rating = ratings.get(0);
                        float averageRating = rating.getAvgRating().floatValue();
                        int numRatings = rating.getNumRatings().intValue();
                        ratingBar.setRating(averageRating);
                        tvRatings.setText(String.format(Locale.getDefault(),"(%s)", numRatings));
                    }
                    else {
                        ratingBar.setRating(NO_RATING);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Query for rating not successful", Toast.LENGTH_LONG).show();
                }
            }
        });

        // load user profileImage
        ParseFile profileImage = user.getParseFile(KEY_PROFILE_PICTURE);
        if (profileImage != null) {
            Glide.with(getApplicationContext())
                    .load(profileImage.getUrl())
                    .centerCrop()
                    .into(ivProfile);
        }
        String username = user.getUsername();
        if(user.getUsername() != null) {
            tvUsername.setText(username);
            tvUsername2.setText(username);
        }

        // load user bio
        String bio = user.getString(BIO);
        if (bio != null) {
            tvBio.setText(bio);
        }
        else {
            tvBio.setText(R.string.no_bio);
        }
    }
}