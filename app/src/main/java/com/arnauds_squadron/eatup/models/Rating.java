package com.arnauds_squadron.eatup.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Ratings")
public class Rating extends ParseObject {
    private static final String KEY_NUM_RATINGS_GUEST = "numRatingsGuest";
    private static final String KEY_AVG_RATINGS_GUEST = "avgRatingsGuest";
    private static final String KEY_NUM_RATINGS_HOST = "numRatingsHost";
    private static final String KEY_AVG_RATINGS_HOST = "avgRatingsHost";
    private static final String KEY_USER = "user";

    public Number getNumRatingsGuest() {
        return getNumber(KEY_NUM_RATINGS_GUEST);
    }

    public void setNumRatingsGuest(int numRatings) {
        put(KEY_NUM_RATINGS_GUEST, numRatings);
    }

    public Number getAvgRatingGuest() {
        return getNumber(KEY_AVG_RATINGS_GUEST);
    }

    public void setAvgRatingGuest(float avgRating) {
        put(KEY_AVG_RATINGS_GUEST, avgRating);
    }

    public Number getNumRatingsHost() {
        return getNumber(KEY_NUM_RATINGS_HOST);
    }

    public void setNumRatingsHost(int numRatings) {
        put(KEY_NUM_RATINGS_HOST, numRatings);
    }

    public Number getAvgRatingHost() {
        return getNumber(KEY_AVG_RATINGS_HOST);
    }

    public void setAvgRatingHost(float avgRating) {
        put(KEY_AVG_RATINGS_HOST, avgRating);
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

