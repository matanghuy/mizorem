package com.example.matanghuy.mizorem.editevent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.example.matanghuy.mizorem.Event;
import com.example.matanghuy.mizorem.R;
import com.example.matanghuy.mizorem.Utils;
import com.example.matanghuy.mizorem.editevent.attendees.Attendee;
import com.example.matanghuy.mizorem.editevent.attendees.AttendeesFragment;
import com.example.matanghuy.mizorem.editevent.attendees.AttendeesHandler;
import com.example.matanghuy.mizorem.editevent.details.EventDetailsFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class EditEventActivity extends ActionBarActivity implements ActionBar.TabListener, AttendeesHandler {
    private static final String TAG = "CreateEventActivity";

    public static final String KEY_EVENT_ID = "eventID";
    public static final String KEY_LOCATION = "location";
    public static final int SUCCESS_RESULT_CODE = 1;
    public static final int RESULT_CODE_ERROR = 234;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    private Event event;
    private List<Attendee> attendeeList;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_create_event);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        retrieveEventFromServer(getIntent().getStringExtra(KEY_EVENT_ID), (LatLng)getIntent().getParcelableExtra(KEY_LOCATION));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_event, menu);
        boolean editable = amIAdmin();
        MenuItem saveButton = menu.findItem(R.id.action_save_event);
        saveButton.setEnabled(editable);
        saveButton.setVisible(editable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_event:
                saveEvent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    @Override
    public void setAttendees(List<Attendee> attendeeList) {
        this.attendeeList = attendeeList;
    }

    private void retrieveEventFromServer(final String eventId, final LatLng eventLocation) {
        new AsyncTask<String, Object, Object>() {
            @Override
            protected void onPreExecute() {
                setSupportProgressBarIndeterminate(true);
                super.onPreExecute();
            }

            @Override
            protected Object doInBackground(String[] params) {
                String eventId = params[0];
                if(eventId == null) { //new Event
                    ParseUser cUser = ParseUser.getCurrentUser();
                    event = new Event(cUser, eventLocation);
                    attendeeList = new ArrayList<>();
                    Attendee adminAttendee = new Attendee(cUser.getString(Utils.FB_ID),cUser.getString(Utils.FULL_NAME_KEY), true);
                    attendeeList.add(adminAttendee);
                } else { // Existing event
                    try {
                        event = Event.getQuery().get(eventId);
                        ParseQuery<Attendee> query = Attendee.getQueryWithParentEvent(event);
                        attendeeList = query.find();
                    } catch (ParseException e) {
                        Log.e(TAG, "Cannot retrieve event from server. " + e.getMessage());
                        setResult(RESULT_CODE_ERROR);
                        finish();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(event != null && attendeeList != null) {
                    eventLoaded();
                }
                setSupportProgressBarIndeterminate(false);
            }
        }.execute(eventId);
    }

    public void eventLoaded() {
        Log.d(TAG, "Event Loaded => " + event.toString());
        boolean adminMode = amIAdmin();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            ((EventFragment) fragment).onEventLoaded(event,attendeeList, adminMode);
        }
        if(adminMode) {
            invalidateOptionsMenu();
        }

    }

    private boolean amIAdmin() {
        if(event != null) {
            return event.getAdmin().getObjectId().equals(ParseUser.getCurrentUser().getObjectId());
        } else {
            return false;
        }
    }

    public void saveEvent() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if(!((EventFragment) fragment).onSave()){
                return;
            }
        }
        Log.d(TAG, "Saving event...\n" + event.toString());
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error occurred while saving event. " + e.getMessage());
                } else {
                    event.setAttendeeList(attendeeList);
                }
            }
        });
        setResult(SUCCESS_RESULT_CODE);
        finish();
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return EventDetailsFragment.newInstance();
                case 1:
                    return AttendeesFragment.newInstance();

            }
            return EventDetailsFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.event_details).toUpperCase(l);
                case 1:
                    return getString(R.string.attendees).toUpperCase(l);
            }
            return null;
        }
    }


}
