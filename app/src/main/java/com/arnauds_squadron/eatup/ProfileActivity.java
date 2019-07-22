package com.arnauds_squadron.eatup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.models.Event;
import com.parse.ParseImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {

    Event event;
    @BindView(R.id.ivProfile)
    ParseImageView ivProfile;
    @BindView(R.id.etUsername)
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
    }
}
