package com.arnauds_squadron.eatup.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

@ParseClassName("Chat")
public class Chat extends ParseObject {
    private static final String KEY_NAME = "name";
    private static final String KEY_MEMBERS = "members";
    private static final String KEY_UPDATED_AT = "updatedAt";

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public void justUpdated() {
        put(KEY_UPDATED_AT, new Date());
    }

    public void addMember(ParseUser user) {
        add(KEY_MEMBERS, user);
    }

    public List<ParseUser> getMembers() {
        return getList(KEY_MEMBERS);
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

        public Query matchesUser(ParseUser user) {
            whereEqualTo(KEY_MEMBERS, user);
            return this;
        }
    }
}
