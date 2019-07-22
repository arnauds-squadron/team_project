package com.arnauds_squadron.eatup.models;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;

import static com.arnauds_squadron.eatup.utils.Constants.PENDING_GUESTS;

@ParseClassName("Event")
public class Event extends ParseObject {
    public static final String KEY_EVENT_IMAGE = "eventImage";
    public static final String KEY_DATE = "date";
    public static final String KEY_TITLE = "title";
    public static final String KEY_HOST = "host";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_FOOD_TYPE = "foodType";
    public static final String KEY_MAX_GUESTS = "maxGuests";
    public static final String KEY_21 = "over21";
    public static final String KEY_RESTAURANT = "isRestaurant";
    public static final String KEY_CONVERSATION = "conversation";
    public static final String KEY_PENDING_GUESTS = "pendingGuests";
    public static final String KEY_UPDATED_GUESTS = "updatedGuests";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final Double MAX_DISTANCE = 0.1;


    public Event() {}
    // ParseFile - class in SDK that allows accessing files stored with Parse
    public ParseFile getEventImage() {
        return getParseFile(KEY_EVENT_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_EVENT_IMAGE, image);
    }

    public Date getDate() {
        return getDate(KEY_DATE);
    }

    public void setDate(Date date) {
        put(KEY_DATE, date);
    }

    public String getTitle() {
        return getString(KEY_TITLE);
    }

    public void setTitle(String title) {
        put(KEY_TITLE, title);
    }

    public ParseUser getHost() {
        return getParseUser(KEY_HOST);
    }

    public void setHost(ParseUser host) {
        put(KEY_HOST, host);
    }

    // TODO figure out how to turn geopointer into string and vice versa
    public ParseGeoPoint getAddress() {
        return getParseGeoPoint(KEY_ADDRESS);
    }

    public void setAddress(ParseGeoPoint address) {
        put(KEY_ADDRESS, address);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    // TODO figure out if this should be set as multiple tags in array rather than a string
    public String getCuisine() {
        return getString(KEY_FOOD_TYPE);
    }

    public void setCuisine(String foodType) {
        put(KEY_FOOD_TYPE, foodType);
    }

    public int getMaxGuests() {
        return getInt(KEY_MAX_GUESTS);
    }

    public void setMaxGuests(int maxGuests) {
        put(KEY_MAX_GUESTS, maxGuests);
    }

    public Boolean getOver21() {
        return getBoolean(KEY_21);
    }

    public void setOver21(Boolean over21) {
        put(KEY_21, over21);
    }

    public Boolean getRestaurant() {
        return getBoolean(KEY_RESTAURANT);
    }

    public void setRestaurant(Boolean restaurant) {
        put(KEY_RESTAURANT, restaurant);
    }

    // TODO how to access conversation/do we actually need to use the create/update at methods

    // inner class to query event model
    public static class Query extends ParseQuery<Event> {
        //
        public Query() {
            super(Event.class);
        }

        public Query getOlder(Date maxId) {
            whereLessThan("createdAt", maxId);
            return this;
        }

        // get most recent 20 posts
        public Query getTop() {
            setLimit(20);
            orderByDescending(KEY_CREATED_AT);
            // builder pattern, allow chain methods
            return this;
        }

        public Query withHost() {
            include("host");
            return this;
        }

        public Query getClosest(ParseGeoPoint location) {
            whereWithinMiles(KEY_ADDRESS, location, MAX_DISTANCE);
            return this;
        }
    }

    public void createRequest(ParseUser user, Event event) {
        event.addUnique(PENDING_GUESTS, user.getUsername());
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("createRequest", "RSVP requested");
                }
                else {
                    Log.d("createRequest", "Error in making request. Try again.");
                }
            }
        });
    }
}
