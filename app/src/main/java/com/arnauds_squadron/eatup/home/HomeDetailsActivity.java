package com.arnauds_squadron.eatup.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.profile.ProfileActivity;
import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.yelp_api.YelpApiResponse;
import com.arnauds_squadron.eatup.yelp_api.YelpData;
import com.arnauds_squadron.eatup.yelp_api.YelpService;
import com.arnauds_squadron.eatup.models.Event;
import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class HomeDetailsActivity extends AppCompatActivity {

    Context context;
    Event event;
    @BindView(R.id.ivProfile)
    ParseImageView ivProfile;
    @BindView(R.id.cbRestaurant)
    CheckBox cbRestaurant;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_details);
        ButterKnife.bind(this);
        event = Parcels.unwrap(getIntent().getParcelableExtra(Event.class.getSimpleName()));
        context = getApplicationContext();
        //call the HomeDetailsActivity.apiAuth to get the Authorization and return a service for the ApiResponse
        // if we have a response, then get the specific information defined in the Business Class
        Call<YelpApiResponse> meetUp = YelpData.retrofit(context).getLocation(event.getAddress().getLatitude(), event.getAddress().getLongitude(), event.getCuisine(), 15);

        meetUp.enqueue(new Callback<YelpApiResponse>() {
            @Override
            public void onResponse(Call<YelpApiResponse> call, retrofit2.Response<YelpApiResponse> response) {
                if (response.isSuccessful()) {

                    YelpApiResponse yelpApiResponse = response.body();
                    tvYelp.setText(yelpApiResponse.businessList.get(0).name);
                    final String url = yelpApiResponse.businessList.get(0).url;
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
                                .load(yelpApiResponse.businessList.get(0).imageUrl)
                                .override(100,100)
                                .into(ivImage);
                }
            }
            @Override
            public void onFailure(Call<YelpApiResponse> call, Throwable t) {

            }
        });
        if(event.getTitle() != null) {
            tvTitle.setText(event.getTitle());
        }
        if(event.getEventImage() != null) {
            ivProfile.setParseFile(event.getEventImage());
            ivProfile.loadInBackground();
        }
        if(event.getCuisine() != null) {
            tvFood.setText(event.getCuisine());
        }
        //Todo get the username to appear
        if(event.getHost() != null) {
            try {
                tvPerson.setText(event.getHost().fetchIfNeeded().getUsername());
                final ParseUser user = event.getHost();
                tvPerson.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(HomeDetailsActivity.this, ProfileActivity.class);
                        i.putExtra("user",Parcels.wrap(user));
                        startActivity(i);
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(event.getOver21() != null) {
            if(event.getOver21()) {
                cbLegal.setChecked(true);
            }
        } else {
            cbLegal.setChecked(false);
        }
        if(event.getRestaurant() != null) {
            if(event.getRestaurant()) {
                cbRestaurant.setChecked(true);
            }
        } else {
            cbRestaurant.setChecked(false);
        }
    }
}
