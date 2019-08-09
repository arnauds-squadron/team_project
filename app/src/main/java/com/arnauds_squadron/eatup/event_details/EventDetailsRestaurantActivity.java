package com.arnauds_squadron.eatup.event_details;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Business;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.yelp_api.YelpData;
import com.bumptech.glide.Glide;

import org.parceler.Parcels;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsRestaurantActivity extends AppCompatActivity {

    @BindView(R.id.ivEventImage)
    ImageView ivEventImage;
    @BindView(R.id.tvRestaurantName)
    TextView tvRestaurantName;
    @BindView(R.id.restaurantRating)
    RatingBar restaurantRating;
    @BindView(R.id.tvNumberRatings)
    TextView tvNumberRatings;
    @BindView(R.id.tvPhone)
    TextView tvPhone;
    @BindView(R.id.tvPrice)
    TextView tvPrice;
    @BindView(R.id.tvAddress)
    TextView tvAddress;
    @BindView(R.id.btYelp)
    ImageView btYelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_restaurant);
        ButterKnife.bind(this);

        Event event = Parcels.unwrap(getIntent().getParcelableExtra("event"));
        Business restaurant = Parcels.unwrap(getIntent().getParcelableExtra("business"));

        tvAddress.setText(event.getAddressString());
        Glide.with(EventDetailsRestaurantActivity.this)
                .load(event.getYelpImage())
                .into(ivEventImage);

        tvRestaurantName.setText(restaurant.name);
        restaurantRating.setRating(restaurant.rating);
        tvNumberRatings.setText(String.format(Locale.getDefault(), "(%s)", Integer.toString(restaurant.reviewCount)));
        tvPhone.setText(restaurant.displayPhone);


        final String url = restaurant.url;
        btYelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                finish();
            }
        });
        tvPrice.setText(restaurant.price);
    }
}

