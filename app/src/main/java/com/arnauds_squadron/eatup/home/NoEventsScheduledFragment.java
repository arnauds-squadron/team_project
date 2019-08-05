package com.arnauds_squadron.eatup.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.Constants;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment that displays the user's RSVP'd events and is the fragment to accept or deny guests.
 * Serves as an agenda and constantly refreshes to stay up to date.
 */
public class NoEventsScheduledFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_no_events_scheduled, container, false);
        ButterKnife.bind(this, view);
        return view;
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

    @OnClick(R.id.btnCreateEvent)
    public void goToLocalFragment() {
        mListener.navigateToFragment(Constants.LOCAL_FRAGMENT_INDEX);
    }

    @OnClick(R.id.btnFindEvent)
    public void goToVisitorFragment() {
        mListener.navigateToFragment(Constants.VISITOR_FRAGMENT_INDEX);
    }

    public interface OnFragmentInteractionListener {
        /**
         * Navigates to the fragment at the ViewPager's specified index
         * @param index The index of the fragment in the ViewPager
         */
        void navigateToFragment(int index);
    }
}
