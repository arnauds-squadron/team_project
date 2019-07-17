package com.arnauds_squadron.eatup.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.Home;
import com.arnauds_squadron.eatup.visitor.BrowseEventAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private HomeAdapter homeAdapter;
    ArrayList<Event> agenda;

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
        rvAgenda = view.findViewById(R.id.rvAgenda);
        rvAgenda.setLayoutManager(new LinearLayoutManager(getContext()));
        homeAdapter = new HomeAdapter(agenda);
        rvAgenda.setAdapter(homeAdapter);

        final ParseQuery<Event> query = ParseQuery.getQuery(Event.class);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        Event event = objects.get(i);
                        agenda.add(event);
                        homeAdapter.notifyItemInserted(agenda.size() - 1);
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
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
        rvAgenda.setLayoutManager(layoutManager);
        rvAgenda.setAdapter(homeAdapter);
    }
}
