package com.arnauds_squadron.eatup.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Message")
public class Message extends ParseObject {
    private static final String KEY_SENDER = "sender";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_CHAT_ID = "chatId";
    private static final String KEY_CREATED_AT = "createdAt";

    public ParseUser getSender() {
        return getParseUser(KEY_SENDER);
    }

    public void setSender(ParseUser user) {
        put(KEY_SENDER, user);
    }

    public String getContent() {
        return getString(KEY_CONTENT);
    }

    public void setContent(String content) {
        put(KEY_CONTENT, content);
    }

    public void setChatId(String chat) { put(KEY_CHAT_ID, chat); }

    // inner class to query event model
    public static class Query extends ParseQuery<Message> {
        public Query() {
            super(Message.class);
        }

        public Query newestFirst() {
            orderByDescending(KEY_CREATED_AT);
            return this;
        }

        public Query matchesChat(Chat chat) {
            whereEqualTo(KEY_CHAT_ID, chat.getObjectId());
            return this;
        }
    }
}
