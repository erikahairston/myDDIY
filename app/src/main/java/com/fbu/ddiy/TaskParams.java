// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

/**
 * Created by cduche on 7/31/15.
 */
public class TaskParams {
    String category;
    String method;
    String school;
    String taskUUID;
    String userUUID;

    TaskParams(String method, String category, String school, String userUUID) {
        this.method = method;
        this.category = category;
        this.school = school;
        this.userUUID = userUUID;
    }
}
