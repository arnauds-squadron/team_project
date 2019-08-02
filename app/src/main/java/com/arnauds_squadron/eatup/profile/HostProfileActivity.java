package com.arnauds_squadron.eatup.profile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Rating;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.BIO;
import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.arnauds_squadron.eatup.utils.Constants.NO_RATING;

public class HostProfileActivity extends AppCompatActivity {
    ParseUser user;
    @BindView(R.id.ivImage)
    ImageView ivProfile;
    @BindView(R.id.tvUsername)
    TextView tvUsername;
    @BindView(R.id.tvBio)
    TextView tvBio;
    @BindView(R.id.tvRatings)
    TextView tvRatings;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_profile);
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
                        float averageRating = rating.getAvgRatingHost().floatValue();
                        int numRatings = rating.getNumRatingsHost().intValue();
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