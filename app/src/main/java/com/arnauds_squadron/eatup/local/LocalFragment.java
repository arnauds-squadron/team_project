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
import com.arnauds_squadron.eatup.local.setup.ReviewFragment;
import com.arnauds_squadron.eatup.local.setup.StartFragment;
import com.arnauds_squadron.eatup.local.setup.tags.TagsFragment;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.navigation.NoSwipingPagerAdapter;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocalFragment extends Fragment implements
        StartFragment.OnFragmentInteractionListener,
        AddressFragment.OnFragmentInteractionListener,
        TagsFragment.OnFragmentInteractionListener,
        DateFragment.OnFragmentInteractionListener,
        ReviewFragment.OnFragmentInteractionListener {

    @BindView(R.id.frameLayout)
    NoSwipingPagerAdapter setupViewPager;

    // Listener that communicates with the parent activity to switch back to the HomeFragment
    // when the event is finally created
    private OnFragmentInteractionListener mListener;
    // The local event variable that is updated as the user creates their event
    private Event event;

    public static LocalFragment newInstance() {
        return new LocalFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local, container, false);
        ButterKnife.bind(this, view);
        // Set the viewpager's adapter so that it can display the setup fragments
        setupViewPager.setAdapter(new SetupFragmentPagerAdapter(getChildFragmentManager()));
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
     * Begin creating a completely new event
     */
    @Override
    public void startEventCreation() {
        advanceViewPager();
    }

    /**
     * Overrides the TagsFragment interface
     * <p>
     * Updates some of the initial fields of the newly created event (tags, 21+, restaurant, etc)
     */
    @Override
    public void updateTags(Event newEvent) {
        event = newEvent;
        advanceViewPager();
    }

    /**
     * Overrides the AddressFragment interface
     * <p>
     * Updates the address of the local event with the ParseGeoPoint of the address of the event
     */
    @Override
    public void updateAddress(ParseGeoPoint address, String addressString) {
        event.setAddress(address);
        event.setAddressString(addressString);
        advanceViewPager();
    }

    /**
     * Overrides the DateFragment interface
     * <p>
     * Updates the date parameter on the event, also the last field to be called
     * so we can save the event after this method runs
     */
    @Override
    public void updateDate(Date date) {
        event.setDate(date);
        advanceViewPager();
    }

    /**
     * Overrides the ReviewFragment interface
     *
     * @return the current event so the ReviewFragment can display all the details of the created
     * event
     */
    @Override
    public Event getCurrentEvent() {
        return event;
    }

    /**
     * Overrides the ReviewFragment interface
     * <p>
     * Saves the event to the parse server, resets the setup fragment, and switches to the home
     * fragment
     */
    @Override
    public void createEvent(String eventTitle) {
        createEventChat(eventTitle);
        event.setTitle(eventTitle);
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), "Event created!", Toast.LENGTH_LONG).show();
                    mListener.onEventCreated();
                    resetSetupViewPager();
                } else {
                    Toast.makeText(getActivity(), "Error creating event", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    // TODO: move get current user to new thread
    // TODO: add accepted guests immediately after being accepted

    /**
     * Creates the event's chat once the create event button is hit
     * @param eventTitle The title of the new event
     */
    private void createEventChat(String eventTitle) {
        final Chat chat = new Chat();
        chat.setName(eventTitle + " Chat");
        chat.addMember(ParseUser.getCurrentUser().getObjectId());

        if (event.getEventImage() != null)
            chat.setImage(event.getEventImage());

        chat.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) { // Register chat to the current event
                    event.addChat(chat);
                    event.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Toast.makeText(getActivity(), "Could not create the event's chat",
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
     * Method to be called by the parent activity to handle back presses. Moves the pager one
     * fragment backwards if possible
     *
     * @return true if the view pager was moved backwards, false if we were already on the first
     * item
     */
    public boolean onBackPressed() {
        if (setupViewPager.getCurrentItem() == 0) {
            return false;
        } else {
            setupViewPager.setCurrentItem(setupViewPager.getCurrentItem() - 1);
            return true;
        }
    }

    /**
     * Method to be called by the parent activity to reset the setup viewpager to the start fragment
     */
    public void resetSetupViewPager() {
        setupViewPager.setCurrentItem(0);
    }

    /**
     * Moves the pager one fragment forward
     */
    private void advanceViewPager() {
        setupViewPager.setCurrentItem(setupViewPager.getCurrentItem() + 1);
    }


    /**
     * Interface to communicate with the parent activity so the HomeFragment is navigated to
     * after an event is created
     */
    public interface OnFragmentInteractionListener {
        /**
         * Callback to the parent's listener to switch to the home fragment
         */
        void onEventCreated();
    }
}
