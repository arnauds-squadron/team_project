package com.arnauds_squadron.eatup.event_details;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Business;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.Rating;
import com.arnauds_squadron.eatup.profile.ProfileActivity;
import com.arnauds_squadron.eatup.yelp_api.YelpData;
import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.arnauds_squadron.eatup.utils.Constants.AVG_RATING_HOST;
import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.arnauds_squadron.eatup.utils.Constants.NO_RATING;


public class EventDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity context;
    private Event event;
    private RecyclerView recyclerView;
    private final static int CARD_COUNT = 2;

    // TODO implement restaurant detail view when parse database set up

    EventDetailsAdapter(Activity context, Event event, RecyclerView recyclerView) {
        this.context = context;
        this.event = event;
        this.recyclerView = recyclerView;
    }

    // separate ViewHolders for the host and the restaurant
    class HostViewHolder extends RecyclerView.ViewHolder {
        final View rootView;
        @BindView(R.id.ivHostImage)
        ImageView ivHostImage;
        @BindView(R.id.tvHostName)
        TextView tvHostName;
        @BindView(R.id.hostRating)
        RatingBar hostRating;

        HostViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            rootView = itemView;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Event event = (Event) v.getTag();
                    Intent i = new Intent(context, ProfileActivity.class);
                    i.putExtra("user", event.getHost());
                    Pair<View, String> imagePair = Pair.create((View) ivHostImage, "profileImage");
                    Pair<View, String> namePair = Pair.create((View) tvHostName, "hostName");
                    Pair<View, String> ratingPair = Pair.create((View) hostRating, "hostRating");
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(context, imagePair, namePair, ratingPair);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i, options.toBundle());
                }
            });
        }

        void setEventRootViewTag(Event event) {
            rootView.setTag(event);
        }
    }

    class RestaurantViewHolder extends RecyclerView.ViewHolder {
        final View rootView;
        @BindView(R.id.ivEventImage)
        ImageView ivEventImage;
        @BindView(R.id.tvRestaurantName)
        TextView tvRestaurantName;
        @BindView(R.id.restaurantRating)
        RatingBar restaurantRating;

        RestaurantViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            rootView = itemView;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Business business = (Business) v.getTag();
                    Intent i = new Intent(context, EventDetailsRestaurantActivity.class);
                    i.putExtra("restaurant", Parcels.wrap(business));
                    Pair<View, String> imagePair = Pair.create((View) ivEventImage, "eventImage");
                    Pair<View, String> namePair = Pair.create((View) tvRestaurantName, "restaurantName");
                    Pair<View, String> ratingPair = Pair.create((View) restaurantRating, "restaurantRating");
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(context, imagePair, namePair, ratingPair);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i, options.toBundle());
                }
            });
        }

        void setBusinessRootViewTag(Business business) {
            rootView.setTag(business);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return CARD_COUNT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(context);
        int width = recyclerView.getWidth();
        switch (viewType) {
            case 0:
                View hostEventView = inflater.inflate(R.layout.event_details_host, parent, false);
                ViewGroup.LayoutParams hostParams = hostEventView.getLayoutParams();
                hostParams.width = (int)(width * 0.9);
                hostEventView.setLayoutParams(hostParams);
                viewHolder = new HostViewHolder(hostEventView);
                break;
            case 1:
                View restaurantEventView = inflater.inflate(R.layout.event_details_restaurant, parent, false);
                ViewGroup.LayoutParams restParams = restaurantEventView.getLayoutParams();
                restParams.width = (int)(width * 0.9);
                restaurantEventView.setLayoutParams(restParams);
                viewHolder = new RestaurantViewHolder(restaurantEventView);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case 0:
                final HostViewHolder hostViewHolder = (HostViewHolder) holder;
                hostViewHolder.setEventRootViewTag(event);
                hostViewHolder.tvHostName.setText(event.getHost().getUsername());
                ParseFile profileImage = event.getHost().getParseFile(KEY_PROFILE_PICTURE);
                if (profileImage != null) {
                    Glide.with(context.getApplicationContext())
                            .load(profileImage.getUrl())
                            .centerCrop()
                            .into(hostViewHolder.ivHostImage);
                }

                // load user rating
                ParseQuery<Rating> query = new Rating.Query();
                query.whereEqualTo("user", event.getHost());
                query.findInBackground(new FindCallback<Rating>() {
                    public void done(List<Rating> ratings, ParseException e) {
                        if (e == null) {
                            if(ratings.size() != 0) {
                                Rating rating = ratings.get(0);
                                float averageRating = rating.getAvgRatingHost().floatValue();
                                hostViewHolder.hostRating.setRating(averageRating);
                            }
                            else {
                                hostViewHolder.hostRating.setRating(NO_RATING);
                            }
                        } else {
                            Toast.makeText(context, "Query for rating not successful", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
            case 1:
                final RestaurantViewHolder restaurantViewHolder = (RestaurantViewHolder) holder;

                Call<Business> restaurantDetails = YelpData.retrofit(context).getDetails(event.getYelpId());
                restaurantDetails.enqueue(new Callback<Business>() {
                    @Override
                    public void onResponse(@NonNull Call<Business> call,
                                           @NonNull Response<Business> response) {
                        if (response.isSuccessful()) {
                            Business restaurant = response.body();
                            restaurantViewHolder.setBusinessRootViewTag(restaurant);
                            if (restaurant != null) {
                                restaurantViewHolder.tvRestaurantName.setText(restaurant.name);
                                Glide.with(context.getApplicationContext())
                                        .load(restaurant.imageUrl)
                                        .centerCrop()
                                        .into(restaurantViewHolder.ivEventImage);
                                restaurantViewHolder.restaurantRating.setRating(restaurant.rating);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Business> call, @NonNull Throwable t) {
                        t.printStackTrace();
                    }
                });
        }
    }
}
