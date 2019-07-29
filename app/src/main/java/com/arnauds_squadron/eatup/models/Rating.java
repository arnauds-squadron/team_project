package com.arnauds_squadron.eatup.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Ratings")
public class Rating extends ParseObject {
    private static final String KEY_NUM_RATINGS = "numRatings";
    private static final String KEY_AVG_RATINGS = "averageRating";
    private static final String KEY_USER = "user";

    public Number getNumRatings() {
        return getNumber(KEY_NUM_RATINGS);
    }

    public void setNumRatings(int numRatings) {
        put(KEY_NUM_RATINGS, numRatings);
    }

    public Number getAvgRating() {
        return getNumber(KEY_AVG_RATINGS);
    }

    public void setAvgRating(float avgRating) {
        put(KEY_AVG_RATINGS, avgRating);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    // inner class to query rating model
    public static class Query extends ParseQuery<Rating> {

        public Query() {
            super(Rating.class);
        }

    }

}

