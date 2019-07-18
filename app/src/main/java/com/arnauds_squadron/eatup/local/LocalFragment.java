package com.arnauds_squadron.eatup.local;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.SaveCallback;

import java.util.Date;

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
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        getChildFragmentManager().beginTransaction()
                .add(R.id.child_fragment_container, AddressFragment.newInstance())
                .commit();
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
        getChildFragmentManager().beginTransaction()
                .add(R.id.child_fragment_container, FoodTypeFragment.newInstance())
                .addToBackStack("Food type")
                .commit();
    }

    /**
     * Updates the food type parameter of this fragment's event variable
     */
    @Override
    public void updateFoodType(String foodType) {
        event.setCuisine(foodType);
        getChildFragmentManager().beginTransaction()
                .add(R.id.child_fragment_container, DateFragment.newInstance())
                .addToBackStack("Time")
                .commit();
    }

    /**
     * Updates the date parameter on the event, also the last field to be called
     * so we can save the event after this method runs
     */
    @Override
    public void updateDate(Date date) {
        event.setDate(date);
        saveEvent();

        // switch back to the home fragment
        mListener.onEventCreated();

        // switch to the address fragment to restart the setup process
        FragmentManager setupManager = getChildFragmentManager();
        setupManager.beginTransaction()
                .add(R.id.child_fragment_container, AddressFragment.newInstance())
                .commit();
    }

    private void saveEvent() {
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("LocalFragment", "create post success");
                } else {
                    e.printStackTrace();
                }
            }
        });
        // TODO: validate data
        // TODO: upload event to parse server
        // TODO: only return to home screen within onsuccess

        if (getFragmentManager() != null) {
            try {
                TabLayout tabLayout = getActivity().findViewById(R.id.tab_bar);
                tabLayout.getTabAt(1).select();
                Toast.makeText(getActivity(), "Event created", Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                Log.e(TAG, "Activity, tab layout, or home tab is null");
                Toast.makeText(getActivity(), "Could not create event", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onEventCreated();
    }
}
