package com.arnauds_squadron.eatup.home;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.User;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HomeDetailsActivity extends AppCompatActivity {

    Event event;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_details);
        ButterKnife.bind(this);

//        OkHttpClient client = new OkHttpClient();
//
//        Request request = new Request.Builder()
//                .url("https://api.yelp.com/v3/businesses/matches")
//                .build();
        //ParseUser username = ParseUser.getCurrentUser();
        event = Parcels.unwrap(getIntent().getParcelableExtra(Event.class.getSimpleName()));
//        user = Parcels.unwrap(getIntent().getParcelableExtra(User.class.getSimpleName()));

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
//        if(username.getUsername() != null) {
//            tvPerson.setText(username.getUsername());
//        }
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
