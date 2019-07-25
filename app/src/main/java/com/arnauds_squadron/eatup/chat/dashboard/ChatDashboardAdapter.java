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
import com.arnauds_squadron.eatup.utils.FormatHelper;
import com.bumptech.glide.Glide;
import com.parse.ParseFile;

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
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Chat chat = chatList.get(i);

        viewHolder.tvName.setText(chat.getName());
        viewHolder.tvUpdatedAt.setText(FormatHelper.formatTimestamp(chat.getUpdatedAt()));

        ParseFile image = chat.getImage();

        if (image != null) {
            Glide.with(context)
                    .load(chat.getImage().getUrl())
                    .into(viewHolder.ivImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivImage)
        ImageView ivImage;

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
