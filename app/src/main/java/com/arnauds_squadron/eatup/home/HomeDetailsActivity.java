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
import com.parse.ParseImageView;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeDetailsActivity extends AppCompatActivity {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_details);
        ButterKnife.bind(this);

        event = Parcels.unwrap(getIntent().getParcelableExtra(Event.class.getSimpleName()));
        if(event.getTitle() != null) {
            tvTitle.setText(event.getTitle());
        }
        if(event.getEventImage() != null) {
            ivProfile.setParseFile(event.getEventImage());
            ivProfile.loadInBackground();
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
