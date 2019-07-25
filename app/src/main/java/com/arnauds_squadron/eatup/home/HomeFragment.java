package com.arnauds_squadron.eatup.home;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.models.Event;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private HomeAdapter homeAdapter;
    private OnFragmentInteractionListener mListener;

    private ArrayList<Event> agenda;

    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    @BindView(R.id.rvAgenda)
    RecyclerView rvAgenda;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // initialize data source
        agenda = new ArrayList<>();
        // construct adapter from data source
        homeAdapter = new HomeAdapter(this, agenda);
        // RecyclerView setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        rvAgenda.setLayoutManager(layoutManager);
        rvAgenda.setAdapter(homeAdapter);

        // Setup refresh listener which triggers new data loading
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

        // TODO: change to progress bar in the middle?
        swipeContainer.setRefreshing(true);
        fetchTimelineAsync();
    }

    /**
     * Attaches the listener to the MainActivity
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement the interface");
        }
    }

    public void fetchTimelineAsync() {
        final Event.Query query = new Event.Query();
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    new UpdateTimeLineAsyncTask(HomeFragment.this)
                            .execute((objects.toArray(new Event[0])));
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Called by the HomeAdapter to open an event's chat
     */
    public void openChat(Chat chat) {
        mListener.switchToChatFragment(chat);
    }

    //TODO: documentation
    public interface OnFragmentInteractionListener {
        void switchToChatFragment(Chat chat);
    }

    /**
     * AsyncTask to update the timeline since fetchIfNeeded() was hanging the application on the
     * main thread
     */
    private static class UpdateTimeLineAsyncTask extends AsyncTask<Event, Void, Void> {

        private WeakReference<HomeFragment> context;
        private List<Event> usersEvents;

        UpdateTimeLineAsyncTask(HomeFragment context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected final Void doInBackground(Event... params) {
            usersEvents = new ArrayList<>();
            String currentUsername = ParseUser.getCurrentUser().getUsername();

            for (Event event : params) {
                String username = null;
                try {
                    username = event.getHost().fetchIfNeeded().getUsername();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }

                if (username.equals(currentUsername))
                    usersEvents.add(event);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            HomeFragment fragment = context.get();

            if(fragment != null) {
                fragment.agenda.clear();
                fragment.agenda.addAll(usersEvents);
                fragment.homeAdapter.notifyDataSetChanged();
                fragment.swipeContainer.setRefreshing(false);
            }
        }
    }
}
