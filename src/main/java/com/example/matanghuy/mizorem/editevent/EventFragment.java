package com.example.matanghuy.mizorem.editevent;

import com.example.matanghuy.mizorem.Event;
import com.example.matanghuy.mizorem.editevent.attendees.Attendee;

import java.util.List;

/**
 * Created by matanghuy on 3/31/15.
 */
public interface EventFragment {
    void onEventLoaded(Event event, List<Attendee> attendeeList, boolean adminMode);

    boolean onSave();


}
