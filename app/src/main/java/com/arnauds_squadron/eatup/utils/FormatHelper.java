package com.arnauds_squadron.eatup.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Date;

public class FormatHelper {
    //TODO: add documentation

    public static String formatDateWithMonthNames(Date date) {
        return DateFormat.format("MMM dd, yyyy", date).toString();
    }

    public static String formatTime(Date date, Context context) {
        boolean uses24HourTime = android.text.format.DateFormat.is24HourFormat(context);

        if (uses24HourTime)
            return DateFormat.format("HH:mm", date).toString();
        else
            return DateFormat.format("hh:mm a", date).toString();
    }
}
