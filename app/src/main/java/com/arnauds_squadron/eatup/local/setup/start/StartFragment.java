package com.arnauds_squadron.eatup.local.setup.start;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.rbrooks.indefinitepagerindicator.IndefinitePagerIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment that displays all the selected fields and will create the event when confirmed
 */
public class StartFragment extends Fragment {

    @BindView(R.id.rvRecentEvents)
    RecyclerView rvRecentEvents;

    @BindView(R.id.tvNoRecentEvents)
    TextView tvNoRecentEvents;

    @BindView(R.id.pager_indicator)
    IndefinitePagerIndicator pagerIndicator;

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
        recentEventAdapter = new RecentEventAdapter(this, events);
        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(rvRecentEvents);
        pagerIndicator.attachToRecyclerView(rvRecentEvents);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1,
                GridLayoutManager.HORIZONTAL, false);
        rvRecentEvents.setLayoutManager(layoutManager);
        rvRecentEvents.setAdapter(recentEventAdapter);

        getEventsAsync();

        return view;
    }

    /**
     * Queries the Parse server to get the user's most recent events as host
     */
    public void getEventsAsync() {
        Event.Query query = new Event.Query();
        query.newestFirst().getTop().withHost().ownEvent(Constants.CURRENT_USER);

        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if(objects.size() > 0) {
                    tvNoRecentEvents.setVisibility(View.INVISIBLE);
                    events.clear();
                    events.addAll(objects);
                    recentEventAdapter.notifyDataSetChanged();
                } else {
                    tvNoRecentEvents.setVisibility(View.VISIBLE);
                }
            }
        });
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
        mListener.startEventCreation(null);
    }

    /**
     * Calls the LocalFragment to pass the event selected by the user from the recent event list
     * @param event The event to use in the setup wizard
     */
    public void startEventCreation(Event event) {
        mListener.startEventCreation(Event.copyEvent(event));
    }

    public interface OnFragmentInteractionListener {
        /**
         * Method that triggers the event creation method in the parent fragment
         */
        void startEventCreation(Event event);
    }
}
