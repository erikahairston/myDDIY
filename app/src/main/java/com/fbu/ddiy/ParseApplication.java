// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

/**
 * Created by ashleyxue on 7/15/15.
 */

import android.app.Application;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SaveCallback;


public class ParseApplication extends Application {
    private static boolean loginActivityRunning;

    public static boolean getIsLoginActivityIsRunning() {
        return loginActivityRunning;
    }
    public static void setIsLoginActivityRunning(boolean state) {
        loginActivityRunning = state;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(getApplicationContext());
        // Enable Local Datastore.

        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(
                getApplicationContext(),
                "CruWAo3ttNjm1NnAMG9k68Yq6gH97drjbbAVOh8K",
                "g5rvVJS7SD0cynx08GAtTY1ZytcYc44Rscxhk74i");
        ParseInstallation.getCurrentInstallation().saveInBackground();



        // Add your initialization code here
        ParseFacebookUtils.initialize(getApplicationContext());


        // ParseUser.enableAutomaticUser();
        //   ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        // defaultACL.setPublicReadAccess(true);
        // ParseACL.setDefaultACL(defaultACL, true);
    }
}
