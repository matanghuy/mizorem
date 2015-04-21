package com.example.matanghuy.mizorem;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by matanghuy on 3/24/15.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.user_settings);
    }
}
