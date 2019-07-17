import android.app.Application;

import com.arnauds_squadron.eatup.models.Event;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // tell Parse that Post is a custom Parse model created to encapsulate data
        ParseObject.registerSubclass(Event.class);

        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("eat_up")
                .clientKey("arnaud")
                .server("http://fbu-eat-up.herokuapp.com/parse")
                .build();

        Parse.initialize(configuration);
    }
}
