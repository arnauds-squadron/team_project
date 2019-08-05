package com.arnauds_squadron.eatup.local.setup;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Business;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.FormatHelper;
import com.arnauds_squadron.eatup.utils.UIHelper;
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
 * Fragment that asks the user to input the date and time of their event through a series of
 * input dialogs.
 */
public class DateFragment extends Fragment implements OnMapReadyCallback {

    @BindView(R.id.tvAddress)
    TextView tvAddress;

    @BindView(R.id.tvTags)
    TextView tvTags;

    @BindView(R.id.tvSelectedDate)
    TextView tvSelectedDate;

    @BindView(R.id.tvSelectedTime)
    TextView tvSelectedTime;

    @BindView(R.id.ivCalendar)
    ImageView calendar;

    @BindView(R.id.ivClock)
    ImageView clock;

    @BindView(R.id.tvMaxGuests)
    TextView tvMaxGuests;

    @BindView(R.id.cbOver21)
    CheckBox cbOver21;

    @BindView(R.id.etEventTitle)
    EditText etEventTitle;

    private OnFragmentInteractionListener mListener;

    private Event event;
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
            UIHelper.hideKeyboard(getActivity(), getView());
            event = mListener.getCurrentEvent();

            SupportMapFragment mapFragment = (SupportMapFragment)
                    getChildFragmentManager().findFragmentById(R.id.mapFragment);
            mapFragment.getMapAsync(this); // update the map

            if (event.getDate() != null) { // recent event, fill in all the fields
                setPreviousFields();
            } else {
               showDatePickerDialog();
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
     * Communicates with the LocalFragment with the listener to create the event with the
     * given event name. Requires the name to not be empty or just spaces.
     */
    @OnClick(R.id.btnCreateEvent)
    public void createEvent() {
        final String eventTitle = etEventTitle.getText().toString().trim();

        if (TextUtils.isEmpty(eventTitle)) {
            Toast.makeText(getActivity(), "Give your event a name!", Toast.LENGTH_SHORT).show();
            return;
        }

        Context context = getContext();
        //call the HomeDetailsActivity.apiAuth to get the Authorization and return a service for the ApiResponse
        // if we have a response, then get the specific information defined in the Business Class
        Call<YelpApiResponse> meetUp = YelpData.retrofit(context).getLocation(
                event.getAddress().getLatitude(), event.getAddress().getLongitude(),
                event.getTags().get(0), 50);

        meetUp.enqueue(new Callback<YelpApiResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<YelpApiResponse> call,
                                   @NonNull retrofit2.Response<YelpApiResponse> response) {
                if (response.isSuccessful()) {
                    YelpApiResponse yelpApiResponse = response.body();
                    if (yelpApiResponse != null && yelpApiResponse.businessList.size() > 0) {
                        Business restaurant = yelpApiResponse.businessList.get(0);
                        mListener.createEvent(eventTitle, restaurant.imageUrl);
                    } else {
                        mListener.createEvent(eventTitle, null);
                    }
                } else {
                    mListener.createEvent(eventTitle, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<YelpApiResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * Can't just call showDatePickerDialog because we don't also want to query the user for a new
     * time if they just messed up the date
     */
    @OnClick(R.id.tvSelectedDate)
    public void updateDate() {
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
                updateTimeTextViews();
            }
        }, currentYear, currentMonth, currentDay);

        datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int buttonId) {
                dateSet = false;
            }
        });

        datePicker.show();
    }

    /**
     * Allows the user to change the time they already set.
     */
    @OnClick(R.id.tvSelectedTime)
    public void updateTime() {
        showTimePickerDialog();
    }

    /**
     * Creates the DatePickerDialog programmatically and initializes it with the current date
     */
    private void showDatePickerDialog() {
        selectedTime = Calendar.getInstance();
        int currentYear = selectedTime.get(Calendar.YEAR);
        int currentMonth = selectedTime.get(Calendar.MONTH);
        int currentDay = selectedTime.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        selectedTime.set(Calendar.YEAR, year);
                        selectedTime.set(Calendar.MONTH, month);
                        selectedTime.set(Calendar.DAY_OF_MONTH, day);
                        dateSet = true;
                        showTimePickerDialog();
                    }
                }, currentYear, currentMonth, currentDay);

        datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int buttonId) {
                        dateSet = false;
                        showTimePickerDialog();
                    }
                });
        datePicker.show();
    }

    /**
     * Creates the TimePickerDialog programmatically after the DatePickerDialog has finished,
     * and initializes it with the current time
     */
    private void showTimePickerDialog() {
        int currentHour = selectedTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = selectedTime.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                        selectedTime.set(Calendar.MINUTE, minute);
                        timeSet = true;
                        updateTimeTextViews();
                    }
                }, currentHour, currentMinute, true);

        timePicker.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int buttonId) {
                        timeSet = false;
                        updateTimeTextViews();
                    }
                });
        timePicker.show();
    }

    /**
     * Update the date and time text views and handle if the time wasn't set
     */
    private void updateTimeTextViews() {
        if (!dateSet)
            tvSelectedDate.setText(getString(R.string.event_creation_date_not_selected));
        else
            tvSelectedDate.setText(FormatHelper.formatDateWithMonthNames(selectedTime.getTime()));

        if (!timeSet)
            tvSelectedTime.setText(getString(R.string.event_creation_time_not_selected));
        else
            tvSelectedTime.setText(FormatHelper.formatTime(selectedTime.getTime(), getActivity()));

        calendar.setVisibility(View.VISIBLE);
        clock.setVisibility(View.VISIBLE);
    }

    /**
     * Initialize all the TextViews and details of the recent event
     */
    private void setPreviousFields() {
        tvTags.setText(FormatHelper.listToString(event.getTags()));
        tvMaxGuests.setText(String.format(Locale.ENGLISH, "%d", event.getMaxGuests()));
        cbOver21.setChecked(event.getOver21());
        tvAddress.setText(event.getAddressString());
        tvSelectedDate.setText(FormatHelper.formatDateWithMonthNames(event.getDate()));
        tvSelectedTime.setText(FormatHelper.formatTime(event.getDate(), getActivity()));
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
        void createEvent(String eventTitle, String imageUrl);
    }
}
