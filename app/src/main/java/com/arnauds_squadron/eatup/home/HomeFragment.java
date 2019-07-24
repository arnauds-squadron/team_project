package com.arnauds_squadron.eatup.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.arnauds_squadron.eatup.ProfileActivity;
import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.parse.Parse.LOG_LEVEL_DEBUG;
import static com.parse.Parse.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private HomeAdapter homeAdapter;
    private Unbinder unbinder;

    ArrayList<Event> agenda;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;
    @BindView(R.id.rvAgenda)
    RecyclerView rvAgenda;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // initialize data source
        agenda = new ArrayList<>();
        // construct adapter from data source
        homeAdapter = new HomeAdapter(agenda);
        // RecyclerView setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        rvAgenda.setLayoutManager(layoutManager);
        rvAgenda.setAdapter(homeAdapter);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                fetchTimelineAsync();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        final Event.Query query = new Event.Query();
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    ParseUser parseUser = ParseUser.getCurrentUser();
                    String username = null;
                    for (int i = 0; i < objects.size(); i++) {
                        Event event = objects.get(i);
                        try {
                            username = event.getHost().fetchIfNeeded().getUsername();
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        if (username.equals(parseUser.getUsername())) {
                            agenda.add(event);
                            homeAdapter.notifyItemInserted(agenda.size() - 1);
                        }
                    }
                }
            }
        });
    }
    public void fetchTimelineAsync() {
        agenda.clear();
        final ParseQuery<Event> query = ParseQuery.getQuery(Event.class);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    ParseUser parseUser = ParseUser.getCurrentUser();
                    String username = null;
                    for (int i = 0; i < objects.size(); i++) {
                        Event event = objects.get(i);
                        try {
                            username = event.getHost().fetchIfNeeded().getUsername();
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        if (username.equals(parseUser.getUsername())) {
                            agenda.add(event);
                            homeAdapter.notifyItemInserted(agenda.size() - 1);
                        }
                    }
                    swipeContainer.setRefreshing(false);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
}
