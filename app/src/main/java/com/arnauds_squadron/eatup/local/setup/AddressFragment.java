package com.arnauds_squadron.eatup.local.setup;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.utils.UIHelper;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.ParseGeoPoint;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment that asks the user to input an address of the event. Displays the selected address
 * on the map fragment
 */
public class AddressFragment extends Fragment implements OnMapReadyCallback {
    private final static String TAG = "AddressFragment";

    // The listener that communicates to the LocalFragment to update the address when
    // the user hits the next button
    private OnFragmentInteractionListener mListener;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    // The place that the user selects after searching for its address
    private Place selectedPlace;

    public static AddressFragment newInstance() {
        return new AddressFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_address, container, false);
        ButterKnife.bind(this, view);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        setupAutoCompleteFragment();
        initializePlaces(); // initialize the places sdk
        mapFragment.getMapAsync(this); // update the map
        setupAutoCompleteFragment(); // set up the auto

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null)
            mMap.clear();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Called whenever getMapAsync() is called, and sets up the map
     * @param googleMap The map obtained from the maps API
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(selectedPlace == null) {
            // TODO: move map to current location
            // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user.location.latlng, 1));
        } else {
            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(selectedPlace.getLatLng(),
                            UIHelper.DEFAULT_MAP_ZOOM_LEVEL));

            mMap.addMarker(new MarkerOptions()
                    .position(selectedPlace.getLatLng())
                    .title(selectedPlace.getName())
                    .snippet("snippet")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
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

    @OnClick(R.id.btnNext)
    public void goToNextFragment() {
        if (selectedPlace == null) {
            Toast.makeText(getActivity(), "Select an address first!", Toast.LENGTH_LONG).show();
        } else {
            double latitude = selectedPlace.getLatLng().latitude;
            double longitude = selectedPlace.getLatLng().longitude;
            mListener.updateAddress(new ParseGeoPoint(latitude, longitude),
                    selectedPlace.getAddress());
        }
    }

    /**
     * Initalizes the places SDK within the main activity
     */
    private void initializePlaces() {
        Places.initialize(getActivity(), getString(R.string.google_api_key));
    }

    /**
     * Sets up the autocomplete fragment with the address hint, and sets the OnPlaceSelectedListener
     * that will update the selectedPlace object and the map fragment
     */
    private void setupAutoCompleteFragment() {
        if (!Places.isInitialized()) {
            Log.e(TAG, "Places should be initialized already");
            initializePlaces();
        }

        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setHint("Address");
        autocompleteFragment.setPlaceFields(Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                selectedPlace = place;
                mapFragment.getMapAsync(AddressFragment.this);
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
        void updateAddress(ParseGeoPoint address, String addressString);
    }
}
