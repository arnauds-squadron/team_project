package com.arnauds_squadron.eatup.local.creation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.ParseGeoPoint;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddressFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddressFragment extends Fragment {

    private final static String TAG = "AddressFragment";

    // The place that the user selects after searching for its address
    private Place selectedPlace;

    // The listener that communicates to the LocalFragment to update the address when
    // the user hits the next button
    private OnFragmentInteractionListener mListener;

    public static AddressFragment newInstance() {
        Bundle args = new Bundle();
        AddressFragment fragment = new AddressFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);
        ButterKnife.bind(this, view);
        setupAutoCompleteFragment();
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.btnNext)
    public void goToNextFragment() {
        if (selectedPlace == null) {
            Toast.makeText(getActivity(), "Select an address first!", Toast.LENGTH_LONG).show();
        } else {
            double latitude = selectedPlace.getLatLng().latitude;
            double longitude = selectedPlace.getLatLng().longitude;
            mListener.updateAddress(new ParseGeoPoint(latitude,longitude));
        }
    }

    private void setupAutoCompleteFragment() {
        if (!Places.isInitialized()) {
            Log.e(TAG, "Places should be initialized already");
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setHint("Address");
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                selectedPlace = place;
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getActivity(), "Error getting the address", Toast.LENGTH_SHORT)
                        .show();
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    public interface OnFragmentInteractionListener {
        /**
         * Method called in the parent to update the event object to have the user's selected
         * address, and to switch to the next setup fragment
         */
        void updateAddress(ParseGeoPoint address);
    }
}
