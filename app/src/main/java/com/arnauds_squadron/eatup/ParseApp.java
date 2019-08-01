package com.arnauds_squadron.eatup;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.models.Message;
import com.arnauds_squadron.eatup.models.Rating;
import com.parse.Parse;
import com.parse.ParseObject;

import static com.arnauds_squadron.eatup.utils.Constants.CHANNEL_ID;

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
        createNotificationChannel();
    }

    // create notification channel for push notifications
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Toast Notification Channel";
            String description = "Sends Toast push notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
