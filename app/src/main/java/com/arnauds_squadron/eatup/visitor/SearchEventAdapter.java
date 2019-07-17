package com.arnauds_squadron.eatup.visitor;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.EventDetailsActivity;
import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchEventAdapter extends RecyclerView.Adapter<SearchEventAdapter.ViewHolder> {

    private List<Event> events;
    // context defined as global variable so Glide in onBindViewHolder has access
    private Context context;

    // pass event array in constructor
    public SearchEventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    // for each row, inflate layout and cache references into ViewHolder
    // method invoked only when creating a new row
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View eventView = inflater.inflate(R.layout.search_event_linear, parent, false);
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
        public ImageView ivEventImage;
        @BindView(R.id.tvEventName)
        public TextView tvEventName;
        @BindView(R.id.ratingBar)
        public RatingBar ratingBar;
        @BindView(R.id.tvCuisine)
        public TextView tvCuisine;
        @BindView(R.id.tvDistance)
        public TextView tvDistance;
        @BindView(R.id.btBook)
        public Button btBook;

        // constructor takes in inflated layout
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);

            btBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO send request to Parse server with request to make reservation
                }
            });
        }

        public void bind(Event event) {
            // populate views according to data
            tvEventName.setText(event.getTitle());
            // TODO set rating bar, cuisine
            // TODO calculate distance
            ParseFile eventImage = event.getEventImage();
            if (eventImage != null) {
                Glide.with(context)
                        .load(eventImage.getUrl())
                        .centerCrop()
                        .into(ivEventImage);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            // ensure position valid (exists in view)
            if (position != RecyclerView.NO_POSITION) {
                Log.d("eventAdapter", "View event Details");
                Event event = events.get(position);

                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra("event_id", event.getObjectId());
                context.startActivity(intent);
            }
        }
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
