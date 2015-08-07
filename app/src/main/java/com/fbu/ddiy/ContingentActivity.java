// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.facebook.appevents.AppEventsLogger;
import com.parse.ParseUser;

/**
 * Created by cduche on 7/10/15.
 */
public class ContingentActivity extends AppCompatActivity {
    /**
     * *******************************************************************************************
     * Constants
     */
    private static final String TAG = "DDIY";

    /**
     * *******************************************************************************************
     * Member Variables
     */
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mMenuOptions;
    private DrawerLayout mDrawerLayout;
    private FrameLayout mFrameLayout;
    private ListView mDrawerList;
    private FloatingActionButton fab;

    /**********************************************************************************************
     * SubClasses
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            //view.setBackgroundColor(getResources().getColor(R.color.hot_color));
            selectItem(position);
        }
    }

    /**********************************************************************************************
     * Methods
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ddiy);
          getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        //setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });

        mMenuOptions = getResources().getStringArray(R.array.menu_options);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mMenuOptions));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "User needs to get signed up");
            ParseApplication.setIsLoginActivityRunning(true);
            Intent intent = new Intent(ContingentActivity.this, LoginActivity.class);
            this.finish();
            startActivity(intent);
        }
        Log.d(TAG, "User already logged in");
        FragmentManager fragmentManager = getSupportFragmentManager();

        Intent intent = getIntent();
        Boolean bo = intent.getBooleanExtra("toNotifFrag", false);

        if (!bo) fragmentManager.beginTransaction().replace(R.id.content_frame,
                createCategoryFragment()).commit();
        else fragmentManager.beginTransaction().replace(R.id.content_frame,
                new NotificationFragment()).commit();
    }

    private void selectItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = createCategoryFragment(); //category fragment
                break;
            case 1:
                fragment = createProfileFragment(); //my profile fragment
                break;
            case 2:
                fragment = createMyCreatedTasksFragment(); //mycreated_incomplete
                break;
            case 3:
                fragment = createTasksToDoFragment(); //to-do tasks
                break;
            case 4:
                fragment = createTasksIveDoneFragment(); //doer_completed
                break;
            case 5:
                fragment = createPastCreatedTasksFragment(); //my_created_completed
                break;
            case 6:
                ParseUser.logOut();
                mDrawerList.setItemChecked(position, false);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;

            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).addToBackStack(null)
                    .commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
          //  setTitle(mMenuOptions[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            finish();
            Intent i = getIntent();
            startActivity(i);
            Log.e("DDIY", "Restarting ContingentAct");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ddiy, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (mDrawerToggle.onOptionsItemSelected(item)) return true;

        switch (item.getItemId()) {
          /**  case R.id.menu_item_new_task:
                addTask();
                return true; */
            case R.id.notifications:
                if (NotificationFragment.getNotificationsIsOpen() == false) {
                    Log.d(TAG, "Opening notifications");
                    viewNotifications();
                } else {
                    Log.d(TAG, "Trying to close Notif");
                    NotificationFragment.setNotificationsIsOpen(false);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame,
                            createCategoryFragment()).commit();
                }
                return true;
            default:
                return false;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }



    /**********************************************************************************************
     * Helper Methods
     */
    private void addTask() {
        Log.d(TAG, "adding Task");
        Intent i = new Intent(this, AddTaskActivity.class);
        startActivity(i);
    }

    protected Fragment createCategoryFragment() { return CategoryFragment.newInstance(); }

    protected Fragment createMyCreatedTasksFragment() {
        return MyCreated_InCompleteTasksFragment.newInstance();
    }

    protected Fragment createNotificationFragment() { return NotificationFragment.newInstance(); }

    protected Fragment createPastCreatedTasksFragment() {
        return MyCreated_CompletedTasksFragment.newInstance();
    }

    protected Fragment createProfileFragment() { return ProfileFragment.newInstance(); }

    protected Fragment createTasksIveDoneFragment() {
        return Doer_CompletedTasksFragment.newInstance();
    }

    protected Fragment createTasksToDoFragment() { return TasksToDoFragment.newInstance(); }

    private void viewNotifications() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, createNotificationFragment())
                .addToBackStack(null)
                .commit();
    }
}
