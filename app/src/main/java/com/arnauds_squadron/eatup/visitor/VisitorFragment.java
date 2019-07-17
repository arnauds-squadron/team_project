package com.arnauds_squadron.eatup.visitor;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.EndlessRecyclerViewScrollListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class VisitorFragment extends Fragment {

    @BindView(R.id.tvUserName)
    TextView tvUserName;
    @BindView(R.id.tvBrowseTitle)
    TextView tvBrowseTitle;
    @BindView(R.id.rvBrowse)
    RecyclerView rvBrowse;
    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.tvCurrentLocation)
    TextView tvCurrentLocation;
    private Unbinder unbinder;
    private EndlessRecyclerViewScrollListener scrollListener;
    private BrowseEventAdapter postAdapter;
    private ArrayList<Event> mEvents;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visitor, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // initialize data source
        mEvents = new ArrayList<>();
        // construct adapter from data source
        postAdapter = new BrowseEventAdapter(getContext(), mEvents);
        // RecyclerView setup
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false);
        rvBrowse.setLayoutManager(gridLayoutManager);
        rvBrowse.setAdapter(postAdapter);


        // load data entries

        // retain instance so can call "resetStates" for fresh searches
//        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//                Date maxPostId = getMaxDate();
//                Log.d("DATE", maxPostId.toString());
//                loadTopPosts(getMaxDate());
//            }
//        };
        // add endless scroll listener to RecyclerView
//        rvPosts.addOnScrollListener(scrollListener);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

// methods to load posts into the recyclerview based on location
//    protected void loadTopPosts(Date maxDate) {
//        progressBar.setVisibility(View.VISIBLE);
//        final Post.Query postsQuery = new Post.Query();
//        // if opening app for the first time, get top 20 and clear old items
//        // otherwise, query for posts older than the oldest
//        if (maxDate.equals(new Date(0))) {
//            eventAdapter.clear();
//            postsQuery.getTop().withUser();
//        } else {
//            postsQuery.getOlder(maxDate).getTop().withUser();
//        }
//
//        postsQuery.findInBackground(new FindCallback<Post>() {
//            @Override
//            public void done(List<Post> objects, ParseException e) {
//                if (e == null) {
//                    for (int i = 0; i < objects.size(); ++i) {
//                        mEvents.add(objects.get(i));
//                        eventAdapter.notifyItemInserted(mEvents.size() - 1);
//                        // on successful reload, signal that refresh has completed
//                        swipeContainer.setRefreshing(false);
//                    }
//                } else {
//                    e.printStackTrace();
//                }
//                progressBar.setVisibility(View.INVISIBLE);
//            }
//        });
//    }
//
//    // get date of oldest post
//    protected Date getMaxDate() {
//        int postsSize = mEvents.size();
//        if (postsSize == 0) {
//            return (new Date(0));
//        } else {
//            Post oldest = mEvents.get(mEvents.size() - 1);
//            return oldest.getCreatedAt();
//        }
//    }

}
