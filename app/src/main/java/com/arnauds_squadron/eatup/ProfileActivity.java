package com.arnauds_squadron.eatup;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.models.Event;
import com.parse.ParseImageView;
import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.io.File;
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
    @BindView(R.id.ivProfile)
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

        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        // load user rating
        Number rating = user.getNumber(AVERAGE_RATING);
        Number numRatings = user.getNumber(NUM_RATINGS);
        if (rating != null) {
            ratingBar.setRating(rating.floatValue());
        }
        else {
            ratingBar.setRating(NO_RATING);
        }
        tvRatings.setText(String.format(Locale.getDefault(),"(%s)", numRatings.toString()));

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