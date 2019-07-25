package com.arnauds_squadron.eatup.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.models.Event;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private ArrayList<Event> mAgenda;
    private Context context;
    private HomeFragment homeFragment;

    HomeAdapter(HomeFragment homeFragment, ArrayList<Event> mAgenda) {
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
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Event event = mAgenda.get(i);
        if (event.getDate() != null) {
            Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
            if (event.getDate() != null) {
                if (event.getDate().before(localCalendar.getTime())) {
                    viewHolder.tvDate.setTextColor(Color.RED);
                } else {
                    viewHolder.tvDate.setTextColor(Color.BLACK);
                }
                viewHolder.tvDate.setText(event.getDate().toString());
            }
            if (event.getTitle() != null) {
                viewHolder.tvTitle.setText(event.getTitle());
            }
            if (event.getEventImage() != null) {
                viewHolder.ivProfile.setParseFile(event.getEventImage());
                viewHolder.ivProfile.loadInBackground();
            }
            //       viewHolder.tvPlace.setText(event.getAddress());
        }
    }

    @Override
    public int getItemCount() {
        return mAgenda.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivProfile)
        ParseImageView ivProfile;

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

        ViewHolder(@NonNull View itemView) {
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

            ibOpenChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Event event = mAgenda.get(getAdapterPosition());
                    final Chat chat = event.getChat() == null ? new Chat() : event.getChat();

                    chat.setName(event.getTitle() + " Chat");
                    if (event.getEventImage() != null) {
                        chat.setImage(event.getEventImage());
                    }
                    // TODO: move get current user to new thread
                    chat.addMember(ParseUser.getCurrentUser());
                    // TODO: add accepted guests
                    //newChat.addMembers(event.get);

                    chat.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) { // Register chat to the current event
                                event.addChat(chat);
                                event.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            // Must save chat first before opening otherwise
                                            // we get an IllegalStateException
                                            homeFragment.openChat(chat);
                                        } else {
                                            Log.e("HomeAdapter", "Could not save the chat");
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } else {
                                Log.e("HomeAdapter", "Could not save the chat");
                                e.printStackTrace();
                            }
                        }
                    });
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
