package com.arnauds_squadron.eatup.home.requests;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.profile.HostProfileActivity;
import com.arnauds_squadron.eatup.profile.VisitorProfileActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private Event event;
    private List<ParseUser> requests;
    private Context context;

    public RequestAdapter(Event event, List<ParseUser> requests) {
        this.event = event;
        this.requests = requests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.item_request, viewGroup, false);
        return new ViewHolder(postView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        ParseUser user = requests.get(i);
        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                String requestText = object.getString("username") + " would like to join";

                //TODO: spannable to make name and event name bold
                viewHolder.tvRequestText.setText(requestText);
            }
        });

        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                ParseFile profilePicture = object.getParseFile("profilePicture");
                if (profilePicture != null) {
                    Glide.with(context)
                            .load(profilePicture.getUrl())
                            .transform(new CircleCrop())
                            .into(viewHolder.ivProfile);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    /**
     * ViewHolder that shows a single request that the user has
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivImage)
        ImageView ivProfile;

        @BindView(R.id.tvRequestText)
        TextView tvRequestText;

        @BindView(R.id.btnAccept)
        Button btnAccept;

        @BindView(R.id.btnDeny)
        Button btnDeny;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    ParseUser user = requests.get(position);
                    handleRequest(user, position, true);
                }
            });

            btnDeny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    ParseUser user = requests.get(position);
                    handleRequest(user, position, false);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, VisitorProfileActivity.class);
                    ParseUser user = requests.get(getAdapterPosition());
                    intent.putExtra("user", user);
                    context.startActivity(intent);
                }
            });
        }
    }

    private void handleRequest(final ParseUser user, final int position, boolean isAccepted) {
        event.handleRequest(user, isAccepted);
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    requests.remove(position);
                    notifyItemRemoved(position);
                    Log.d("RequestAdapter", "Request handled");
                } else {
                    Log.d("RequestAdapter", "Error handling request.");
                }
            }
        });
    }
}
