package com.arnauds_squadron.eatup.home;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.FindCallback;
import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    @BindView(R.id.rvAgenda)
    RecyclerView rvAgenda;

    private OnFragmentInteractionListener mListener;
    private List<Event> agenda;
    private HomeAdapter homeAdapter;

    private boolean refreshRunnableNotStarted = false;
    // Handler to post the runnable on the Looper's queue every second
    private Handler updateHandler = new Handler();
    // Refresh runnable that refreshes the messages every second
    private Runnable refreshEventsRunnable = new Runnable() {
        @Override
        public void run() {
            refreshEventsAsync();
            Log.i("afsdfasdf", "refreshing events");
            updateHandler.postDelayed(this, Constants.EVENT_UPDATE_SPEED_MILLIS);
        }
    };

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
        agenda = new ArrayList<>();
        // construct adapter from data source
        homeAdapter = new HomeAdapter(getContext(), this, agenda);
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
                refreshEventsAsync();
            }
        });

        // TODO: standardize
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        startUpdatingEvents();
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

    /**
     * // TODO: also show pending events?
     * Fetches all events from the Parse server and filters if the current user is the host
     * or is an accepted guest
     */
    public void refreshEventsAsync() {
        // List that represents the timeline if it were to be updates. The timeline only updates
        // when this list is different from the current timeline.
        final List<Event> tempEvents = new ArrayList<>();

        final String userId = Constants.CURRENT_USER.getObjectId();
        final Event.Query query = new Event.Query();
        query.withHost().orderByDescending("createdAt");
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    for (final Event event : objects) {
                        final JSONArray guests = event.getAcceptedGuests();
                        String hostId = event.getHost().getObjectId();
                        if (userId.equals(hostId) || (guests != null &&
                                guests.toString().contains(userId)))
                            tempEvents.add(event);
                    }
                    if (tempEvents.size() != agenda.size()) { // Only update if events changed
                        agenda.clear();
                        agenda.addAll(tempEvents);
                        homeAdapter.notifyDataSetChanged();
                    }
                    swipeContainer.setRefreshing(false);
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

    /**
     * Called by the parent Activity to stop updating the events when the user is logged out
     */
    public void stopUpdatingEvents() {
        updateHandler.removeCallbacks(refreshEventsRunnable);
        refreshRunnableNotStarted = false;
    }

    private void startUpdatingEvents() {
        // TODO: change to progress bar in the middle?
        swipeContainer.setRefreshing(true);
        refreshEventsAsync();

        if (!refreshRunnableNotStarted) { // only one runnable
            refreshEventsRunnable.run();
            refreshRunnableNotStarted = true;
        }
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
            String currentUserId = Constants.CURRENT_USER.getObjectId();

            for (Event event : params) {
                try {
                    String hostId = event.getHost().fetchIfNeeded().getObjectId();
                    JSONArray jsonArray = event.getAcceptedGuests();
                    if (jsonArray != null) {
                        if (jsonArray.toString().contains(hostId)) {
                            usersEvents.add(event);
                        }
                    }
                    if (currentUserId.equals(hostId)) {
                        usersEvents.add(event);
                    } else {
                        JSONArray acceptedGuests = event.getAcceptedGuests();

                        if (acceptedGuests != null) {
                            for (int i = 0; i < acceptedGuests.length(); i++) {
                                JSONObject object = acceptedGuests.getJSONObject(i);
                                String acceptedGuestId = object.getString("objectId");

                                if (currentUserId.equals(acceptedGuestId))
                                    usersEvents.add(event);
                            }
                        }
                    }
                } catch (ParseException | JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            HomeFragment fragment = context.get();

            if (fragment != null) {
                fragment.agenda.clear();
                fragment.agenda.addAll(usersEvents);
                fragment.homeAdapter.notifyDataSetChanged();
                fragment.swipeContainer.setRefreshing(false);
            }
        }
    }
}
