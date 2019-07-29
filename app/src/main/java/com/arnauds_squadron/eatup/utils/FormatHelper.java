package com.arnauds_squadron.eatup.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
            return DateFormat.format("h:mm a", date).toString();
    }

    /**
     * Formats a timestamp to show the time of the message if it was on the same day,
     * or the date if it was longer ago.
     * <p>
     * Shows the time if it was sent on the same day, shows the name of the day if the message
     * was recent, and shows the actual date for any later dates.
     *
     * @return A correctly formatted date that gives the most useful information on how
     * long ago a timestamp was
     */
    public static String formatTimestamp(Date date, Context context) {
        long millis = new Date().getTime() - date.getTime();
        long days = TimeUnit.DAYS.convert(millis, TimeUnit.MILLISECONDS);

        if (days < 1) { // show the time if it was very recent
            return formatTime(date, context);
        } else if (days < 6) { // show the name of the day if it was this week
            return DateFormat.format("EEE", date).toString();
        } else if (days < 365) { // show the date without the year if it was this year
            return DateFormat.format("MMM, dd", date).toString();
        } else { // show a regular date
            return formatDateWithMonthNames(date);
        }
    }

    /**
     * Concatenates all the elements of the given list into a comma separated String
     *
     * @param list A list of string elements
     * @return A comma separated String of elements
     */
    public static String listToString(List<String> list) {
        StringBuilder returnString = new StringBuilder();

        for (String item : list)
            returnString.append(", ").append(item);

        return returnString.substring(2);
    }
}
