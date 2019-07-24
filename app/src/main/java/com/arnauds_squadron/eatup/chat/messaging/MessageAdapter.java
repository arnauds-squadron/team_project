package com.arnauds_squadron.eatup.chat.messaging;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Message;
import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messages;
    private Context context;
    private ParseUser sender;

    public MessageAdapter(Context context, ParseUser sender, List<Message> messages) {
        this.messages = messages;
        this.sender = sender;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        boolean isMe = message.getSender().equals(sender);

        if (isMe) {
            holder.ivProfileMe.setVisibility(View.VISIBLE);
            holder.ivProfileOther.setVisibility(View.GONE);
            holder.tvContent.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        } else {
            holder.ivProfileOther.setVisibility(View.VISIBLE);
            holder.ivProfileMe.setVisibility(View.GONE);
            holder.tvContent.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        }

        ImageView profileView = isMe ? holder.ivProfileMe : holder.ivProfileOther;
        // TODO: new thread?

        try {
            ParseFile profilePicture = (ParseFile)
                    message.getSender().fetchIfNeeded().get("profilePicture");

            if (profilePicture != null)
                Glide.with(context).load(profilePicture.getUrl()).into(profileView);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvContent.setText(message.getContent());
    }

    // Change gravity of image based on the hash value obtained from userId
    private static String getProfileUrl(final String userId) {
        String hex = "";
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final byte[] hash = digest.digest(userId.getBytes());
            final BigInteger bigInt = new BigInteger(hash);
            hex = bigInt.abs().toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO: change icon to user's profile image
        return "https://www.gravatar.com/avatar/" + hex + "?d=identicon";
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivProfileOther)
        ImageView ivProfileOther;

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