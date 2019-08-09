package com.arnauds_squadron.eatup.home;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.RateUserActivity;
import com.arnauds_squadron.eatup.models.Business;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.Location;
import com.arnauds_squadron.eatup.profile.HostProfileActivity;
import com.arnauds_squadron.eatup.utils.Constants;
import com.arnauds_squadron.eatup.utils.FormatHelper;
import com.arnauds_squadron.eatup.yelp_api.YelpData;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.parse.DeleteCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;

import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.arnauds_squadron.eatup.utils.FormatHelper.formatDateDay;
import static com.arnauds_squadron.eatup.utils.FormatHelper.formatDateMonth;
import static com.arnauds_squadron.eatup.utils.FormatHelper.formatTime;

public class HomeDetailsActivity extends AppCompatActivity {

    @BindView(R.id.ivImage)
    ImageView ivImage;

    @BindView(R.id.tvDay)
    TextView tvDay;

    @BindView(R.id.tvMonth)
    TextView tvMonth;

    @BindView(R.id.tvTime)
    TextView tvTime;

    @BindView(R.id.tvEventTitle)
    TextView tvTitle;

    @BindView(R.id.ivHost)
    ImageView ivHost;

    @BindView(R.id.tvHost)
    TextView tvPerson;

    @BindView(R.id.tvRestaurant)
    TextView tvRestaurant;

    @BindView(R.id.rbYelp)
    RatingBar rbYelp;

    @BindView(R.id.tvCuisine)
    TextView tvCuisine;

    @BindView(R.id.tvAddress)
    TextView tvAddress;

    @BindView(R.id.clLegal)
    ConstraintLayout clLegal;

    @BindView(R.id.btnCancel)
    Button btnCancel;

//    @BindView(R.id.ivLink)
//    ImageView ivLink;

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_details);
        ButterKnife.bind(this);
        event = Parcels.unwrap(getIntent().getParcelableExtra(Event.class.getSimpleName()));

        // Using retrofit to call the YelpApiRepsonse on  the events Cuisine and geopoint location
        // then checking for a response if we have a response, then get the specific information
        // defined in the Business Class
        Context context = getApplicationContext();
        //call the HomeDetailsActivity.apiAuth to get the Authorization and return a service for the ApiResponse
        // if we have a response, then get the specific information defined in the Business Class
        Call<Business> meetUp;
        //call the HomeDetailsActivity.apiAuth to get the Authorization and return a service for the
        // ApiResponse if we have a response, then get the specific information defined in the
        // Business Class
        meetUp = YelpData.retrofit(context).getDetails(event.getYelpId());

        meetUp.enqueue(new Callback<Business>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<Business> call,
                                   @NonNull retrofit2.Response<Business> response) {
                if (response.isSuccessful()) {
                    Business business = response.body();
                    if (business != null) {
                        Location location = business.location;
                        //tvAddress.setText(location.getAddress1() + " " + location.getCity() + "," + location.getState() + " " + location.getZipCode());
                        final String url = business.url;

//                        ivLink.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent i = new Intent(Intent.ACTION_VIEW);
//                                i.setData(Uri.parse(url));
//                                startActivity(i);
//                                finish();
//                            }
//                        });
                        rbYelp.setRating(business.rating);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Business> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });

        tvDay.setText(formatDateDay(event.getDate()));
        tvMonth.setText(formatDateMonth(event.getDate()));
        tvTime.setText(formatTime(event.getDate(), context));

        if (event.getTitle() != null) {
            tvTitle.setText(event.getTitle());
        }

        tvAddress.setText(event.getAddressString());
        tvRestaurant.setText(event.getYelpRestaurant());
        Glide.with(HomeDetailsActivity.this)
                .load(event.getYelpImage())
                .into(ivImage);

        //load the profile image
        ParseUser parseUser = event.getHost();
        ParseFile profileImage = null;

        // load user profileImage
        if (parseUser.equals(Constants.CURRENT_USER)) {
            profileImage = Constants.CURRENT_USER.getParseFile(KEY_PROFILE_PICTURE);
        } else {
            try {
                profileImage = parseUser.fetchIfNeeded().getParseFile(KEY_PROFILE_PICTURE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (profileImage != null) {
            Glide.with(this)
                    .load(profileImage.getUrl())
                    .transform(new CircleCrop())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivHost);
        } else {
            Glide.with(this)
                    .load(FormatHelper.getProfilePlaceholder(this))
                    .transform(new CircleCrop())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivHost);
        }

        if (event.getTags() != null) {
            tvCuisine.setText(event.getTags().get(0));
        }

        if (event.getHost() != null) {
            try {
                tvPerson.setText(event.getHost().fetchIfNeeded().getUsername());
                final ParseUser user = event.getHost();
                tvPerson.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(HomeDetailsActivity.this, HostProfileActivity.class);
                        i.putExtra("user", user);
                        startActivity(i);
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (event.getOver21() != null) {
            if (event.getOver21()) {
                clLegal.setVisibility(View.VISIBLE);
            } else {
                clLegal.setVisibility(View.INVISIBLE);
            }
        }

        // check if the current user is the host of the event
        if(event.getHost().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            btnCancel.setText("Cancel event");
        } else {
            btnCancel.setText("Remove RSVP");
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete entire event if user is host
                if(event.getHost().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                    event.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null) {
                                Toast.makeText(getApplicationContext(), "Event deleted.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Error deleting event.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                // remove user from attending guest list otherwise
                else {
                    List<ParseUser> attendingUsers = event.getAcceptedGuestsList();
                    attendingUsers.remove(ParseUser.getCurrentUser());
                    event.setAcceptedGuestsList(attendingUsers);
                    event.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null) {
                                Toast.makeText(getApplicationContext(), "RSVP removed from event.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Error removing RSVP from event.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
