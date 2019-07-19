package com.arnauds_squadron.eatup.local.setup;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.arnauds_squadron.eatup.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment that asks the user to input the date and time of their event through a series of
 * input dialogs.
 */
public class DateFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.tvSelectedDate)
    TextView tvSelectedDate;

    @BindView(R.id.tvSelectedTime)
    TextView tvSelectedTime;

    // Calendar object that holds the date and time that the user selects
    private Calendar selectedTime;

    public static DateFragment newInstance() {
        Bundle args = new Bundle();
        DateFragment fragment = new DateFragment();
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

        if (isVisibleToUser)
            // TODO: delay dialog so keyboard is hidden
            showDatePickerDialog();
    }

    /**
     * Creates the DatePickerDialog programmatically and initializes it with the current date
     */
    private void showDatePickerDialog() {
        selectedTime = Calendar.getInstance();
        int currentYear = selectedTime.get(Calendar.YEAR);
        int currentMonth = selectedTime.get(Calendar.MONTH);
        int currentDay = selectedTime.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog( getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                selectedTime.set(Calendar.YEAR, year);
                selectedTime.set(Calendar.MONTH, month);
                selectedTime.set(Calendar.DAY_OF_MONTH, day);

                String dateText = month + "/" + day + "/" + year;
                tvSelectedDate.setText(dateText);
                showTimePickerDialog();
            }
        }, currentYear, currentMonth, currentDay);

        datePicker.show();
    }

    /**
     * Creates the TimePickerDialog programmatically after the DatePickerDialog has finished,
     * and initializes it with the current time
     */
    private void showTimePickerDialog() {
        int currentHour = selectedTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = selectedTime.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                selectedTime.add(Calendar.HOUR, hour);
                selectedTime.add(Calendar.MINUTE, minute);

                String timeText = hour + ":" + String.format(Locale.ENGLISH, "%02d", minute);
                tvSelectedTime.setText(timeText);
            }
        }, currentHour, currentMinute, true);

        timePicker.show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        // TODO: require date and time
        mListener.updateDate(selectedTime.getTime());
    }

    public interface OnFragmentInteractionListener {
        /**
         * When called by the parent fragment, it should switch to the next fragment in the
         * setup queue
         */
        void updateDate(Date date);
    }
}
