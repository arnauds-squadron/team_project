package com.arnauds_squadron.eatup.event_details;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventDetailsRestaurantActivity extends AppCompatActivity {

    Event event;
    @BindView(R.id.ivEventImage)
    ImageView ivEventImage;
    @BindView(R.id.tvRestaurantName)
    TextView tvRestaurantName;
    @BindView(R.id.restaurantRating)
    RatingBar restaurantRating;
    @BindView(R.id.tvAddress)
    TextView tvAddress;
    @BindView(R.id.tvPhone)
    TextView tvPhone;
    @BindView(R.id.tvURL)
    TextView tvURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_restaurant);
        ButterKnife.bind(this);

        event = getIntent().getParcelableExtra("event");

        // load eventImage
        tvRestaurantName.setText(event.getTitle());
        ParseFile eventImage = event.getEventImage();
        if (eventImage != null) {
            Glide.with(getApplicationContext())
                    .load(eventImage.getUrl())
                    .centerCrop()
                    .into(ivEventImage);
        }
        // TODO get all restaurant information from database and query to Yelp API

        // load restaurant rating
//        Number rating = user.getNumber(AVERAGE_RATING);
//        Number numRatings = user.getNumber(NUM_RATINGS);
//        if (rating != null) {
//            ratingBar.setRating(rating.floatValue());
//        }
//        else {
//            ratingBar.setRating(NO_RATING);
//        }
//        tvRatings.setText(String.format(Locale.getDefault(),"(%s)", numRatings.toString()));
    }
}

