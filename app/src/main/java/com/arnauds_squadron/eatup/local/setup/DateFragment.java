package com.arnauds_squadron.eatup.local.setup;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.utils.FormatHelper;
import com.arnauds_squadron.eatup.utils.UIHelper;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Fragment that asks the user to input the date and time of their event through a series of
 * input dialogs.
 */
public class DateFragment extends Fragment {

    @BindView(R.id.tvSelectedDate)
    TextView tvSelectedDate;

    @BindView(R.id.tvSelectedTime)
    TextView tvSelectedTime;

    @BindView(R.id.ivCalendar)
    ImageView calendar;

    @BindView(R.id.ivClock)
    ImageView clock;


    private OnFragmentInteractionListener mListener;
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
            // delay ui thread to show date after keyboard has disappeared
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showDatePickerDialog();
                }
            }, 250);
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
     * Communicates with the LocalFragment to move to the next fragment once the date and time are
     * set.
     */
    @OnClick(R.id.btnNext)
    public void goToNextFragment() {
        if (dateSet && timeSet)
            mListener.updateDate(selectedTime.getTime());
        else
            Toast.makeText(getActivity(), "Select a date and time!", Toast.LENGTH_SHORT).show();
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

    public interface OnFragmentInteractionListener {
        /**
         * When called by the parent fragment, it should switch to the next fragment in the
         * setup queue
         */
        void updateDate(Date date);
    }
}
