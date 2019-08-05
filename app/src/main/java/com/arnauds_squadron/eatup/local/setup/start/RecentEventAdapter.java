package com.arnauds_squadron.eatup.local.setup.start;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecentEventAdapter extends RecyclerView.Adapter<RecentEventAdapter.ViewHolder> {

    private List<Event> events;
    private StartFragment startFragment;
    private Context context;

    RecentEventAdapter(StartFragment startFragment, List<Event> events) {
        this.events = events;
        this.startFragment = startFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.item_recent_event, viewGroup, false);
        return new ViewHolder(postView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        Event event = events.get(i);

        viewHolder.tvTitle.setText(event.getTitle());
        viewHolder.tvPlace.setText(event.getAddressString());

        if (event.getYelpImage() != null) {
            Glide.with(context)
                    .load(event.getYelpImage())
                    .into(viewHolder.ivEventImage);
        }
        if(event.getEventImage() != null)
            Glide.with(context)
                .load(event.getEventImage().getUrl())
                .into(viewHolder.ivEventImage);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder that shows a single request that the user has
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivEventImage)
        ImageView ivEventImage;

        @BindView(R.id.tvTitle)
        TextView tvTitle;

        @BindView(R.id.tvPlace)
        TextView tvPlace;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startFragment.useRecentEvent(events.get(getAdapterPosition()));
                }
            });
        }
    }
}
