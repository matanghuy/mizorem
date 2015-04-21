package com.example.matanghuy.mizorem;

import android.util.Log;

import com.example.matanghuy.mizorem.editevent.attendees.Attendee;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

/**
 * Created by matanghuy on 2/3/15.
 */

@ParseClassName("Event")
public class Event extends ParseObject {
    private static final String TAG = "Event";
    public static final String NAME_KEY = "name";
    public static final String PLACE_KEY = "place";
    public static final String ADMIN_KEY = "admin";
    public static final String LOCATION_KEY = "location";
    public static final String DATE_KEY = "date";

    public Event() {
    }

    public Event(ParseUser admin) {
        setAdmin(admin);
        setDate(new Date());
        Log.d(TAG, "Event Created => admin: " + getAdmin() + " date: " + getDate());
    }

    public Event(ParseUser admin, LatLng location) {
        this(admin);
        setLocation(location);
    }

    public String getName() {
        return getString(NAME_KEY);
    }

    public void setName(String name) {
        put(NAME_KEY, name);
    }

    public String getPlace() {
        return getString(PLACE_KEY);
    }

    public void setPlace(String place) {
        put(PLACE_KEY, place);
    }

    public ParseUser getAdmin() {
        return getParseUser(ADMIN_KEY);
    }

    public void setAdmin(ParseUser admin) {
        put(ADMIN_KEY, admin);
    }

    public void setAttendeeList(final List<Attendee> attendeeList) {
        for (Attendee attendee : attendeeList) {
            attendee.setParent(this);
        }
        try {
            Attendee.saveAll(attendeeList);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(LOCATION_KEY);
    }

    public LatLng getLatLngLocation() {
        ParseGeoPoint geoPoint = getLocation();
        return new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    public void setLocation(LatLng latLng) {
        put(LOCATION_KEY, new ParseGeoPoint(latLng.latitude, latLng.longitude));
    }

    public Date getDate() {
        return getDate(DATE_KEY);
    }

    public void setDate(Date date) {
        put(DATE_KEY, date);
    }


    public static ParseQuery<Event> getQuery() {
        return ParseQuery.getQuery(Event.class);
    }

    @Override
    public String toString() {
        return  "name: " + (getName() != null ? getName() : "No Name") +
                ", place: " + (getPlace() != null ? getPlace() : "No Place") +
                ", admin: " + (getAdmin() != null ? getAdmin() : "No Admin") +
                ", location: " + (getLocation() != null ? getLocation() : "No Location") +
                ", date: " + (getDate() == null ? "No Date" : Utils.getDateAsString(getDate()));
    }

}
