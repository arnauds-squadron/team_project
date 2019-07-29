package com.arnauds_squadron.eatup;

import android.app.Application;

import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.Message;
import com.arnauds_squadron.eatup.models.Rating;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Event.class);
        ParseObject.registerSubclass(Chat.class);
        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(Rating.class);

        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("eat_up")
                .clientKey("arnaud")
                .server("http://fbu-eat-up.herokuapp.com/parse")
                .build();

        Parse.initialize(configuration);
    }
}
