package com.arnauds_squadron.eatup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.EventDetailsRestaurantActivity;
import com.arnauds_squadron.eatup.ProfileActivity;
import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.AVERAGE_RATING;
import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.arnauds_squadron.eatup.utils.Constants.NO_RATING;


public class EventDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private Context context;
    private Event event;
    private RecyclerView recyclerView;
    private final int CARD_COUNT = 2;

    public EventDetailsAdapter(Context context, Event event, RecyclerView recyclerView) {
        this.context = context;
        this.event = event;
        this.recyclerView = recyclerView;
    }

    // separate viewholders for the host and the restaurant
    class HostViewHolder extends RecyclerView.ViewHolder {
        final View rootView;
        @BindView(R.id.ivHostImage)
        ImageView ivHostImage;
        @BindView(R.id.tvHostName)
        TextView tvHostName;
        @BindView(R.id.hostRating)
        RatingBar hostRating;

        public HostViewHolder(View itemView) {
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
                            makeSceneTransitionAnimation((Activity) context, imagePair, namePair, ratingPair);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i, options.toBundle());
                }
            });
        }

        void setRootViewTag(Event event) {
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

        public RestaurantViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            rootView = itemView;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Event event = (Event) v.getTag();
                    Intent i = new Intent(context, EventDetailsRestaurantActivity.class);
                    i.putExtra("event", event);
                    Pair<View, String> imagePair = Pair.create((View) ivEventImage, "eventImage");
                    Pair<View, String> namePair = Pair.create((View) tvRestaurantName, "restaurantName");
                    Pair<View, String> ratingPair = Pair.create((View) restaurantRating, "restaurantRating");
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((Activity) context, imagePair, namePair, ratingPair);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i, options.toBundle());
                }
            });
        }

        void setRootViewTag(Event event) {
            rootView.setTag(event);
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case 0:
                HostViewHolder hostViewHolder = (HostViewHolder) holder;
                hostViewHolder.setRootViewTag(event);
                hostViewHolder.tvHostName.setText(event.getHost().getUsername());
                ParseFile profileImage = event.getHost().getParseFile(KEY_PROFILE_PICTURE);
                if (profileImage != null) {
                    Glide.with(context.getApplicationContext())
                            .load(profileImage.getUrl())
                            .centerCrop()
                            .into(hostViewHolder.ivHostImage);
                }
                Number rating = event.getHost().getNumber(AVERAGE_RATING);
                if (rating != null) {
                    hostViewHolder.hostRating.setRating(rating.floatValue());
                }
                else {
                    hostViewHolder.hostRating.setRating(NO_RATING);
                }
                break;
            case 1:
                RestaurantViewHolder restaurantViewHolder = (RestaurantViewHolder) holder;
                restaurantViewHolder.setRootViewTag(event);
                restaurantViewHolder.tvRestaurantName.setText(event.getTitle());
                ParseFile eventImage = event.getEventImage();
                if (eventImage != null) {
                    Glide.with(context.getApplicationContext())
                            .load(eventImage.getUrl())
                            .centerCrop()
                            .into(restaurantViewHolder.ivEventImage);
                }
                // TODO get rating and restaurant name
        }
    }
}
