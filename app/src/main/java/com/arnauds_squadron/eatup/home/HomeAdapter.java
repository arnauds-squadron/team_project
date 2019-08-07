package com.arnauds_squadron.eatup.home;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.MainActivity;
import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.RateUserActivity;
import com.arnauds_squadron.eatup.home.requests.RequestAdapter;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.FormatHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.CHANNEL_ID;
import static com.arnauds_squadron.eatup.utils.Constants.GUEST;
import static com.arnauds_squadron.eatup.utils.Constants.HOST;
import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.arnauds_squadron.eatup.utils.FormatHelper.formatDateDay;
import static com.arnauds_squadron.eatup.utils.FormatHelper.formatDateMonth;
import static com.arnauds_squadron.eatup.utils.FormatHelper.formatTime;

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

        viewHolder.divider.setVisibility(View.INVISIBLE);

        if (event.getDate() != null) {
            Date date = new Date();
            // event has not passed
            if (date.after(event.getDate())) {
                // check if current user is the host
                if (event.getHost().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                    if(event.getAcceptedGuestsList() != null) {
                        if(event.getAcceptedGuestsList().size() != 0) {
                            viewHolder.btnCancel.setText("Rate guests");
                            viewHolder.btnCancel.setTag(GUEST);
                        }
                    }
                    else {
                        viewHolder.btnCancel.setVisibility(View.INVISIBLE);
                    }
                }
                else {
                    viewHolder.btnCancel.setText("Rate host");
                    viewHolder.btnCancel.setTag(HOST);
                }
            }
            viewHolder.tvDay.setText(formatDateDay(event.getDate()));
            viewHolder.tvMonth.setText(formatDateMonth(event.getDate()));
            viewHolder.tvTime.setText(formatTime(event.getDate(), context));
        }
        viewHolder.tvTitle.setText(event.getTitle());
        viewHolder.tvAddress.setText(event.getAddressString());

        // Display requests if the current user is the host of this event
        if (event.getHost().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            requests = new ArrayList<>();
            requestAdapter = new RequestAdapter(event, requests);
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            layoutManager.setReverseLayout(true);
            viewHolder.rvRequests.setVisibility(View.VISIBLE);
            viewHolder.rvRequests.setLayoutManager(layoutManager);
            viewHolder.rvRequests.setAdapter(requestAdapter);

            getPendingRequests(event, viewHolder);
        }

        event.getHost().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                ParseFile image = object.getParseFile("profilePicture");

                if (image != null) {
                    Glide.with(context)
                            .load(object.getParseFile(KEY_PROFILE_PICTURE).getUrl())
                            .transform(new CircleCrop())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(viewHolder.ivProfileImage);
                } else {
                    Glide.with(context)
                            .load(FormatHelper.getProfilePlaceholder(context))
                            .transform(new CircleCrop())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(viewHolder.ivProfileImage);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAgenda.size();
    }

    /**
     * Queries the Parse Server to get the list of pending request for this particular event
     */

    private void getPendingRequests(Event event, ViewHolder viewHolder) {
        List<ParseUser> pending = event.getPendingRequests();
        String eventTitle = event.getTitle();
        if (pending != null && pending.size() > requests.size()) {
            requests.clear();
            requests.addAll(pending);

            if(requests.size() != 0) {
                viewHolder.divider.setVisibility(View.VISIBLE);
            } else {
                viewHolder.divider.setVisibility(View.INVISIBLE);
            }

            requestAdapter.notifyItemRangeInserted(0, pending.size());

            // create notifications for each of the pending requests
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            for (int i = 0; i < pending.size(); i++) {
                // notificationId is a unique int for each notification that you must define
                int notificationId = i;
                String contentText = String.format(Locale.getDefault(), "You have a new request to join %s!", eventTitle);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_toast_logo)
                        .setContentTitle("New request to join event")
                        .setContentText(contentText)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);
                notificationManager.notify(notificationId, builder.build());
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvDay)
        TextView tvDay;

        @BindView(R.id.tvMonth)
        TextView tvMonth;

        @BindView(R.id.tvTime)
        TextView tvTime;

        @BindView(R.id.ivProfileImage)
        ImageView ivProfileImage;

        @BindView(R.id.tvEventTitle)
        TextView tvTitle;

        @BindView(R.id.tvRestaurant)
        TextView tvRestaurant;

        @BindView(R.id.constraintLayoutAddress)
        ConstraintLayout constraintLayoutAddress;

        @BindView(R.id.tvAddress)
        TextView tvAddress;

        @BindView(R.id.btnChat)
        Button btnChat;

        @BindView(R.id.btnCancel)
        Button btnCancel;

        @BindView(R.id.rvRequests)
        RecyclerView rvRequests;

        @BindView(R.id.divider)
        View divider;

        ViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int eventPosition = getAdapterPosition();
                    final Event event = mAgenda.get(eventPosition);

                    // if past event date, rate the attending users. otherwise cancel the event
                    Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
                    if (event.getDate() != null) {
                        if (event.getDate().after(localCalendar.getTime())) {
                            Intent i = new Intent(context, RateUserActivity.class);
                            i.putExtra("event", event);
                            i.putExtra("ratingType", (String) btnCancel.getTag());
                            context.startActivity(i);
                        }
                        // TODO add some sort of check to remove the user from the event in the Parse database
                        mAgenda.remove(eventPosition);
                        notifyItemRemoved(eventPosition);
                        notifyItemRangeChanged(eventPosition, mAgenda.size());
                        Snackbar snackbar = Snackbar.make(itemView, "DELETED!", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mAgenda.add(eventPosition, event);
                                        notifyDataSetChanged();
                                    }
                                });
                        snackbar.setActionTextColor(Color.YELLOW);
                        snackbar.show();
                    }
                }
            });

            btnChat.setOnClickListener(new View.OnClickListener() {
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