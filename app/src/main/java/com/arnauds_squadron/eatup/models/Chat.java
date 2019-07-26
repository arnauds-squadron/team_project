package com.arnauds_squadron.eatup.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;

@ParseClassName("Chat")
public class Chat extends ParseObject {
    private static final String KEY_NAME = "name";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_MEMBERS = "members";
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

    public void addMember(String userId) {
        add(KEY_MEMBERS, userId);
    }

    // inner class to query event model
    public static class Query extends ParseQuery<Chat> {

        public Query() {
            super(Chat.class);
        }

        // get most recently active chats
        public Query newestFirst() {
            orderByDescending(KEY_UPDATED_AT);
            return this;
        }

        public Query matchesUserId(String userId) {
            whereEqualTo(KEY_MEMBERS, userId);
            return this;
        }
    }
}
