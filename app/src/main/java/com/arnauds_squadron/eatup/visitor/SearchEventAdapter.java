package com.arnauds_squadron.eatup.visitor;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.event_details.EventDetailsActivity;
import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchEventAdapter extends RecyclerView.Adapter<SearchEventAdapter.ViewHolder> {

    private List<Event> events;
    // context defined as global variable so Glide in onBindViewHolder has access
    private Context context;
    private ParseGeoPoint userLocation;

    // pass event array in constructor
    public SearchEventAdapter(Context context, List<Event> events, ParseGeoPoint userLocation) {
        this.context = context;
        this.events = events;
        this.userLocation = userLocation;
    }

    // for each row, inflate layout and cache references into ViewHolder
    // method invoked only when creating a new row
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View eventView = inflater.inflate(R.layout.search_event_grid, parent, false);
        return new ViewHolder(eventView);
    }

    // bind values based on element position
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get data according to position
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    // create ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.ivEventImage)
        ImageView ivEventImage;
        @BindView(R.id.tvEventName)
        TextView tvEventName;
        @BindView(R.id.tvDate)
        TextView tvDate;
        @BindView(R.id.tvTags)
        TextView tvCuisine;
        @BindView(R.id.tvDistance)
        TextView tvDistance;
        @BindView(R.id.layoutOver21)
        ConstraintLayout layoutOver21;

        // constructor takes in inflated layout
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(Event event) {
            // populate views according to data
            ParseGeoPoint eventAddress = event.getAddress();
            double distanceInMiles = eventAddress.distanceInMilesTo(userLocation);

            tvDistance.setText(String.format(Locale.getDefault(), "%.2f mi", distanceInMiles));
            tvDistance.setTag(distanceInMiles);
            tvEventName.setText(event.getTitle());
            tvDate.setText(event.getDateString(context));

            List<String> cuisineTags = event.getTags();
            tvCuisine.setText(android.text.TextUtils.join(", ", cuisineTags));

            ParseFile eventImage = event.getEventImage();
            if (eventImage != null) {
                Glide.with(context)
                        .load(eventImage.getUrl())
                        .centerCrop()
                        .into(ivEventImage);
            }

            if(event.getOver21()) {
                layoutOver21.setVisibility(View.VISIBLE);
            } else {
                layoutOver21.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            // ensure position valid (exists in view)
            if (position != RecyclerView.NO_POSITION) {
                Log.d("eventAdapter", "View event details");
                Event event = events.get(position);

                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra("event_id", event.getObjectId());
                intent.putExtra("distance", (Double) v.getRootView().findViewById(R.id.tvDistance).getTag());
                context.startActivity(intent);
            }
        }
    }

    void updateCurrentLocation(ParseGeoPoint userLocation) {
        this.userLocation = userLocation;
    }

    // RecyclerView adapter helper methods to clear items from or add items to underlying dataset
    // clean recycler elements
    public void clear() {
        events.clear();
        notifyDataSetChanged();
    }

    // add list of events - change list type depending on item type used
    public void addAll(List<Event> list) {
        events.addAll(list);
        notifyDataSetChanged();
    }
}
