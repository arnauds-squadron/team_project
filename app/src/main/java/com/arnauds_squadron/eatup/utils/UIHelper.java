package com.arnauds_squadron.eatup.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Utility class to help with some necessary UI updates but that aren't specific to any fragment
 */
public class UIHelper {

    // 1 (zoomed out) - 19 (too close)
    public final static float DEFAULT_MAP_ZOOM_LEVEL = 15;

    // Shows name of restaurant if it's decently well known
    public final static float DETAILED_MAP_ZOOM_LEVEL = 18;

    /**
     * Given an activity context and a view, it will hide the soft Android keyboard from that view
     * @param context The activity context (getActivity() from within a fragment)
     * @param view The view (getView() from within a fragment)
     */
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Activity.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
