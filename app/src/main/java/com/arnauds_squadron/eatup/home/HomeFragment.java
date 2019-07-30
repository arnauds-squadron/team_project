package com.arnauds_squadron.eatup.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
}
