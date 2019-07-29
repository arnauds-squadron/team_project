package com.arnauds_squadron.eatup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.Rating;
import com.arnauds_squadron.eatup.profile.ProfileActivity;
import com.arnauds_squadron.eatup.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.AVERAGE_RATING;
import static com.arnauds_squadron.eatup.utils.Constants.DISPLAY_NAME;
import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.arnauds_squadron.eatup.utils.Constants.NUM_RATINGS;

// Provide the underlying view for an individual list item.
public class RateUserAdapter extends RecyclerView.Adapter<RateUserAdapter.VH> {
    private Activity mContext;
    private Context context;
    private List<ParseUser> mUsers;
    private RecyclerView rvUsers;

    public RateUserAdapter(Activity context, List<ParseUser> users, RecyclerView rvUsers) {
        this.mContext = context;
        this.mUsers = users;
        this.rvUsers = rvUsers;
    }

    // Inflate the view based on the viewType provided.
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.item_rate_user, parent, false);
        return new VH(itemView, mContext);
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
        @BindView(R.id.btSubmitRating)
        Button btSubmitRating;

        VH(View itemView, final Context context) {
            super(itemView);
            rootView = itemView;
            ButterKnife.bind(this, itemView);

            btSubmitRating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitRating(context, (ParseUser) rootView.getTag(), userRatingBar.getRating());
                    btSubmitRating.setText("Rating submitted");
                    btSubmitRating.setOnClickListener(null);
                }
            });
        }
    }

    void submitRating(final Context context, final ParseUser user, final float newRating) {
        ParseQuery<Rating> query = new Rating.Query();
        query.whereEqualTo("user", user);
        query.findInBackground(new FindCallback<Rating>() {
            public void done(List<Rating> ratings, ParseException e) {
                if (e == null) {
                    if(ratings.size() != 0) {
                        Rating rating = ratings.get(0);
                        float averageRating = rating.getAvgRating().floatValue();
                        int numRatings = rating.getNumRatings().intValue();

                        rating.put(AVERAGE_RATING, calculateRating(averageRating, numRatings, newRating));
                        rating.increment(NUM_RATINGS);

                        rating.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(context, "User successfully rated.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d("RateUserActivity", "Error while saving");
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else {
                        createRating(context, newRating, user);
                    }

                } else {
                    Toast.makeText(context,"Query for rating not successful",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private float calculateRating(float averageRating, int numRatings, float newRating) {
        float currentTotal = averageRating * numRatings;
        return (currentTotal + newRating) / (numRatings + 1);
    }

    private void createRating(final Context context, float rating, ParseUser user) {
        final Rating newRating = new Rating();
        newRating.setAvgRating(rating);
        newRating.setUser(user);
        newRating.setNumRatings(1);

        newRating.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("RateUserActivity", "Create new rating successful");
                    Toast.makeText(context, "User successfully rated.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("PostDetailsActivity", "Error: unable to make new rating");
                    e.printStackTrace();
                }
            }
        });
    }
}
