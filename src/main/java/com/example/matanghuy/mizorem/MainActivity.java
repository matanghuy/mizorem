package com.example.matanghuy.mizorem;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matanghuy.mizorem.editevent.EditEventActivity;
import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MainActivity";
    private static final int UPDATE_INTERVAL_IN_MILLIS = 30000;
    private static final int REQUEST_CREATE_EVENT= 1;
    private static final int REQUEST_EDIT_EVENT= 2;

    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Marker tempMarker;
    private Map<Marker, Event> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        markers = new HashMap<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createLocationRequest();
        buildGoogleApiClient();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();
        googleApiClient.connect();
        map.setOnMapLongClickListener(this);
        map.setInfoWindowAdapter(new EventDetailsWindowAdapter());
        map.setOnInfoWindowClickListener(this);
        map.setMyLocationEnabled(true);
    }

    private void getCloseEvents(Location currentLocation) {
        ParseGeoPoint currentGeoPoint = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        ParseQuery<Event> query = Event.getQuery();
        int searchRadius = Application.getSearchRadius();
        query.whereWithinKilometers(Event.LOCATION_KEY, currentGeoPoint, searchRadius);
        if(!Application.getShowPastEvent()) {
            query.whereGreaterThan(Event.DATE_KEY, new Date());
        }
        query.setLimit(50);
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException exception) {
                if (exception != null) {
                    Log.e(TAG, exception.getMessage());
                    Toast.makeText(getApplicationContext(), "Cannot retrieve events.\n Check your internet connection", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, events.size() + " events retrieved");
                    map.clear();
                    markers.clear();
                    for (Event event : events) {
                        Log.d(TAG, "Event retrieved:" + event.toString());
                        LatLng latLng = event.getLatLngLocation();
                        markers.put(map.addMarker(new MarkerOptions().position(latLng)), event);
                    }
                }
            }
        });


    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLIS);
        mLocationRequest.setFastestInterval(UPDATE_INTERVAL_IN_MILLIS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        //Do nothing.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_logout:
                ParseUser.logOut();
                Session fbSession = Session.getActiveSession();
                if (fbSession == null) {
                    fbSession = new Session(getApplicationContext());
                    Session.setActiveSession(fbSession);
                }
                fbSession.closeAndClearTokenInformation();
                Intent intent = new Intent(MainActivity.this, DispatchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connection to Google API established");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mLastLocation == null) {
            Log.w(TAG, "Last location unknown");
        } else {
            focusMap(mLastLocation);
            getCloseEvents(mLastLocation);
        }
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        Log.d(TAG, "Starting Location Updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, mLocationRequest, this);
    }

    private void focusMap(Location mLastLocation) {
        Log.d(TAG, "Focus Map");
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 15f));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Failed to connect to Google API", Toast.LENGTH_LONG).show();
        Log.e(TAG, "Connection to Google API Failed\n " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location Updated: " + location.toString());
        mLastLocation = location;

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(tempMarker != null) {
            tempMarker.remove();
        }
        tempMarker = map.addMarker(new MarkerOptions().position(latLng).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        tempMarker.showInfoWindow();
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        String eventId = null;
        int requestCode;
        Intent intent = new Intent(this, EditEventActivity.class);
        if(markers.containsKey(marker)) { //Existing event
            requestCode = REQUEST_EDIT_EVENT;
            eventId = markers.get(marker).getObjectId();
        } else { //new event
            requestCode = REQUEST_CREATE_EVENT;
        }
        intent.putExtra(EditEventActivity.KEY_EVENT_ID, eventId);
        intent.putExtra(EditEventActivity.KEY_LOCATION, marker.getPosition());
        startActivityForResult(intent, requestCode);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CREATE_EVENT && resultCode == EditEventActivity.SUCCESS_RESULT_CODE) {
            if(mLastLocation != null) {
                getCloseEvents(mLastLocation);
            }
        } else if(resultCode == EditEventActivity.RESULT_CODE_ERROR) {
            Toast.makeText(this, "Cannot retrieve event from server", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mLastLocation != null) {
            getCloseEvents(mLastLocation);
        }
        if (googleApiClient.isConnected()) {
            startLocationUpdates();
        }
        if(tempMarker != null) {
            tempMarker.remove();
            tempMarker = null;
        }
    }


    class EventDetailsWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View defaultWindow = getLayoutInflater().inflate(R.layout.event_details_window, null);
        private final View newEventWindow = getLayoutInflater().inflate(R.layout.info_window, null);

        @Override
        public View getInfoWindow(Marker marker) {
            if (markers.containsKey(marker)) {
                Event event = markers.get(marker);
                TextView eventName = (TextView) defaultWindow.findViewById(R.id.tvEventName);
                TextView eventPlace = (TextView) defaultWindow.findViewById(R.id.tvEventPlace);
                TextView dateAndTime = (TextView) defaultWindow.findViewById(R.id.tvEventDateAndTime);
                if(event.getName() != null) {
                    eventName.setText(event.getName().toUpperCase(Locale.US));
                }
                if(event.getPlace() != null) {
                    eventPlace.setText(event.getPlace());
                }
                if(event.getDate() != null) {
                    dateAndTime.setText(Utils.getDateAndTimeAsString(event.getDate()));
                }

                return defaultWindow;
            } else {
                TextView header = (TextView) newEventWindow.findViewById(R.id.tvEventName);
                header.setText(getResources().getString(R.string.click_to_create));
                header.setTextColor(Color.BLUE);

                return newEventWindow;
            }

        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }
}
