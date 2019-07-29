package com.arnauds_squadron.eatup.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.RateUserActivity;
import com.arnauds_squadron.eatup.home.requests.RequestAdapter;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.yelp_api.YelpApiResponse;
import com.arnauds_squadron.eatup.yelp_api.YelpData;
import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private List<Event> mAgenda;
    private Context context;
    private HomeFragment homeFragment;
    private List<ParseUser> requests;
    private RequestAdapter requestAdapter;


    HomeAdapter(Context context, HomeFragment homeFragment, List<Event> mAgenda) {
        this.context = context;
        this.homeFragment = homeFragment;
        this.mAgenda = mAgenda;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.item_agenda, viewGroup, false);
        return new ViewHolder(postView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        Event event = mAgenda.get(i);
        if (event.getDate() != null) {
            Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
            if (event.getDate() != null) {
                // event has already passed
                if (event.getDate().before(localCalendar.getTime())) {
                    viewHolder.tvDate.setTextColor(Color.RED);
                    // check if current user is the host
                    if (event.getHost().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        viewHolder.btnCancel.setText("Rate guests");
                    }
                    else {
                        viewHolder.btnCancel.setText("Rate host");
                    }
                }
                // event is in the future
                else {
                    viewHolder.tvDate.setTextColor(Color.BLACK);
                }
                viewHolder.tvDate.setText(event.getDate().toString());
            }
        }
            if (event.getTitle() != null) {
                viewHolder.tvTitle.setText(event.getTitle());
            }

            try {
                viewHolder.tvPlace.setText(event.getHost().fetchIfNeeded().getUsername());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Call<YelpApiResponse> meetUp = YelpData.retrofit(context).getLocation(event.getAddress().getLatitude(), event.getAddress().getLongitude(), event.getCuisine(), 15);
            meetUp.enqueue(new Callback<YelpApiResponse>() {
            @Override
            public void onResponse(Call<YelpApiResponse> call, retrofit2.Response<YelpApiResponse> response) {
                if (response.isSuccessful()) {

                    YelpApiResponse yelpApiResponse = response.body();
                    if(yelpApiResponse.businessList.size() > 0) {
                        Glide.with(context)
                                .load(yelpApiResponse.businessList.get(0).imageUrl)
                                .override(100,100)
                                .into(viewHolder.ivProfile);
                    }
                }
            }
            @Override
            public void onFailure(Call<YelpApiResponse> call, Throwable t) {

            }
        });

        if (event.getTitle() != null) {
            viewHolder.tvTitle.setText(event.getTitle());
        }
        if (event.getEventImage() != null) {
            viewHolder.ivImage.setParseFile(event.getEventImage());
            viewHolder.ivImage.loadInBackground();
        }

        try {
            viewHolder.tvPlace.setText(event.getHost().fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        requests = new ArrayList<>();
        requestAdapter = new RequestAdapter(event, requests);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setReverseLayout(true);
        viewHolder.rvRequests.setLayoutManager(layoutManager);
        viewHolder.rvRequests.setAdapter(requestAdapter);

        getPendingRequests(event);
    }

    @Override
    public int getItemCount() {
        return mAgenda.size();
    }

    /**
     * Queries the Parse Server to get the list of pending request for this particular event
     */
    private void getPendingRequests(Event event) {
        List<ParseUser> pending = event.getPendingRequests();
        if (pending != null && pending.size() > requests.size()) {
            requests.clear();
            requests.addAll(pending);
            requestAdapter.notifyItemRangeInserted(0, pending.size());
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivProfile)
        ImageView ivProfile;
        @BindView(R.id.ivImage)
        ParseImageView ivImage;

        @BindView(R.id.ibOpenChat)
        ImageButton ibOpenChat;

        @BindView(R.id.btnCancel)
        Button btnCancel;

        @BindView(R.id.tvDate)
        TextView tvDate;

        @BindView(R.id.tvTitle)
        TextView tvTitle;

        @BindView(R.id.tvPlace)
        TextView tvPlace;

        @BindView(R.id.rvRequests)
        RecyclerView rvRequests;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int eventPosition = getAdapterPosition();
                    Event event = mAgenda.get(eventPosition);

                    // if past event date, rate the attending users. otherwise cancel the event
                    Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
                    if (event.getDate() != null) {
                        if (event.getDate().before(localCalendar.getTime())) {
                            Intent i = new Intent(context, RateUserActivity.class);
                            i.putExtra("event", event);
                            context.startActivity(i);
                        }
                        // TODO add some sort of check to remove the user from the event in the Parse database
                        mAgenda.remove(eventPosition);
                        notifyItemRemoved(eventPosition);
                        notifyItemRangeChanged(eventPosition, mAgenda.size());
                    }
                }
            });

            ibOpenChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Event event = mAgenda.get(getAdapterPosition());
                    homeFragment.openChat(event.getChat());
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
                        context.startActivity(intent);
                    }
                }
            });
        }
    }
}
