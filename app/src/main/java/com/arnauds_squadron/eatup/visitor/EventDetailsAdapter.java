package com.arnauds_squadron.eatup.visitor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.AVERAGE_RATING;
import static com.arnauds_squadron.eatup.utils.Constants.BIO;
import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.arnauds_squadron.eatup.utils.Constants.NO_RATING;


public class EventDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private Event event;
    private final int CARD_COUNT = 2;

    public EventDetailsAdapter(Context context, Event event) {
        this.context = context;
        this.event = event;
    }

    // separate viewholders for the host and the restaurant
    class HostViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivHostImage)
        ImageView ivHostImage;
        @BindView(R.id.tvHostName)
        TextView tvHostName;
        @BindView(R.id.hostRating)
        RatingBar hostRating;
        @BindView(R.id.tvHostDescription)
        TextView tvHostDescription;

        public HostViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class RestaurantViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivEventImage)
        ImageView ivEventImage;
        @BindView(R.id.tvLocation)
        TextView tvLocation;
        @BindView(R.id.locationRating)
        RatingBar locationRating;
        @BindView(R.id.tvAddress)
        TextView tvAddress;
        @BindView(R.id.tvPhone)
        TextView tvPhone;
        @BindView(R.id.tvURL)
        TextView tvURL;

        public RestaurantViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    // TODO fix this method
    @Override
    public int getItemCount() {
        return CARD_COUNT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case 0:
                View hostEventView = inflater.inflate(R.layout.event_details_host, parent, false);
                viewHolder = new HostViewHolder(hostEventView);
                break;
            case 1:
                View restaurantEventView = inflater.inflate(R.layout.event_details_location, parent, false);
                viewHolder = new RestaurantViewHolder(restaurantEventView);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            // populate views here
            case 0:
                HostViewHolder hostViewHolder = (HostViewHolder) holder;
                hostViewHolder.tvHostName.setText(event.getHost().getUsername());
                hostViewHolder.tvHostDescription.setText(event.getHost().getString(BIO));
                Number rating = event.getHost().getNumber(AVERAGE_RATING);
                if (rating != null) {
                    hostViewHolder.hostRating.setRating(rating.floatValue());
                }
                else {
                    hostViewHolder.hostRating.setRating(NO_RATING);
                }

                ParseFile profileImage = event.getHost().getParseFile(KEY_PROFILE_PICTURE);
                if (profileImage != null) {
                    Glide.with(context.getApplicationContext())
                            .load(profileImage.getUrl())
                            .centerCrop()
                            .into(hostViewHolder.ivHostImage);
                }
                break;
            case 1:
                RestaurantViewHolder restaurantViewHolder = (RestaurantViewHolder) holder;
                // TODO fill in the location of the event
                // TODO get rating, address, phone, URL for the restaurant from query to Yelp API

                // restaurantViewHolder.tvLocation.setText(event.getRestaurant());
//                if (rating != null) {
//                    restaurantViewHolder.locationRating.setRating(rating.floatValue());
//                }
//                else {
//                    restaurantViewHolder.locationRating.setRating(NO_RATING);
//                }

//                restaurantViewHolder.tvAddress.setText();
//                restaurantViewHolder.tvPhone.setText();
//                restaurantViewHolder.tvURL.setText();

                ParseFile eventImage = event.getEventImage();
                if (eventImage != null) {
                    Glide.with(context.getApplicationContext())
                            .load(eventImage.getUrl())
                            .centerCrop()
                            .into(restaurantViewHolder.ivEventImage);
                }
        }
    }
}
