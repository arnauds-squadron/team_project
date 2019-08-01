package com.arnauds_squadron.eatup.chat.dashboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.utils.Constants;
import com.arnauds_squadron.eatup.utils.FormatHelper;
import com.bumptech.glide.Glide;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatDashboardAdapter extends RecyclerView.Adapter<ChatDashboardAdapter.ViewHolder> {

    private Context context;
    private List<Chat> chatList;
    private ChatDashboardFragment parentFragment;

    ChatDashboardAdapter(ChatDashboardFragment parent, List<Chat> chatList) {
        this.chatList = chatList;
        this.parentFragment = parent;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View postView = LayoutInflater.from(context).inflate(R.layout.item_chat, viewGroup, false);
        return new ViewHolder(postView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        Chat chat = chatList.get(position);

        viewHolder.tvName.setText(chat.getName());
        viewHolder.tvUpdatedAt.setText(FormatHelper.formatTimestamp(chat.getUpdatedAt(), context));

        List<ParseUser> members = chat.getMembers();
        for (int i = members.size() - 1; i >= 0; i--) {
            if (members.get(i).getObjectId().equals(Constants.CURRENT_USER.getObjectId()))
                members.remove(i);
        }

        if (members.size() > 1) {
            viewHolder.ivImage.setVisibility(View.INVISIBLE);
            final ImageView[] imageViews = {viewHolder.ivSmallImage1, viewHolder.ivSmallImage2};

            for (int i = 0; i < imageViews.length; i++) {
                imageViews[i].setVisibility(View.VISIBLE);
                final int finalI = i;

                members.get(i).fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        ParseFile image = object.getParseFile("profilePicture");
                        Glide.with(context)
                                .load(image.getUrl())
                                .into(imageViews[finalI]);
                    }
                });
            }
        } else {
            viewHolder.ivImage.setVisibility(View.VISIBLE);
            viewHolder.ivSmallImage1.setVisibility(View.GONE);
            viewHolder.ivSmallImage2.setVisibility(View.GONE);
            ParseUser member = members.size() == 1 ? members.get(0) : Constants.CURRENT_USER;

            member.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    ParseFile image = object.getParseFile("profilePicture");
                    Glide.with(context)
                            .load(image.getUrl())
                            .into(viewHolder.ivImage);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivImage)
        ImageView ivImage;

        @BindView(R.id.ivSmallImage1)
        ImageView ivSmallImage1;

        @BindView(R.id.ivSmallImage2)
        ImageView ivSmallImage2;

        @BindView(R.id.tvName)
        TextView tvName;

        @BindView(R.id.tvUpdatedAt)
        TextView tvUpdatedAt;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Chat chat = chatList.get(getAdapterPosition());
                    parentFragment.openChat(chat);
                }
            });
        }
    }
}
