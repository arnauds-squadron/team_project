package com.arnauds_squadron.eatup.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Business;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.Location;
import com.arnauds_squadron.eatup.profile.HostProfileActivity;
import com.arnauds_squadron.eatup.yelp_api.YelpApiResponse;
import com.arnauds_squadron.eatup.yelp_api.YelpData;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;

public class HomeDetailsActivity extends AppCompatActivity {

    @BindView(R.id.rbYelp)
    RatingBar rbYelp;

    @BindView(R.id.ivProfile)
    ParseImageView ivProfile;

    @BindView(R.id.ivImage)
    ImageView ivImage;

    @BindView(R.id.cbLegal)
    CheckBox cbLegal;

    @BindView(R.id.tvEventTitle)
    TextView tvTitle;

    @BindView(R.id.tvAddress)
    TextView tvPlace;

    @BindView(R.id.tvPerson)
    TextView tvPerson;

    @BindView(R.id.tvFood)
    TextView tvFood;

    @BindView(R.id.tvYelp)
    TextView tvYelp;

    @BindView(R.id.ivLink)
    ImageView ivLink;

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
        //call the HomeDetailsActivity.apiAuth to get the Authorization and return a service for the ApiResponse
        // if we have a response, then get the specific information defined in the Business Class
        Call<YelpApiResponse> meetUp = null;
        if (event.getTags() != null){
            YelpData.retrofit(context).getLocation(event.getAddress().getLatitude(), event.getAddress().getLongitude(), event.getTags().get(0), 50);
        } else {
            YelpData.retrofit(context).getLocation(event.getAddress().getLatitude(), event.getAddress().getLongitude(), event.getCuisine(), 50);
        }
        //call the HomeDetailsActivity.apiAuth to get the Authorization and return a service for the
        // ApiResponse if we have a response, then get the specific information defined in the
        // Business Class
        meetUp = YelpData.retrofit(context).getLocation(
                event.getAddress().getLatitude(), event.getAddress().getLongitude(),
                event.getTags().get(0), 50);

        meetUp.enqueue(new Callback<YelpApiResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<YelpApiResponse> call,
                                   @NonNull retrofit2.Response<YelpApiResponse> response) {
                if (response.isSuccessful()) {

                    YelpApiResponse yelpApiResponse = response.body();
                    if (yelpApiResponse != null && yelpApiResponse.businessList.size() > 0) {
                        Business restaurant = yelpApiResponse.businessList.get(0);
                        Location location = restaurant.location;
                        //tvAddress.setText(location.getAddress1() + " " + location.getCity() + "," + location.getState() + " " + location.getZipCode());
                        tvPlace.setText(event.getAddressString());
                        tvYelp.setText(restaurant.name);
                        final String url = restaurant.url;

                        ivLink.setOnClickListener(new View.OnClickListener() {
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

        //load the profile image
        ParseUser parseUser = event.getHost();
        File parseFile = null;
        if (parseUser.equals(ParseUser.getCurrentUser()) && ParseUser.getCurrentUser().getParseFile("profilePicture") != null) {
            try {
                parseFile = ParseUser.getCurrentUser().getParseFile("profilePicture").getFile();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                if (parseUser.fetchIfNeeded().getParseFile("profilePicture") != null){
                    try {
                        parseFile = parseUser.getParseFile("profilePicture").getFile();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        ivProfile.loadInBackground();
        Glide.with(context)
                .load(parseFile)
                .transform(new CircleCrop())
                .into(ivProfile);

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
                        Intent i = new Intent(HomeDetailsActivity.this, HostProfileActivity.class);
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
