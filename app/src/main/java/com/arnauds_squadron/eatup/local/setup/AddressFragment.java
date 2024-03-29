package com.arnauds_squadron.eatup.local.setup;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.Constants;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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

    private OnFragmentInteractionListener mListener;
    private GoogleMap map;
    // The place that the user selects after searching for its address
    private Place selectedPlace;
    private AutocompleteSupportFragment autocompleteFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_address, container, false);
        ButterKnife.bind(this, view);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);

        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        setupAutoCompleteFragment();
        initializePlaces(); // initialize the places sdk
        mapFragment.getMapAsync(this); // update the map
        return view;
    }

    /**
     * Gets the selected event if any to set the previous location again
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            Event recentEvent = mListener.getRecentEvent();
            Event currentEvent = mListener.getCurrentEvent();
            if (recentEvent != null) {
                ParseGeoPoint point = recentEvent.getAddress();
                LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                String address = recentEvent.getAddressString();

                moveCamera(latLng, Constants.DEFAULT_MAP_ZOOM, recentEvent.getTitle());
                autocompleteFragment.a.setText(address); // EditText view
                selectedPlace = Place.builder().setLatLng(latLng).setAddress(address).build();
            } else if (currentEvent == null) {
                if (map != null) {
                    map.clear();
                    moveCamera(new LatLng(0, 0), 1, null);
                }
                autocompleteFragment.a.setText("");
                selectedPlace = null;
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Called whenever getMapAsync() is called, and sets up the map
     *
     * @param googleMap The map obtained from the maps API
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    /**
     * Called in onCreate to bind the this child fragment to its parent, so the listener
     * can be used
     *
     * @param fragment The parent fragment
     */
    public void onAttachToParentFragment(Fragment fragment) {
        try {
            mListener = (OnFragmentInteractionListener) fragment;
        } catch (ClassCastException e) {
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
     * Animates the Google Map to the specified latitude and longitude, and makes a marker with the
     * given title if it is not null
     *
     * @param latLng Object holding the latitude and longitude of the point the camera is moving to
     * @param title  title of the marker, if set to null no marker is drawn.
     */
    private void animateCamera(LatLng latLng, String title) {
        try {
            map.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(latLng, Constants.DEFAULT_MAP_ZOOM));
            if (title != null) {
                map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Moves the Google Map to the specified latitude and longitude, and makes a marker with the
     * given title if it is not null
     *
     * @param latLng Object holding the latitude and longitude of the point the camera is moving to
     * @param title  title of the marker, if set to null no marker is drawn.
     */
    private void moveCamera(LatLng latLng, float zoomLevel, String title) {
        try {
            map.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(latLng, zoomLevel));

            if (title != null) {
                map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the places SDK within the main activity
     */
    private void initializePlaces() {
        Places.initialize(getActivity(), getString(R.string.google_api_key));
    }

    /**
     * Sets up the autocomplete fragment with the address hint, and sets the OnPlaceSelectedListener
     * that will update the selectedPlace object and the map fragment
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupAutoCompleteFragment() {
        if (!Places.isInitialized()) {
            initializePlaces();
        }

        setAutocompleteFragmentColors();
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
                animateCamera(place.getLatLng(), place.getName());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getActivity(), "Error getting the address", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void setAutocompleteFragmentColors() {
        // Set search icon color to toast red
        ((ImageButton) autocompleteFragment.getView()
                .findViewById(R.id.places_autocomplete_search_button))
                .setColorFilter(getResources().getColor(R.color.toast_red));
    }

    public interface OnFragmentInteractionListener {
        /**
         * Gets the previously created event if it exists
         *
         * @return The previous event if it exists, null otherwise
         */
        Event getRecentEvent();

        /**
         * Gets the event currently being created
         *
         * @return The current event if it exists, null otherwise
         */
        Event getCurrentEvent();

        /**
         * Method called in the parent to update the event object to have the user's selected
         * address, and to switch to the next setup fragment
         */
        void updateAddress(ParseGeoPoint address, String addressString);
    }
}
