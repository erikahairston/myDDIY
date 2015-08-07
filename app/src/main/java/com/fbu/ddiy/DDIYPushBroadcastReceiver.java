// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.content.Context;
import android.content.Intent;

import com.parse.ParsePushBroadcastReceiver;

public class DDIYPushBroadcastReceiver extends ParsePushBroadcastReceiver {
    @Override
    public void onPushOpen(Context context, Intent intent) {
        Intent i = new Intent(context, ContingentActivity.class);
        i.putExtra("toNotifFrag", true);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
