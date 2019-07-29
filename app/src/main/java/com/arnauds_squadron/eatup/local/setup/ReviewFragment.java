package com.arnauds_squadron.eatup.local.setup;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.FormatHelper;
import com.arnauds_squadron.eatup.utils.UIHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment that displays all the selected fields and will create the event when confirmed
 */
public class ReviewFragment extends Fragment implements OnMapReadyCallback {

    @BindView(R.id.tvTags)
    TextView tvTags;

    @BindView(R.id.tvMaxGuests)
    TextView tvMaxGuests;

    @BindView(R.id.cbOver21)
    CheckBox cbOver21;

    @BindView(R.id.tvAddress)
    TextView tvAddress;

    @BindView(R.id.tvSelectedDate)
    TextView tvSelectedDate;

    @BindView(R.id.tvSelectedTime)
    TextView tvSelectedTime;

    @BindView(R.id.etEventTitle)
    EditText etEventTitle;

    private OnFragmentInteractionListener mListener;
    private Event event;

    public static ReviewFragment newInstance() {
        return new ReviewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_review, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    /**
     * Method to only initialize the vies when this fragment is visible (don't initialize views
     * before the dates are selected)
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            event = mListener.getCurrentEvent();
            initializeViews();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    /**
     * Communicates with the LocalFragment with the listener to create the event with the
     * given event name. Requires the name to not be empty or just spaces.
     */
    @OnClick(R.id.btnCreateEvent)
    public void createEvent() {
        String eventTitle = etEventTitle.getText().toString().trim();

        if (!eventTitle.isEmpty())
            mListener.createEvent(eventTitle);
        else
            Toast.makeText(getActivity(), "Give your event a name!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Initialize all the TextViews and details of the current event
     */
    private void initializeViews() {
        tvTags.setText(FormatHelper.listToString(event.getTags()));
        tvMaxGuests.setText(String.format(Locale.ENGLISH, "%d", event.getMaxGuests()));
        cbOver21.setChecked(event.getOver21());
        tvAddress.setText(event.getAddressString());
        tvSelectedDate.setText(FormatHelper.formatDateWithMonthNames(event.getDate()));
        tvSelectedTime.setText(FormatHelper.formatTime(event.getDate(), getActivity()));

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this); // update the map
    }

    /**
     * Called whenever getMapAsync() is called, and sets up the map
     *
     * @param googleMap The map obtained from the maps API
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng eventLocation = new LatLng(event.getAddress().getLatitude(),
                event.getAddress().getLongitude());

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation,
                UIHelper.DETAILED_MAP_ZOOM_LEVEL));

        googleMap.addMarker(new MarkerOptions()
                .position(eventLocation)
                .title(event.getTitle())
                .snippet("snippet")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    public interface OnFragmentInteractionListener {
        /**
         * Gets the created event from the parent fragment
         */
        Event getCurrentEvent();

        /**
         * Method that triggers the event creation method in the parent fragment
         */
        void createEvent(String eventTitle);
    }
}
