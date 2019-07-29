package com.arnauds_squadron.eatup.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Business;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.profile.ProfileActivity;
import com.arnauds_squadron.eatup.yelp_api.YelpApiResponse;
import com.arnauds_squadron.eatup.yelp_api.YelpData;
import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;

public class HomeDetailsActivity extends AppCompatActivity {

    @BindView(R.id.rbYelp)
    RatingBar rbYelp;

    @BindView(R.id.ivProfile)
    ParseImageView ivProfile;

    @BindView(R.id.cbLegal)
    CheckBox cbLegal;

    @BindView(R.id.tvTitle)
    TextView tvTitle;

    @BindView(R.id.tvPlace)
    TextView tvPlace;

    @BindView(R.id.tvPerson)
    TextView tvPerson;

    @BindView(R.id.tvFood)
    TextView tvFood;

    @BindView(R.id.tvYelp)
    TextView tvYelp;

    @BindView(R.id.btnLink)
    Button btnLink;

    @BindView(R.id.ivImage)
    ImageView ivImage;

    private Event event;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_details);
        ButterKnife.bind(this);
        event = Parcels.unwrap(getIntent().getParcelableExtra(Event.class.getSimpleName()));
        // Using retrofit to call the YelpApiRepsonse on  the events Cuisine and geopoint location
        // then checking for a response if we have a response, then get the specific information
        // defined in the Business Class
        context = getApplicationContext();
        //call the HomeDetailsActivity.apiAuth to get the Authorization and return a service for the
        // ApiResponse if we have a response, then get the specific information defined in the
        // Business Class
        Call<YelpApiResponse> meetUp = YelpData.retrofit(context).getLocation(
                event.getAddress().getLatitude(), event.getAddress().getLongitude(),
                event.getTags().get(0));

        meetUp.enqueue(new Callback<YelpApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<YelpApiResponse> call,
                                   @NonNull retrofit2.Response<YelpApiResponse> response) {
                if (response.isSuccessful()) {

                    YelpApiResponse yelpApiResponse = response.body();
                    if (yelpApiResponse != null) {
                        Business restaurant = yelpApiResponse.businessList.get(0);
                        tvYelp.setText(restaurant.name);
                        final String url = restaurant.url;
                        btnLink.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                                finish();
                            }
                        });
                        Glide.with(HomeDetailsActivity.this)
                                .load(restaurant.imageUrl)
                                .into(ivImage);
                        rbYelp.setRating(restaurant.rating);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<YelpApiResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
        if (event.getTitle() != null) {
            tvTitle.setText(event.getTitle());
        }
        if (event.getEventImage() != null) {
            ivProfile.setParseFile(event.getEventImage());
            ivProfile.loadInBackground();
        }
        if (event.getCuisine() != null) {
            tvFood.setText(event.getCuisine());
        }
        //Todo get the username to appear
        if (event.getHost() != null) {
            try {
                tvPerson.setText(event.getHost().fetchIfNeeded().getUsername());
                final ParseUser user = event.getHost();
                tvPerson.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(HomeDetailsActivity.this, ProfileActivity.class);
                        i.putExtra("user",user);
                        startActivity(i);
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (event.getOver21() != null) {
            if (event.getOver21()) {
                cbLegal.setChecked(true);
            }
        } else {
            cbLegal.setChecked(false);
        }
    }
}
