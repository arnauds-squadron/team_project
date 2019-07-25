package com.arnauds_squadron.eatup.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Date;

/**
 * Helper class to help format dates and times
 */
public class FormatHelper {

    /**
     * Function to format the given Date to (Jul 4, 1776 or Jun 24, 2019)
     *
     * @return A String in the format MMM dd, YYYY
     */
    public static String formatDateWithMonthNames(Date date) {
        return DateFormat.format("MMM dd, yyyy", date).toString();
    }

    /**
     * Function to obtain the time information in the given Date object and correctly formats the
     * time depending on if the user uses 24 hour time or 12 hour time
     *
     * @param context Context to get the user's time preference
     * @return A String in the format HH:mm if the user is in 24 hour time, or hh:mm a if the user
     * is in 12 hour time
     */
    public static String formatTime(Date date, Context context) {
        boolean uses24HourTime = android.text.format.DateFormat.is24HourFormat(context);

        if (uses24HourTime)
            return DateFormat.format("HH:mm", date).toString();
        else
            return DateFormat.format("hh:mm a", date).toString();
    }

    // TODO: implement this correctly (temp right now)
    /**
     * Formats a timestamp to show the time of the message if it was on the same day,
     * or the date if it was longer ago
     *
     * @return A correctly formatted date that gives the most useful information on how
     * long ago a timestamp was
     */
    public static String formatTimestamp(Date date) {
        return formatDateWithMonthNames(date);
    }
}
