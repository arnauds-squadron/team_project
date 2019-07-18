package com.arnauds_squadron.eatup.local;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.local.creation.AddressFragment;
import com.arnauds_squadron.eatup.local.creation.FoodTypeFragment;
import com.arnauds_squadron.eatup.local.creation.TimeFragment;
import com.arnauds_squadron.eatup.models.Event;
import com.parse.ParseGeoPoint;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocalFragment extends Fragment implements
        AddressFragment.OnFragmentInteractionListener {

    private final static String TAG = "LocalFragment";

    private OnFragmentInteractionListener mListener;
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
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Fragment childFragment = new AddressFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.child_fragment_container, childFragment)
                .addToBackStack("Address")
                .commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement the interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void updateAddress(ParseGeoPoint address) {
        event = new Event();
        event.setAddress(address);
        getChildFragmentManager().beginTransaction()
                .add(containerId, FoodTypeFragment.newInstance())
                .addToBackStack("Food type")
                .commit();
    }

    public void showNextFragment(int index, Event event) {
        Log.i("tyoyoyoyoyo", Integer.toString(index));
        int containerId = R.id.child_fragment_container;
        switch (index) {
            case 0:
                getChildFragmentManager().beginTransaction()
                        .add(containerId, FoodTypeFragment.newInstance())
                        .addToBackStack("Food type")
                        .commit();
            case 1:
                getChildFragmentManager().beginTransaction()
                        .add(containerId, TimeFragment.newInstance())
                        .addToBackStack("Time")
                        .commit();
                return;
            case 2:

                getChildFragmentManager().beginTransaction()
                        .replace(containerId, AddressFragment.newInstance())
                        .addToBackStack("Address")
                        .commit();
        }
    }

    public interface OnFragmentInteractionListener {
        void addFragmentToStack(Fragment fragment);

        void removeFragmentFromStack(Fragment fragment);
    }
}
