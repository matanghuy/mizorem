package com.example.matanghuy.mizorem;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.matanghuy.mizorem.editevent.attendees.Attendee;
import com.facebook.model.GraphUser;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

import java.util.List;

public class Application extends android.app.Application {
    public static final boolean APPDEBUG = false;

    // Key for saving the search distance preference
    private static final String KEY_SEARCH_RADIUS = "searchRadius";
    private static final String KEY_SHOW_PAST_EVENTS = "showPastEvents";

    private static final String DEFAULT_SEARCH_RADIUS = "5";
    private static final String PARSE_APPLICATION_ID = "40mM5qiIPj9xeIHWq3WLhEpzLhB9oDmp85mwDXUA";
    private static final String PARSE_CLIENT_KEY = "5jhDpJrfD21H9Zed9Q16Qv5e3AM30iM2CPcJjpza";

    private static SharedPreferences preferences;
    private List<GraphUser> selectedUsers;

    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Event.class);
        ParseObject.registerSubclass(Attendee.class);

        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);
        ParseFacebookUtils.initialize(getResources().getString(R.string.facebook_app_id));
        preferences = getSharedPreferences("com.example.matanghuy.mizorem_preferences", Context.MODE_PRIVATE);

    }

    public static int getSearchRadius() {
        return Integer.parseInt(preferences.getString(KEY_SEARCH_RADIUS, DEFAULT_SEARCH_RADIUS));
    }
    public static boolean getShowPastEvent() {
        return preferences.getBoolean(KEY_SHOW_PAST_EVENTS, false);
    }




    public List<GraphUser> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<GraphUser> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

}
