package com.arnauds_squadron.eatup.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Home")
public class Home extends ParseObject {

    private static final String KEY_PERSON = "Person";
    private static final String KEY_PLACE = "Place";
    private static final String KEY_DATE = "Date";

    public Home() {}

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
}
