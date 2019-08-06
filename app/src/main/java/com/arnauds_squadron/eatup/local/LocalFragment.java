package com.arnauds_squadron.eatup.local;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.local.setup.AddressFragment;
import com.arnauds_squadron.eatup.local.setup.DateFragment;
import com.arnauds_squadron.eatup.local.setup.start.StartFragment;
import com.arnauds_squadron.eatup.local.setup.tags.TagsFragment;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.navigation.NoSwipingViewPager;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment that handles the creation of a new recentEvent
 */
public class LocalFragment extends Fragment implements
        StartFragment.OnFragmentInteractionListener,
        AddressFragment.OnFragmentInteractionListener,
        TagsFragment.OnFragmentInteractionListener,
        DateFragment.OnFragmentInteractionListener {

    @BindView(R.id.flNoEventsScheduled)
    NoSwipingViewPager viewPager;

    private OnFragmentInteractionListener mListener;
    // The local event variable that is updated as the user creates their recentEvent
    private Event currentEvent;
    // The previous event that has already been created
    private Event recentEvent;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local, container, false);
        ButterKnife.bind(this, view);
        // Set the viewpager's setupAdapter so that it can display the setup fragments
        SetupFragmentPagerAdapter setupAdapter = new SetupFragmentPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(setupAdapter);
        viewPager.setOffscreenPageLimit(setupAdapter.getCount() - 1);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement the interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Overrides the StartFragment interface
     * <p>
     * Begin creating a completely new recentEvent
     */
    @Override
    public void startEventCreation(Event newEvent) {
        if(newEvent == null) {
            recentEvent = new Event();
            recentEvent.setHost(Constants.CURRENT_USER);
        } else {
            recentEvent = newEvent;
        }
        advanceViewPager();
    }

    /**
     * Overrides the TagsFragment interface
     * <p>
     * Updates some of the initial fields of the newly created recentEvent (tags, 21+, restaurant, etc)
     */
    @Override
    public void updateTags(List<String> tagList) {
        recentEvent.setTags(tagList);
        advanceViewPager();
    }

    /**
     * Overrides the AddressFragment interface
     * <p>
     * Updates the address of the local recentEvent with the ParseGeoPoint of the address of the recentEvent
     */
    @Override
    public void updateAddress(ParseGeoPoint address, String addressString) {
        currentEvent = new Event();
        currentEvent.setHost(Constants.CURRENT_USER);
        currentEvent.setAddress(address);
        currentEvent.setAddressString(addressString);
        advanceViewPager();
    }

    @Override
    public void updateFinalFields(String title, Date date, int maxGuests, boolean over21) {
        currentEvent.setTitle(title);
        currentEvent.setDate(date);
        currentEvent.setMaxGuests(maxGuests);
        currentEvent.setOver21(over21);
        saveEvent();
    }

    /**
     * Overrides the ReviewFragment interface
     *
     * @return the current recentEvent so the ReviewFragment can display all the details of the created
     * recentEvent
     */
    @Override
    public Event getRecentEvent() {
        return recentEvent;
    }

    /**
     * Overrides the ReviewFragment interface
     * <p>
     * Saves the recentEvent to the parse server, resets the setup fragment, and switches to the home
     * fragment
     */
    public void saveEvent() {
        currentEvent.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), "Event created!", Toast.LENGTH_LONG).show();
                    createEventChat(recentEvent.getTitle()); // create the corresponding chat
                    updateRecentEvents(); // update the list of recent events in the start fragment
                    mListener.onEventCreated(); // notify the MainActivity to go to the HomeFragment
                    resetSetupViewPager(); // reset the ViewPager to the StartFragment
                } else {
                    Toast.makeText(getActivity(), "Error creating recentEvent", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Method to be called by the parent activity to handle back presses. Moves the pager one
     * fragment backwards if possible
     *
     * @return true if the view pager was moved backwards, false if we were already on the first
     * item
     */
    public boolean onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            return false;
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            return true;
        }
    }

    /**
     * Method to be called by the parent activity to reset the setup viewpager to the start fragment
     */
    public void resetSetupViewPager() {
        viewPager.setCurrentItem(0);
    }

    /**
     * After the recentEvent is saved to the Parse server, update the list of the user's recent events in
     * the StartFragment to show their newest recentEvent
     */
    private void updateRecentEvents() {
        StartFragment startFragment = (StartFragment) getTypedFragment(StartFragment.class);
        startFragment.getEventsAsync();
    }

    /**
     * Creates the recentEvent's chat once the create recentEvent button is hit
     *
     * @param eventTitle The title of the new recentEvent
     */
    private void createEventChat(String eventTitle) {
        final Chat chat = new Chat();
        chat.setName(eventTitle + " Chat");
        chat.addMember(Constants.CURRENT_USER);
        chat.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) { // Register chat to the current recentEvent
                    recentEvent.setChat(chat);
                    recentEvent.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Toast.makeText(getActivity(), "Could not create the recentEvent's chat",
                                        Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Could not create a new chat",
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Given the class of a specific setup Fragment, this method searches through the child
     * Fragments and returns the given fragment if found, or null if it's not found
     *
     * @param fragmentClass The class of the fragment you are trying to find
     * @return The specified fragment with the matching class, or null if the fragment does not
     * exist
     */
    private Fragment getTypedFragment(Class fragmentClass) {
        List<Fragment> fragments = getChildFragmentManager().getFragments();

        for (Fragment fragment : fragments) {
            if (fragment.getClass().equals(fragmentClass)) {
                return fragment;
            }
        }
        return null;
    }

    /**
     * Moves the pager one fragment forward
     */
    private void advanceViewPager() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    /**
     * Interface to communicate with the parent activity so the HomeFragment is navigated to
     * after an recentEvent is created
     */
    public interface OnFragmentInteractionListener {
        /**
         * Callback to the parent's listener to switch to the home fragment
         */
        void onEventCreated();
    }
}
