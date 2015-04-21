package com.example.matanghuy.mizorem.editevent.attendees;

import com.example.matanghuy.mizorem.Event;
import com.example.matanghuy.mizorem.Utils;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by matanghuy on 3/15/15.
 */

@ParseClassName("Attendee")
public class Attendee extends ParseObject {
    public static final String NAME_KEY = "name";
    public static final String IS_ADMIN_KEY = "isAdmin";
    public static final String PARENT_KEY = "parent";

    public Attendee() {
    }

    public Attendee(String fbId, String name, boolean isAdmin) {
        setFbId(fbId);
        setName(name);
        setIsAdmin(isAdmin);
    }

    public boolean isAdmin() {
        return getBoolean(IS_ADMIN_KEY);
    }

    public void setIsAdmin(boolean isAdmin) {
        put(IS_ADMIN_KEY, isAdmin);
    }

    public void setParent(Event event) {
        put(PARENT_KEY, event);
    }

    public Event getParent() {
        return (Event) getParseObject(PARENT_KEY);
    }

    public void setFbId(String fbId) {
        put(Utils.FB_ID, fbId);
    }

    public String getFbId() {
        return getString(Utils.FB_ID);
    }

    public void setName(String name) {
       put(NAME_KEY, name);
    }

    public String getName() {
        return getString(NAME_KEY);
    }

    public static ParseQuery<Attendee> getQuery() {
        return ParseQuery.getQuery(Attendee.class);
    }
    public static ParseQuery<Attendee> getQueryWithParentEvent(Event event) {
        ParseQuery<Attendee> query = ParseQuery.getQuery(Attendee.class);
        query.whereEqualTo(PARENT_KEY, event);
        return query;
    }

    @Override
    public boolean equals(Object o) {
        try{
            Attendee other = (Attendee)o;
            return other.getFbId().equals(getFbId());
        } catch(ClassCastException e) {
            return false;
        }
    }
}
