package com.arnauds_squadron.eatup.utils;

import com.parse.ParseUser;

public final class Constants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";
    public static final String KEY_PROFILE_PICTURE = "profilePicture";
    public static final String BIO = "bio";
    public static final String DISPLAY_NAME = "displayName";
    public static final String AVERAGE_RATING = "averageRating";
    public static final float NO_RATING = 0f;
    public static final String NUM_RATINGS = "numRatings";
    // search constants
    public static final String SEARCH_CATEGORY = "searchCategory";
    public static final int NO_SEARCH = 0;
    public static final int USER_SEARCH = 1;
    public static final int CUISINE_SEARCH = 2;
    public static final int LOCATION_SEARCH = 3;

    // PagerAdapter constants
    public static final int MAIN_PAGER_START_PAGE = 2;

    public static ParseUser CURRENT_USER;
}