package com.arnauds_squadron.eatup.visitor;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.EndlessRecyclerViewScrollListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;

public class VisitorSearchActivity extends AppCompatActivity {

    // initialize adapter, views, scroll listener
    protected SearchPostAdapter postAdapter;
    protected ArrayList<Post> mPosts;
    private RecyclerView rvPosts;
    protected SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;
    private ProgressBar progressBar;
    private ParseUser user;
    private TextView tvUsername;
    private ImageView ivProfileImage;
    private final String KEY_PROFILE_IMAGE = "profileImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_search);
        ButterKnife.bind(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        rvPosts = (RecyclerView) findViewById(R.id.rvSearchResults);

        // initialize data source
        mPosts = new ArrayList<>();
        // construct adapter from data source
        postAdapter = new SearchPostAdapter(this, mPosts);
        // RecyclerView setup
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rvPosts.setLayoutManager(gridLayoutManager);
        rvPosts.setAdapter(postAdapter);

        user = getIntent().getParcelableExtra("user");
        tvUsername.setText(user.getUsername());
        ParseFile profileImage = user.getParseFile(KEY_PROFILE_IMAGE);
        if (profileImage != null) {
            Glide.with(getApplicationContext())
                    .load(profileImage.getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileImage);
        }

        loadTopPosts(user, new Date(0));

        // retain instance so can call "resetStates" for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Date maxPostId = getMaxDate();
                Log.d("DATE", maxPostId.toString());
                loadTopPosts(user, getMaxDate());
            }
        };
        // add endless scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);

        // set up refresh listener that triggers new data loading
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTopPosts(user, new Date(0));
            }
        });
        // configure refreshing colors
        swipeContainer.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));
    }

    private void loadTopPosts(ParseUser user, Date maxDate) {
        progressBar.setVisibility(View.VISIBLE);
        final Post.Query postsQuery = new Post.Query();
        // if opening app for the first time, get top 20 and clear old items
        // otherwise, query for posts older than the oldest
        if (maxDate.equals(new Date(0))) {
            postAdapter.clear();
            postsQuery.getTop().withUser().whereEqualTo(Post.KEY_USER, user);
        } else {
            postsQuery.getOlder(maxDate).getTop().withUser().whereEqualTo(Post.KEY_USER, user);
        }

        postsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        mPosts.add(objects.get(i));
                        postAdapter.notifyItemInserted(mPosts.size() - 1);
                        // on successful reload, signal that refresh has completed
                        swipeContainer.setRefreshing(false);
                    }
                } else {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    // get date of oldest post
    private Date getMaxDate() {
        int postsSize = mPosts.size();
        if (postsSize == 0) {
            return (new Date(0));
        } else {
            Post oldest = mPosts.get(mPosts.size() - 1);
            return oldest.getCreatedAt();
        }
    }
}
