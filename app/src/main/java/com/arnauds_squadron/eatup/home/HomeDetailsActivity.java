package com.arnauds_squadron.eatup.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.User;
import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeDetailsActivity extends AppCompatActivity {

    Event event;
    Context context;
    User user;
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

//        String secretValue = getString(R.string.parse_application_id);
        String secretKey = getString(R.string.yelp_api_key);

        final OkHttpClient client = new OkHttpClient();
        String accessToken=null;

        final Request request = new Request.Builder()
                // todo when we get the event.getAddress working
                //.url("https://api.yelp.com/v3/businesses/search?term=" + event.getCuisine() + "&location=" + event.getAddress() + "&limit=1&sort_by=rating&price=" + 1 +"")
                .url("https://api.yelp.com/v3/businesses/north-india-restaurant-san-francisco")
                .addHeader("Authorization", "Bearer " + secretKey)
                .build();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                Response response = client.newCall(request).execute();
                final JSONObject jsonObject = new JSONObject(response.body().string().trim());
                //JSONArray myResponse = (JSONArray)jsonObject.get("id");
                final String imageURL = jsonObject.getString("image_url");
                final String url = jsonObject.getString("url");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tvYelp.setText(jsonObject.getString("alias"));
                          Glide.with(HomeDetailsActivity.this)
                                 .load(imageURL)
                                 .override(100,100)
                                 .into(ivImage);
                            btnLink.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(url));
                                    startActivity(i);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException | JSONException e) {
                // TODO Auto-generated catch block
                Log.e("HomeDetailsActivity", "Didn't respond");
                e.printStackTrace();
            }
            }
        });
        thread.start();
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
