package com.arnauds_squadron.eatup.chat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.home.HomeDetailsActivity;
import com.arnauds_squadron.eatup.models.Event;
import com.parse.ParseImageView;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatDashboardAdapter extends RecyclerView.Adapter<ChatDashboardAdapter.ViewHolder> {

    ArrayList<Event>mAgenda;
    Context context;

    public ChatDashboardAdapter(ArrayList<Event> mAgenda) {
        this.mAgenda = mAgenda;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.item_agenda, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(postView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Event event = mAgenda.get(i);
        if(event.getDate() != null) {
            viewHolder.tvDate.setText(event.getDate().toString());
        }
        if(event.getTitle() != null) {
            viewHolder.tvTitle.setText(event.getTitle());
        }
        if(event.getEventImage() != null) {
            viewHolder.ivProfile.setParseFile(event.getEventImage());
            viewHolder.ivProfile.loadInBackground();
        }
 //       viewHolder.tvPlace.setText(event.getAddress());

    }

    @Override
    public int getItemCount() {
        return mAgenda.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivProfile)
        ParseImageView ivProfile;
        @BindView(R.id.btnCancel)
        Button btnCancel;
        @BindView(R.id.tvDate)
        TextView tvDate;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvPlace)
        TextView tvPlace;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAgenda.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    notifyItemRangeChanged(getAdapterPosition(), mAgenda.size());
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Event event = mAgenda.get(position);
                        Intent intent = new Intent(context, HomeDetailsActivity.class);
                        intent.putExtra(Event.class.getSimpleName(), Parcels.wrap(event));
                        context.startActivities(new Intent[]{intent});
                    }
                }
            });
        }
    }
}
