package com.example.matanghuy.mizorem;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.matanghuy.mizorem.editevent.EditEventActivity;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by matanghuy on 4/5/15.
 */
public class PushReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "PushReceiver";
    public static final String DATA_KEY = "com.parse.Data";

    //those keys are identical to those on the cloud code. any change should be there too.
    public static final String EVENT_ID_KEY = "eventID";

    public PushReceiver() {
        super();
    }


    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Log.d(TAG, "Push Open");
        Intent i = new Intent(context, EditEventActivity.class);
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString(DATA_KEY));
            i.putExtra(EVENT_ID_KEY, json.getString(EVENT_ID_KEY));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

}
