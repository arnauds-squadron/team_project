package com.arnauds_squadron.eatup.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.List;

@ParseClassName("Chat")
public class Chat extends ParseObject {
    private static final String KEY_NAME = "name";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_MEMBERS = "members";
    private static final String KEY_MESSAGES = "messages";
    private static final String KEY_UPDATED_AT = "updatedAt";

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    public JSONArray getMembers() {
        return getJSONArray(KEY_MEMBERS);
    }

    public void addMember(ParseUser member) {
        add(KEY_MEMBERS, member);
    }

    public void addMembers(List<ParseUser> members) {
        addAll(KEY_MEMBERS, members);
    }

    public JSONArray getMessages() {
        return getJSONArray(KEY_MESSAGES);
    }

    public void addMessage(String message) {
        add(KEY_NAME, message);
    }

    // inner class to query event model
    public static class Query extends ParseQuery<Chat> {
        private final static int QUERY_LIMIT = 25;

        public Query() {
            super(Chat.class);
        }

        // Only get the first 10 chats
        public Query setQueryLimit() {
            setLimit(QUERY_LIMIT);
            return this;
        }

        // get most recently active chats
        public Query getTop() {
            orderByDescending(KEY_UPDATED_AT);
            return this;
        }
    }
}
