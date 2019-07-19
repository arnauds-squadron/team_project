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
import com.arnauds_squadron.eatup.local.setup.FoodTypeFragment;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.navigation.NoSwipingPagerAdapter;
import com.arnauds_squadron.eatup.navigation.SetupFragmentPagerAdapter;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
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
        AddressFragment.OnFragmentInteractionListener,
        FoodTypeFragment.OnFragmentInteractionListener,
        DateFragment.OnFragmentInteractionListener {

    private final static String TAG = "LocalFragment";

    @BindView(R.id.viewPager)
    NoSwipingPagerAdapter setupViewPager;

    // Listener that communicates with the parent activity to switch back to the HomeFragment
    // when the event is finally created
    private OnFragmentInteractionListener mListener;

    // The local event variable that is updated as the user creates their event
    private Event event;

    public static LocalFragment newInstance() {
        Bundle args = new Bundle();
        LocalFragment fragment = new LocalFragment();
        fragment.setArguments(args);
        return fragment;
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
     * Updates the address of the local event with the ParseGeoPoint of the address of the event
     */
    @Override
    public void updateAddress(ParseGeoPoint address) {
        event = new Event();
        event.setAddress(address);
        advanceViewPager();
    }

    /**
     * Updates the food type parameter of this fragment's event variable
     */
    @Override
    public void updateFoodType(String foodType) {
        event.setCuisine(foodType);
        advanceViewPager();
    }

    /**
     * Updates the date parameter on the event, also the last field to be called
     * so we can save the event after this method runs
     */
    @Override
    public void updateDate(Date date) {
        event.setDate(date);
        saveEvent();
    }

    private void saveEvent() {
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), "Event created!", Toast.LENGTH_SHORT).show();
                    Log.d("LocalFragment", "create post success");
                    // switch back to the home fragment
                    mListener.onEventCreated();
                    // move viewpager to the first setup fragment
                    advanceViewPager();
                } else {
                    Toast.makeText(getActivity(), "Could not create post",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Moves the pager one fragment backwards
     */
    public void retreatViewPager() {
        if (setupViewPager.getCurrentItem() == 0)
            throw new IllegalArgumentException("Cannot retreat view pager on the first fragment!");

        setupViewPager.setCurrentItem(setupViewPager.getCurrentItem() - 1);
    }

    public NoSwipingPagerAdapter getSetupViewPager() {
        return setupViewPager;
    }

    /**
     * Moves the pager one fragment forward
     */
    private void advanceViewPager() {
        setupViewPager.setCurrentItem(setupViewPager.getCurrentItem() + 1);
    }


    public interface OnFragmentInteractionListener {
        void onEventCreated();
    }
}
