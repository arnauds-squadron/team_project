package com.arnauds_squadron.eatup.visitor;

import android.content.Context;
import android.content.Intent;
import android.media.Rating;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchPostAdapter extends RecyclerView.Adapter<SearchPostAdapter.ViewHolder> {

    private List<Post> posts;
    // context defined as global variable so Glide in onBindViewHolder has access
    private Context context;

    // pass Post array in constructor
    public SearchPostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    // for each row, inflate layout and cache references into ViewHolder
    // method invoked only when creating a new row
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.search_post_linear, parent, false);
        return new ViewHolder(postView);
    }

    // bind values based on element position
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get data according to position
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    // create ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.ivProfileImage)
        public ImageView ivProfileImage;
        @BindView(R.id.tvName)
        public TextView tvUsername;
        @BindView(R.id.ratingBar)
        public RatingBar ratingBar;
        @BindView(R.id.tvCuisine)
        public TextView tvCuisine;
        @BindView(R.id.tvDistance)
        public TextView tvDistance;
        @BindView(R.id.btBook)
        public Button btBook;

        // constructor takes in inflated layout
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(Post post) {
            // populate views according to data
            tvDescription.setText(post.getDescription());
            ParseFile postImage = post.getImage();
            if (postImage != null) {
                Glide.with(context)
                        .load(postImage.getUrl())
                        .centerCrop()
                        .into(ivPostImage);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            // ensure position valid (exists in view)
            if (position != RecyclerView.NO_POSITION) {
                Log.d("PostAdapter", "View Post Details");
                Post post = posts.get(position);

                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra("post_id", post.getObjectId());
                context.startActivity(intent);
            }
        }
    }

    // RecyclerView adapter helper methods to clear items from or add items to underlying dataset
    // clean recycler elements
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // add list of posts - change list type depending on item type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }
}
