package com.example.matanghuy.mizorem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseUser;


public class DispatchActivity extends Activity {
    private static final String TAG = "DispatchActivity";

    public DispatchActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ParseUser.getCurrentUser() != null) {
            Log.d(TAG, "Logged in. Dispatching Main Activity...");
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Log.d(TAG, "No user Logged. Dispatching Login Activity...");
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

}
