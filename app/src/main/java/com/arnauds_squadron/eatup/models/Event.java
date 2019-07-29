package com.arnauds_squadron.eatup.models;

import android.util.Log;

import com.arnauds_squadron.eatup.R;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@ParseClassName("Event")
public class Event extends ParseObject {
    private static final String KEY_EVENT_IMAGE = "eventImage";
    private static final String KEY_DATE = "date";
    private static final String KEY_TITLE = "title";
    private static final String KEY_HOST = "host";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_ADDRESS_STRING = "addressString";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_FOOD_TYPE = "foodType";
    private static final String KEY_TAGS = "tags";
    private static final String KEY_MAX_GUESTS = "maxGuests";
    private static final String KEY_21 = "over21";
    private static final String KEY_RESTAURANT = "isRestaurant";
    private static final String KEY_CHAT = "chat";
    private static final String KEY_ALL_REQUESTS = "allRequests";
    private static final String KEY_PENDING_GUESTS = "pendingGuests";
    private static final String KEY_ACCEPTED_GUESTS = "acceptedGuests";
    private static final String KEY_CREATED_AT = "createdAt";
    private static final Double MAX_DISTANCE = 0.1;

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

    public String getAddressString() {
        return getString(KEY_ADDRESS_STRING);
    }

    public void setAddressString(String address) {
        put(KEY_ADDRESS_STRING, address);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public JSONArray getTags() {
        return getJSONArray(KEY_TAGS);
    }

    /**
     * Sets the tags for this event, overwriting any previous tags
     */
    public void setTags(List<String> tags) {
        put(KEY_TAGS, new JSONArray(tags));
    }

    // TODO: replace with the tag system
    public String getCuisine() {
        return getString(KEY_FOOD_TYPE);
    }

    public void setCuisine(String foodType) {
        put(KEY_FOOD_TYPE, foodType);
    }

    public JSONArray getAcceptedGuests() {
        return getJSONArray(KEY_ACCEPTED_GUESTS);
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

    public Chat getChat() {
        return (Chat) get(KEY_CHAT);
    }

    public void addChat(Chat chat) {
        put(KEY_CHAT, chat);
    }

    public List<ParseUser> getPendingRequests() {
        return getList(KEY_PENDING_GUESTS);
    }

    public List<ParseUser> getAllRequests() {
        return getList(KEY_ALL_REQUESTS);
    }

    /**
     * Adds the user to this event's pending guests lists, and they must be accepted or denied
     * later by the host
     *
     * @param user  The user to be added to the pending guests list
     * @param event The event they are RSVPing for
     */
    public void createRequest(ParseUser user, Event event) {
        event.addUnique(KEY_PENDING_GUESTS, user);
        event.addUnique(KEY_ALL_REQUESTS, user);
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("createRequest", "RSVP requested");
                } else {
                    Log.d("createRequest", "Error in making request. Try again.");
                }
            }
        });
    }

    public Boolean checkRequest(ParseUser user, Event event) {
        if (event.getAllRequests() != null) {
            List<ParseUser> userRequests = event.getAllRequests();
            for (int i = 0; i < userRequests.size(); i++) {
                if (userRequests.get(i) == user) {
                    return true;
                }
            }
        }
        return false;
    }

    public void handleRequest(ParseUser user, boolean isAccepted) {
        List<ParseUser> tempList = new ArrayList<>();
        tempList.add(user);
        removeAll(KEY_PENDING_GUESTS, tempList);

        if (isAccepted) {
            if (getAcceptedGuests() == null)
                put(KEY_ACCEPTED_GUESTS, new JSONArray());
            add(KEY_ACCEPTED_GUESTS, user);
        }
    }

    // inner class to query event model
    public static class Query extends ParseQuery<Event> {
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
}