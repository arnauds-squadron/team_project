package com.arnauds_squadron.eatup.local.setup.start;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment that displays all the selected fields and will create the event when confirmed
 */
public class StartFragment extends Fragment {
    // TODO: Copy a recent event (have users working first)

    @BindView(R.id.rvRecentEvents)
    RecyclerView rvRecentEvents;

    private OnFragmentInteractionListener mListener;
    private List<Event> events;
    private RecentEventAdapter recentEventAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_start, container, false);
        ButterKnife.bind(this, view);

        events = new ArrayList<>();
        // construct adapter from data source
        recentEventAdapter = new RecentEventAdapter(this, events);
        // RecyclerView setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvRecentEvents.setLayoutManager(layoutManager);
        rvRecentEvents.setAdapter(recentEventAdapter);

        Event.Query query = new Event.Query();
        query.newestFirst().getTop().withHost().ownEvent(Constants.CURRENT_USER);

        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                events.addAll(objects);
                recentEventAdapter.notifyItemRangeInserted(0, events.size());
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Called in onCreate to bind the this child fragment to its parent, so the listener
     * can be used
     * @param fragment The parent fragment
     */
    public void onAttachToParentFragment(Fragment fragment)
    {
        try {
            mListener = (OnFragmentInteractionListener) fragment;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement the interface");
        }
    }

    @OnClick(R.id.btnStartNewEvent)
    public void startEventCreation() {
        mListener.startEventCreation();
    }

    public void useRecentEvent(Event event) {
        mListener.useRecentEvent(Event.copyEvent(event));

    }

    public interface OnFragmentInteractionListener {

        /**
         * Method that triggers the event creation method in the parent fragment
         */
        void startEventCreation();

        void useRecentEvent(Event event);
    }
}
