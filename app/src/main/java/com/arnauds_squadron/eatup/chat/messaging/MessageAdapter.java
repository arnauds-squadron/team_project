package com.arnauds_squadron.eatup.chat.messaging;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Message;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messages;
    private Context context;
    private ParseUser currentUser;

    MessageAdapter(Context context, ParseUser currentUser, List<Message> messages) {
        this.messages = messages;
        this.currentUser = currentUser;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_message, parent, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.tvContent.setText(message.getContent());
        boolean isMe = message.getSender().getObjectId().equals(currentUser.getObjectId());

        ConstraintSet newSet = new ConstraintSet();
        newSet.clone(holder.constraintLayout);

        int layoutId = holder.constraintLayout.getId();
        int imageId = holder.ivProfileMe.getId();
        int textId = holder.tvContent.getId();

        // Change the anchoring of the profile image and text depending on who sent the message
        if (isMe) {
            newSet.clear(imageId, ConstraintSet.START);
            newSet.clear(textId, ConstraintSet.START);
            newSet.connect(imageId, ConstraintSet.END, layoutId, ConstraintSet.END, 8);
            newSet.connect(textId, ConstraintSet.END, imageId, ConstraintSet.START, 24);
        } else {
            newSet.clear(imageId, ConstraintSet.END);
            newSet.clear(textId, ConstraintSet.END);
            newSet.connect(imageId, ConstraintSet.START, layoutId, ConstraintSet.START, 8);
            newSet.connect(textId, ConstraintSet.START, imageId, ConstraintSet.END, 24);
        }
        holder.constraintLayout.setConstraintSet(newSet);

        message.getSender().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                ParseFile profilePicture = object.getParseFile("profilePicture");
                if (profilePicture != null) {
                    Glide.with(context)
                            .load(profilePicture.getUrl())
                            .transform(new CircleCrop())
                            .into(holder.ivProfileMe);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.constraintLayout)
        ConstraintLayout constraintLayout;

        //@BindView(R.id.ivProfileOther)
        //ImageView ivProfileOther;

        @BindView(R.id.ivProfileMe)
        ImageView ivProfileMe;

        @BindView(R.id.tvContent)
        TextView tvContent;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}