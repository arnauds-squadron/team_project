package com.arnauds_squadron.eatup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.models.User;
import com.parse.ParseImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {

    User user;
    @BindView(R.id.ivProfile)
    ParseImageView ivProfile;
    @BindView(R.id.tvUsername)
    TextView tvUsername;
    @BindView(R.id.tvBio)
    TextView tvBio;
    @BindView(R.id.tvRatings)
    TextView tvRatings;
    @BindView(R.id.rating)
    RatingBar rating;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

//        if(user.getFloatRating() != null) {
//            float floatRating = user.getFloatRating();
//            rating.setRating(floatRating = floatRating > 0 ? user.getFloatRating() / 2.0f : floatRating);
//        }
        if(user.getProfilePicture() != null) {
            ivProfile.setParseFile(user.getProfilePicture());
            ivProfile.loadInBackground();
        }
        if(user.getUsername() != null) {
            tvUsername.setText(user.getUsername());
        }
    }
}
