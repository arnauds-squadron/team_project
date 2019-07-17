package com.example.instagram;

import android.app.Application;

import com.example.instagram.models.Comment;
import com.example.instagram.models.Post;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // tell Parse that Post is a custom Parse model created to encapsulate data
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Comment.class);

        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("fbuInsta")
                .clientKey("fbuInsta3141")
                .server("http://fbuinsta.herokuapp.com/parse")
                .build();

        Parse.initialize(configuration);
    }
}
