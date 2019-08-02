package com.arnauds_squadron.eatup;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.DISPLAY_NAME;

import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;

// Provide the underlying view for an individual list item.
public class RateUserAdapter extends RecyclerView.Adapter<RateUserAdapter.VH> {
    private Activity mContext;
    private Context context;
    private List<ParseUser> mUsers;
    private RecyclerView rvUsers;
    private String ratingType;

    public RateUserAdapter(Activity context, List<ParseUser> users, RecyclerView rvUsers, String ratingType) {
        this.mContext = context;
        this.mUsers = users;
        this.rvUsers = rvUsers;
        this.ratingType = ratingType;
    }

    // Inflate the view based on the viewType provided.
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.item_rate_user, parent, false);
        return new VH(itemView);
    }

    // Display data at the specified position
    @Override
    public void onBindViewHolder(final VH holder, int position) {
        ParseUser user = mUsers.get(position);
        holder.rootView.setTag(user);

        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                String displayName = object.getString(DISPLAY_NAME);
                if(displayName != null) {
                    holder.tvUserToRate.setText(displayName);
                    holder.tvUserName.setText(displayName);
                }

                // load user profileImage
                ParseFile profileImage = object.getParseFile(KEY_PROFILE_PICTURE);
                if (profileImage != null) {
                    Glide.with(mContext.getApplicationContext())
                            .load(profileImage.getUrl())
                            .centerCrop()
                            .into(holder.ivUserToRate);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    // Provide a reference to the views for each contact item
    class VH extends RecyclerView.ViewHolder {
        final View rootView;
        @BindView(R.id.ivUserToRate)
        ImageView ivUserToRate;
        @BindView(R.id.tvUserToRate)
        TextView tvUserToRate;
        @BindView(R.id.tvUserName)
        TextView tvUserName;
        @BindView(R.id.userRatingBar)
        RatingBar userRatingBar;

        VH(View itemView) {
            super(itemView);
            rootView = itemView;
            ButterKnife.bind(this, itemView);

            userRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if(getAdapterPosition() < mUsers.size() - 1) {
                        rvUsers.smoothScrollToPosition(getAdapterPosition() + 1);
                    }
                }
            });
        }
    }
}
