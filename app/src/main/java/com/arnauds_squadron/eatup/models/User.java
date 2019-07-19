package com.arnauds_squadron.eatup.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("User")
public class User extends ParseObject {

    private static final String KEY_PERSON = "Person";
    private static final String KEY_PLACE = "Place";
    private static final String KEY_DATE = "Date";
    private static final String KEY_USER = "username";
    private static final String KEY_RATING = "averageRating";

    public User() {}

    public void setUsername(String username) {
        put(KEY_USER, username);
    }
    public String getUsername() {
        return getString(KEY_USER);
    }
    public void setPerson(String person) {
        put(KEY_PERSON, person);
    }

    public void setPlace(String place) {
        put(KEY_PLACE, place);
    }

    public void setDate(String date) {
        put(KEY_DATE, date);
    }

    public String getPerson() {
        return getString(KEY_PERSON);

    }

    public String getPlace() {
        return getString(KEY_PLACE);

    }

    public String getDate() {
        return getString(KEY_DATE);
    }

    // TODO fix this method to take the average of all ratings
//    public void setRating(float Rating) {
//        put(KEY_RATING, Rating);
//    }

    public Float getFloatRating() {
        return getNumber(KEY_RATING).floatValue();
    }
}
