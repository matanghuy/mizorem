package com.example.matanghuy.mizorem.editevent.attendees;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.matanghuy.mizorem.Application;
import com.example.matanghuy.mizorem.BuildConfig;
import com.example.matanghuy.mizorem.Event;
import com.example.matanghuy.mizorem.R;
import com.example.matanghuy.mizorem.Utils;
import com.example.matanghuy.mizorem.editevent.EventFragment;
import com.facebook.model.GraphUser;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class AttendeesFragment extends ListFragment implements EventFragment {
    private static final String TAG = "AttendeesFragment";
    public static final int MENU_ADD_BUTTON = 2;
    public static final int MENU_JOIN_BUTTON = 3;
    public static final int REQUEST_OPEN_FB_PICKER = 6;

    private Event event;
    private ArrayList<Attendee> attendeeList;
    private List<Attendee> deletedAttendees;
    private AttendeesListAdapter listAdapter;
    private AttendeesHandler attendeesHandler;
    private Attendee selected;
    private boolean adminMode = false;

    public static AttendeesFragment newInstance() {
        return new AttendeesFragment();
    }

    public AttendeesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerForContextMenu(getListView());
        getListView().setOnCreateContextMenuListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        attendeeList = new ArrayList<>();
        deletedAttendees = new ArrayList<>();
        listAdapter = new AttendeesListAdapter(getActivity(), attendeeList);
        setListAdapter(listAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "Attached");
        }
        super.onAttach(activity);
        try {
            attendeesHandler = (AttendeesHandler) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EventHandler");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        attendeesHandler = null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(event != null) {
            if(adminMode) {
                menu.add(1, MENU_ADD_BUTTON, Menu.NONE, "Add Friend")
                        .setIcon(android.R.drawable.ic_menu_add)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            } else {
                menu.add(1, MENU_JOIN_BUTTON, Menu.NONE, "Join")
                        .setIcon(android.R.drawable.ic_menu_compass)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_BUTTON:
                startFriendPicker(PickerActivity.FRIEND_PICKER, REQUEST_OPEN_FB_PICKER);
                break;
            case MENU_JOIN_BUTTON:
                joinOrLeaveEvent();

        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * This method assume that the event is already saved on server and the user is not admin.
     * if the user is admin the menu button will not be shown and this method will not be called.
     *
     */
    private void joinOrLeaveEvent() {
        ParseUser me = ParseUser.getCurrentUser();
        String myFbId = me.getString(Utils.FB_ID);
        for (Attendee attendee : attendeeList) {
            if(attendee.getFbId().equals(myFbId)) {
                attendeeList.remove(attendee);
                listAdapter.notifyDataSetChanged();
                attendee.deleteInBackground();
                Toast.makeText(getActivity(), "Removed from event", Toast.LENGTH_LONG).show();
                return;
            }
        }
        //User is not attendee. add him
        Attendee meAttendee = new Attendee(myFbId, me.getString(Utils.FULL_NAME_KEY), false);
        meAttendee.setParent(event);
        attendeeList.add(meAttendee);
        listAdapter.notifyDataSetChanged();
        meAttendee.saveInBackground();
        Toast.makeText(getActivity(), "Joined to event", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onListItemClick(ListView listView, View v, int position, long id) {
        super.onListItemClick(listView, v, position, id);
        Toast.makeText(getActivity(),"Long click on friend to remove", Toast.LENGTH_SHORT).show();
    }



    private void startFriendPicker(Uri data, int requestCode) {
        Intent intent = new Intent();
        intent.setData(data);
        intent.setClass(getActivity(), PickerActivity.class);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode >= 0) {
            List<GraphUser> selectedUsers = ((Application) getActivity().getApplication()).getSelectedUsers();
            addUsersToEvent(selectedUsers);
            selectedUsers.clear();
        }

    }

    private void addUsersToEvent(List<GraphUser> graphUsers) {
        for (GraphUser graphUser : graphUsers) {
            boolean isExist = false;
            for (Attendee existing : attendeeList) {
                if(graphUser.getId().equals(existing.getFbId())) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                Attendee newAttendee = new Attendee(graphUser.getId(), graphUser.getName(), false);
                attendeeList.add(newAttendee);
                listAdapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "User is already in list");
            }
        }
    }

    private void deleteSelectedFriend() {
        listAdapter.remove(selected);
        selected.deleteInBackground();
        selected = null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        selected = listAdapter.getItem(info.position);
        if(selected.isAdmin()) {
           return;
        }
        getActivity().getMenuInflater().inflate(R.menu.menu_attendees, menu);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_remove_friend:
                if(selected != null) {
                    deleteSelectedFriend();
                    return true;
                }
        }
        selected = null;
        return false;
    }

    @Override
    public void onEventLoaded(Event event, List<Attendee> attendeeList, boolean adminMode) {
        this.event = event;
        this.adminMode = adminMode;
        this.attendeeList.clear();
        this.attendeeList.addAll(attendeeList);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSave() {
        if(!deletedAttendees.isEmpty()) {
            Log.d(TAG, "Deleting Attendees: " + deletedAttendees);
            Attendee.deleteAllInBackground(deletedAttendees);
        }
        attendeesHandler.setAttendees(attendeeList);
        return true;
    }
}
