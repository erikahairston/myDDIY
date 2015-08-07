// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;

import com.parse.ParseFacebookUtils;


/**
 *
 */

public class LoginActivity extends FragmentActivity implements LoginFragment.LoginFragmentListener {

    private static final String TAG = "DDIY";

    public static boolean onLoginActivity;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Required for making Facebook login work
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, intent);

    }

    protected int getLayoutResId() {
        return R.layout.login_activity;
    }


    protected Fragment createFragment() {
        return LoginFragment.newInstance();
    }

    public boolean isOnLoginActivity() {
        return onLoginActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onLoginActivity = true;
        setContentView(getLayoutResId());

        //If not already made, show the login form
        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.ddiy_activity_layout, createFragment()).commit();
        }
    }

    public void onLoginFinish() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Interface method implementation
     */
    @Override
    public void onLoginCompleted() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.ddiy_activity_layout, EditProfileFragment.newInstance());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
