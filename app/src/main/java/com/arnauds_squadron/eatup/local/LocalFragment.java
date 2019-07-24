package com.arnauds_squadron.eatup.local;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.navigation.NoSwipingPagerAdapter;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;

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

    private final static String TAG = "LocalFragment";

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
     *
     * Begin creating a completely new event
     */
    @Override
    public void startEventCreation() {
        advanceViewPager();
    }

    /**
     * Overrides the TagsFragment interface
     *
     * Updates the food type parameter of this fragment's event variable
     */
    @Override
    public void updateTags(List<String> tags) {
        event = new Event();
        // TODO new thread?
        event.setHost(ParseUser.getCurrentUser());
        event.setTags(tags);
        advanceViewPager();
    }

    /**
     * Overrides the AddressFragment interface
     *
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
     *
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
     *
     * Saves the event to the parse server, resets the setup fragment, and switches to the home
     * fragment
     */
    @Override
    public void createEvent() {
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), "Event created!", Toast.LENGTH_SHORT).show();
                    Log.d("LocalFragment", "create post success");
                    mListener.switchToHomeFragment();
                    setupViewPager.setCurrentItem(0);
                } else {
                    Toast.makeText(getActivity(), "Could not create post",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Method to be called by the parent activity to handle back presses. Moves the pager one
     * fragment backwards
     * @return true if the view pager was moved backwards, false if we were already on the first
     * item
     */
    public boolean retreatViewPager() {
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
        void switchToHomeFragment();
    }
}
