package com.example.matanghuy.mizorem;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Arrays;


public class LoginActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button facebookButton = (Button) findViewById(R.id.authButton);
        facebookButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.authButton:
                facebookLogin();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //disable back button
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
        Log.d(TAG, "requestCode:" + requestCode + ", resultCode: " + resultCode);
    }
    @Deprecated
    private void addFacebookIdToUser(final ParseUser user) {
        final ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(final GraphUser graphUser, Response response) {
                user.put(Utils.FB_ID, graphUser.getId());
                user.put(Utils.FULL_NAME_KEY, graphUser.getName());
                installation.put(Utils.FB_ID, graphUser.getId());
                installation.saveInBackground();
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e != null) {
                            Log.d(TAG, "Problem saving user data\n" + e.getMessage());
                        } else {
                            Log.d(TAG, "User saved with facebook ID:" + graphUser.getId() + "and Name: " + graphUser.getName());
                        }

                    }
                });
            }
        }).executeAsync();
    }
    private void finalizeUserCreation(final ParseUser parseUser, final ParseInstallation installation) {
        new AsyncTask<ParseObject, Object, Object>() {

            @Override
            protected Object doInBackground(ParseObject... params) {
                ParseUser parseUser = (ParseUser)params[0];
                ParseInstallation installation = (ParseInstallation)params[1];
                Request request = new Request(Session.getActiveSession(), "me");
                Response response = request.executeAndWait();
                GraphObject graphObject = response.getGraphObject();
                String fbUserId = (String) graphObject.getProperty("id");
                String fbUserName = (String) graphObject.getProperty("name");
                Log.d(TAG, "facebook id = " + fbUserId);
                parseUser.put(Utils.FB_ID, fbUserId);
                parseUser.put(Utils.FULL_NAME_KEY, fbUserName);
                installation.put(Utils.FB_ID, fbUserId);
                try {
                    parseUser.save();
                    installation.save();
                } catch (ParseException e) {
                    Log.e(TAG, "Failed to save User/installation\n" + Arrays.toString(e.getStackTrace()));

                }
                return null;
            }
        }.execute(parseUser, installation);

    }

    private void facebookLogin() {
        ParseFacebookUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user == null) {
                    Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d(TAG, "User signed up and logged in through Facebook!");
                   // addFacebookIdToUser(user);
                    finalizeUserCreation(user, ParseInstallation.getCurrentInstallation());
                    Intent intent = new Intent(LoginActivity.this, DispatchActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "User logged in through Facebook!");
                    Intent intent = new Intent(LoginActivity.this, DispatchActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
    }

}
