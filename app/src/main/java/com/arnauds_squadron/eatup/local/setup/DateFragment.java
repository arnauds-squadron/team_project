package com.arnauds_squadron.eatup.local.setup;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Business;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.Constants;
import com.arnauds_squadron.eatup.utils.FormatHelper;
import com.arnauds_squadron.eatup.yelp_api.YelpApiResponse;
import com.arnauds_squadron.eatup.yelp_api.YelpData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Fragment that asks the user to input the date and time of their recentEvent through a series of
 * input dialogs.
 */
public class DateFragment extends Fragment implements OnMapReadyCallback {

    @BindView(R.id.tvTags)
    TextView tvTags;

    @BindView(R.id.tvSelectedDate)
    TextView tvSelectedDate;

    @BindView(R.id.tvSelectedTime)
    TextView tvSelectedTime;

    @BindView(R.id.ivCalendar)
    ImageView ivCalendar;

    @BindView(R.id.ivClock)
    ImageView ivClock;

    @BindView(R.id.tvMaxGuests)
    TextView tvMaxGuests;

    @BindView(R.id.ivMaxGuests)
    ImageView ivMaxGuests;

    @BindView(R.id.cbOver21)
    CheckBox cbOver21;

    @BindView(R.id.etEventTitle)
    EditText etEventTitle;

    @BindView(R.id.tvRestaurantName)
    TextView tvRestaurantName;

    private OnFragmentInteractionListener mListener;

    private Event currentEvent;
    private Event recentEvent;
    // Calendar object that holds the date and time that the user selects
    private Calendar selectedTime;
    // Booleans to make sure the user selects a time before moving on
    private boolean dateSet;
    private boolean timeSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_date, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    /**
     * Method to create the date picker only when the date fragment is actually visible
     * to the users
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            selectedTime = Calendar.getInstance();

            currentEvent = mListener.getCurrentEvent();
            recentEvent = mListener.getRecentEvent();
            setCurrentEventFields();

            if (recentEvent != null) { // recent event, fill in all the fields
                selectedTime.setTime(recentEvent.getDate());
                dateSet = true;
                timeSet = true;
                setRecentEventFields();
            } else { // reset recent event fields in case the user undoes changes and returns
                dateSet = false;
                timeSet = false;
                resetRecentEventFields();
                showDatePickerDialog(true);
            }
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
     * Communicates with the LocalFragment with the listener to create the recentEvent with the
     * given recentEvent name. Requires the name to not be empty or just spaces.
     */
    @OnClick(R.id.btnCreateEvent)
    public void createEvent() {
        final String eventTitle = etEventTitle.getText().toString().trim();

        if (TextUtils.isEmpty(eventTitle)) {
            Toast.makeText(getActivity(), "Give your event a name!", Toast.LENGTH_SHORT).show();
            return;
        }

        //call the HomeDetailsActivity.apiAuth to get the Authorization and return a service for the
        // ApiResponse if we have a response, then get the specific information defined in the
        // Business Class

        Call<Business> meetUp = YelpData.retrofit(getContext()).getDetails(currentEvent.getYelpId());
        meetUp.enqueue(new Callback<Business>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<Business> call,
                                   @NonNull retrofit2.Response<Business> response) {
                if (response.isSuccessful()) {
                    Business yelpApiResponse = response.body();
                    if (yelpApiResponse != null) {
                        currentEvent.setYelpImage(yelpApiResponse.imageUrl);
                    }
                }
                updateEventFields();
            }

            @Override
            public void onFailure(@NonNull Call<Business> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * Calls the parent fragment to update the new recentEvent with the new fields
     */
    private void updateEventFields() {
        String title = etEventTitle.getText().toString().trim();
        Date date = selectedTime.getTime();
        int maxGuests = Integer.parseInt(tvMaxGuests.getText().toString());
        boolean over21 = cbOver21.isChecked();

        mListener.updateFinalFields(title, date, maxGuests, over21);
    }

    /**
     * Updates the date parameter and updates only the date TextView when a new date is set
     */
    @OnClick(R.id.tvSelectedDate)
    public void updateDate() {
        showDatePickerDialog(false);
    }

    /**
     * Updates the time parameter and updates only the time TextView when a new time is set
     */
    @OnClick(R.id.tvSelectedTime)
    public void updateTime() {
        showTimePickerDialog(false);
    }

    /**
     * Shows the user a popup to select the date of the recentEvent. Default date is the current date
     *
     * @param shouldShowNextDialog Shows the TimePickerDialog if true, just updates the date
     *                             TextView if false
     */
    private void showDatePickerDialog(final boolean shouldShowNextDialog) {
        int currentYear = selectedTime.get(Calendar.YEAR);
        int currentMonth = selectedTime.get(Calendar.MONTH);
        int currentDay = selectedTime.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                selectedTime.set(Calendar.YEAR, year);
                selectedTime.set(Calendar.MONTH, month);
                selectedTime.set(Calendar.DAY_OF_MONTH, day);
                dateSet = true;
                if (shouldShowNextDialog)
                    showTimePickerDialog(true);
                else
                    updateNewFieldTextViews();
            }
        }, currentYear, currentMonth, currentDay);

        datePicker.setCanceledOnTouchOutside(false);
        datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int buttonId) {
                dateSet = false;
                if (!shouldShowNextDialog)
                    updateNewFieldTextViews();
            }
        });
        datePicker.show();
    }

    /**
     * Creates the TimePickerDialog programmatically after the DatePickerDialog has finished,
     * and initializes it with the current time
     */
    private void showTimePickerDialog(final boolean shouldShowNextDialog) {
        int currentHour = selectedTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = selectedTime.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                        selectedTime.set(Calendar.MINUTE, minute);
                        timeSet = true;
                        if (shouldShowNextDialog) {
                            showMaxGuestsDialog();
                        } else {
                            updateNewFieldTextViews();
                        }
                    }
                }, currentHour, currentMinute, true);

        timePicker.setCanceledOnTouchOutside(false);
        timePicker.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int buttonId) {
                        timeSet = false;
                        if (!shouldShowNextDialog) {
                            updateNewFieldTextViews();
                        }
                    }
                });
        timePicker.show();
    }

    /**
     * Shows a popup for the user to select the maximum number of guests for their recentEvent. Minimum is
     * 1 guest, maximum is 100 guests
     */
    @OnClick({R.id.tvMaxGuests, R.id.ivMaxGuests})
    public void showMaxGuestsDialog() {
        final Dialog d = new Dialog(getActivity());
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.dialog_max_guests);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        d.setCanceledOnTouchOutside(false);

        final NumberPicker npMaxGuests = d.findViewById(R.id.npMaxGuests);
        npMaxGuests.setMinValue(1);
        npMaxGuests.setMaxValue(Constants.MAX_GUESTS);
        npMaxGuests.setWrapSelectorWheel(false);
        // TODO: set the start value to the previous selection?

        Button btnSet = d.findViewById(R.id.btnSet);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = npMaxGuests.getValue() + "";
                tvMaxGuests.setText(text);
                d.dismiss();
                updateNewFieldTextViews();
            }
        });
        d.show();
    }

    /**
     * Update the date and time text views and handle if the time wasn't set
     */
    private void updateNewFieldTextViews() {
        toggleImageViews(true);

        if (!dateSet)
            tvSelectedDate.setText(getString(R.string.event_creation_date_not_selected));
        else
            tvSelectedDate.setText(FormatHelper.formatDateWithMonthNames(selectedTime.getTime()));

        if (!timeSet)
            tvSelectedTime.setText(getString(R.string.event_creation_time_not_selected));
        else
            tvSelectedTime.setText(FormatHelper.formatTime(selectedTime.getTime(), getActivity()));
    }

    /**
     * Sets the values that have been set by the user through the wizard (address and tags)
     */
    private void setCurrentEventFields() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this); // update the map

        tvTags.setText(FormatHelper.listToString(currentEvent.getTags()));
        tvRestaurantName.setText(currentEvent.getYelpRestaurant());
    }

    /**
     * Resets the recent event fields if the user goes back and starts a new event and returns to
     * this fragment. Reverts everything done by setRecentEventFields()
     */
    private void resetRecentEventFields() {
        toggleImageViews(false);

        etEventTitle.setText("");
        tvMaxGuests.setText("");
        cbOver21.setChecked(false);
        tvSelectedDate.setText("");
        tvSelectedTime.setText("");
    }

    /**
     * Initialize all the fields that already have values in the recent event the user selected.
     * Also sets the images to visible.
     */
    private void setRecentEventFields() {
        toggleImageViews(true);

        etEventTitle.setText(recentEvent.getTitle());
        tvMaxGuests.setText(String.format(Locale.ENGLISH, "%d", recentEvent.getMaxGuests()));
        cbOver21.setChecked(recentEvent.getOver21());
        tvSelectedDate.setText(FormatHelper.formatDateWithMonthNames(recentEvent.getDate()));
        tvSelectedTime.setText(FormatHelper.formatTime(recentEvent.getDate(), getActivity()));
    }

    /**
     * Show all the ImageViews that are hidden by default if the user hasn't set a date, time, etc.
     */
    private void toggleImageViews(boolean isVisible) {
        ivCalendar.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        ivClock.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        ivMaxGuests.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        cbOver21.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Called whenever getMapAsync() is called, and sets up the map. Uses the current event's
     * location since that will always be set.
     *
     * @param googleMap The map obtained from the maps API
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng eventLocation = new LatLng(currentEvent.getAddress().getLatitude(),
                currentEvent.getAddress().getLongitude());

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation,
                Constants.DETAILED_MAP_ZOOM));

        googleMap.addMarker(new MarkerOptions()
                .position(eventLocation)
                .title(currentEvent.getTitle())
                .snippet("snippet")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    public interface OnFragmentInteractionListener {
        /**
         * Gets the created recentEvent from the parent fragment
         */
        Event getRecentEvent();

        /**
         * Gets the event currently being created from the parent fragment, only used to set the
         * event's location and tags
         */
        Event getCurrentEvent();

        /**
         * Method that triggers the recentEvent creation method in the parent fragment
         */
        void updateFinalFields(String title, Date date, int maxGuests, boolean over21);
    }
}
