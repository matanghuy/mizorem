package com.example.matanghuy.mizorem.editevent.details;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.matanghuy.mizorem.Event;
import com.example.matanghuy.mizorem.R;
import com.example.matanghuy.mizorem.Utils;
import com.example.matanghuy.mizorem.editevent.EventFragment;
import com.example.matanghuy.mizorem.editevent.attendees.Attendee;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class EventDetailsFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, EventFragment{
    private static final String TAG = "EditEventFragment";
    private Event event;
    private Button btnChangeDate;
    private Button btnChangeTime;
    private TextView tvSelectedDate;
    private TextView tvSelectedTime;
    private TextView etEventName;
    private TextView etEventPlace;
    private Date selectedDate;

    public static EventDetailsFragment newInstance() {
        return new EventDetailsFragment();
    }


    public EventDetailsFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Created");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);
        etEventName = (TextView)view.findViewById(R.id.etEventName);
        etEventPlace = (TextView)view.findViewById(R.id.etEventPlace);
        btnChangeDate = (Button)view.findViewById(R.id.btnChangeDate);
        btnChangeTime = (Button)view.findViewById(R.id.btnChangeTime);
        tvSelectedDate = (TextView)view.findViewById(R.id.tvSelectedDate);
        tvSelectedTime = (TextView)view.findViewById(R.id.tvSelectedTime);
        selectedDate = new Date();
        tvSelectedDate.setText(Utils.getDateAsString(selectedDate));
        tvSelectedTime.setText(Utils.getTimeAsString(selectedDate));

        btnChangeDate.setOnClickListener(this);
        btnChangeTime.setOnClickListener(this);
        toggleFields(false);
        return view;
    }

    private void toggleFields(boolean isEnabled) {
        etEventName.setEnabled(isEnabled);
        etEventPlace.setEnabled(isEnabled);
        btnChangeDate.setEnabled(isEnabled);
        btnChangeTime.setEnabled(isEnabled);
        if(!isEnabled) {
            btnChangeDate.setVisibility(View.INVISIBLE);
            btnChangeTime.setVisibility(View.INVISIBLE);
        } else {
            btnChangeDate.setVisibility(View.VISIBLE);
            btnChangeTime.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "fields 'enabled' status is: " + isEnabled);
    }


    private void populateFields() {
        if(event.getName() != null) {
            etEventName.setText(event.getName());
        }
        if(event.getPlace() != null) {
            etEventPlace.setText(event.getPlace());
        }
        if(event.getDate() != null) {
            selectedDate = event.getDate();
            tvSelectedDate.setText(Utils.getDateAsString(selectedDate));
            tvSelectedTime.setText(Utils.getTimeAsString(selectedDate));
        }

    }


    @Override
    public void onClick(View view) {
        Calendar c = Calendar.getInstance();
        c.setTime(selectedDate);
        switch (view.getId()) {
            case R.id.btnChangeDate:
                int startYear = c.get(Calendar.YEAR);
                int startMonth = c.get(Calendar.MONTH);
                int startDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dpDialog = new DatePickerDialog(getActivity(), this, startYear, startMonth, startDay);
                dpDialog.show();
                break;
            case R.id.btnChangeTime:
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog tpDialog = new TimePickerDialog(getActivity(), this, hour, minute, true);
                tpDialog.show();
                break;


        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        selectedDate = Utils.updateDate(selectedDate,dayOfMonth,monthOfYear,year);
        tvSelectedDate.setText(Utils.getDateAsString(selectedDate));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        selectedDate = Utils.updateDate(selectedDate, hourOfDay, minute);
        tvSelectedTime.setText(Utils.getTimeAsString(selectedDate));
    }

    @Override
    public void onEventLoaded(Event event, List<Attendee> attendeeList, boolean adminMode) {
        this.event = event;
        populateFields();
        toggleFields(adminMode);
    }

    @Override
    public boolean onSave() {
        if(event == null) {
            Log.e(TAG, "Something went wrong...");
            throw new IllegalStateException("Event must not be null at this point");
        }
        if(!fieldsValid()) {
            return false;
        }
        event.setDate(selectedDate);
        event.setName(etEventName.getText().toString());
        event.setPlace(etEventPlace.getText().toString());
        return true;
    }

    private boolean fieldsValid() {
        boolean validName = true;
        boolean validPlace = true;
        if (etEventName.getText().toString().isEmpty()) {
            etEventName.setError(getResources().getString(R.string.event_name_missing));
            validName = false;
        }
        if (etEventPlace.getText().toString().isEmpty()) {
            etEventPlace.setError(getResources().getString(R.string.event_place_missing));
            validPlace = false;
        }
        return validName && validPlace;
    }
}
