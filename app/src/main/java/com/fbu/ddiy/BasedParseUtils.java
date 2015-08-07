// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by cduche on 7/15/15.
 * This class just holds all the constants/basic methods we will even need in our application
 */
public class BasedParseUtils {
    /**
     * Application Constants
     */
    public static final String DEFAULT_MESSAGE = "Feed me pls";
    public static final String TAG = "DDIY";

    public static final String PICK_A_TIME_FRAME = "Pick A Time Frame";
    public static final String TIME_AFTERNNON = "Afternoon";
    public static final String TIME_ANYTIME = "Anytime";
    public static final String TIME_EARLY_AFTERNOON = "Early Afternoon";
    public static final String TIME_EARLY_MORNING = "Early Morning";
    public static final String TIME_EVENING = "Evening";
    public static final String TIME_MORNING = "Morning";
    public static final String TIME_NIGHT = "Night";
    private static String taskTitle = "title";

    public static String[] TIME_ARRAY = {PICK_A_TIME_FRAME, TIME_ANYTIME, TIME_EARLY_MORNING,
            TIME_MORNING,
            TIME_EARLY_AFTERNOON, TIME_AFTERNNON, TIME_EVENING,
            TIME_NIGHT};

    public static List<ParseObject> answer;

    /**
     * Async Constants
     */
    public static final String TASKS_BY_SCHOOL_CAT = "TasksBySchoolCat";

    /**
     * Bundle Constants
     */
    public static final String CAT_NAME = "CategoryName";
    public static final String TASK_ID = "TaskID";
    public static final String TO_ACCEPT = "ToAccept";
    public static final String USER_ID = "UserID";

    /**
     * Parse Acceptance Lists
     */
    private static ArrayList<ParseUser> allTheseAccepters = new ArrayList<ParseUser>();

    /**
     * Parse Category Lists
     */
    private static ArrayList<String> categoryList = new ArrayList<>();
    private static List<ParseObject> categoryParseObjects;

    /**
     * Parse Review Lists
     */
    private static ArrayList<Integer> listOfRatings = new ArrayList<Integer>();

    /**
     * Parse Tasks Lists
     */
    private static List<ParseObject> draftsByUser;
    private static List<ParseObject> realDraftsByUser;
    private static List<ParseObject> relevantCompletedTasks;
    private static List<ParseObject> relevantPastTasks;
    private static List<ParseObject> tasksByCreator;
    private static List<ParseObject> tasksByDoer;
    private static List<ParseObject> tasksBySchoolCategory;
    private static List<ParseObject> tasksWithAcceptancesbyUser;

    /**
     * Parse Acceptances Constants
     */
    public static final String ACCEPTANCE_ACCEPTER = "Accepter";
    public static final String ACCEPTANCE_OBJECT_ID = "objectId";
    public static final String ACCEPTANCE_TABLE = "Acceptances";
    public static final String ACCEPTANCE_TASK = "Task";

    /**
     * Parse Category Constants
     */
    public static final String CATEGORY_CREATED_AT = "createdAt";
    public static final String CATEGORY_NAME = "Name";
    public static final String CATEGORY_OBJECT_ID = "objectId";
    public static final String CATEGORY_ORDER = "Order";
    public static final String CATEGORY_TABLE = "Categories";
    public static final String CATEGORY_UPDATED_AT = "updatedAt";

    /**
     * Parse Review Constants
     */
    public static final String REVIEW_CREATED_AT = "createdAt";
    public static final String REVIEW_OBJECT_ID = "objectId";
    public static final String REVIEW_RATING = "Rating";
    public static final String REVIEW_TABLE = "Reviews";
    public static final String REVIEW_TASK = "Task";
    public static final String REVIEW_UPDATED_AT = "updatedAt";

    /**
     * Parse Task Constants
     */
    public static final String TASK_CATEGORY = "Category";
    public static final String TASK_CREATED_AT = "createdAt";
    public static final String TASK_CREATOR = "Task_Creator";
    public static final String TASK_DATE = "Task_Date";
    public static final String TASK_DETAILS = "Task_Details";
    public static final String TASK_DOER = "Task_Doer";
    public static final String TASK_OBJECT_ID = "objectId";
    public static final String TASK_NUMBER_OF_ACCEPTANCES = "Number_Of_Acceptances";
    public static final String TASK_PRICE = "Task_Price";
    public static final String TASK_STATUS = "Status";
    public static final String TASK_STATUS_ACCEPTED = "Accepted";
    public static final String TASK_STATUS_COMPLETED = "Completed";
    public static final String TASK_STATUS_CREATED = "Created";
    public static final String TASK_STATUS_DELETED = "Deleted";
    public static final String TASK_STATUS_DRAFT = "Draft";
    public static final String TASK_STATUS_EXPIRED = "Expired";
    public static final String TASK_STATUS_REVIEWED = "Reviewed";
    public static final String TASK_TABLE = "Tasks";
    public static final String TASK_TIME = "Task_Time";
    public static final String TASK_TITLE = "Task_Title";
    public static final String TASK_UPDATED_AT = "updatedAt";
    public static final String TASK_PHOTO = "Task_Photo";
    public static final String TASK_CREATOR_PHOTO = "Task_Creator_Pic_Id";

    /**
     * ParseUser Constants
     */
    public static final String USER_ABOUT_ME = "About_Me";
    public static final String USER_CREATED_AT = "createdAt";
    public static final String USER_EMAIL = "email";
    public static final String USER_FB_AUTH = "authData";
    public static final String USER_GENDER = "gender";
    public static final String USER_USERNAME = "username";
    public static final String USER_NAME = "Name";
    public static final String USER_OBJECT_ID = "objectId";
    public static final String USER_PIC_ID = "id";
    public static final String USER_PICTURE_URL = "pictureURL";
    public static final String USER_PHONE_NUMBER = "Phone_Number";
    public static final String USER_SCHOOL = "School";
    public static final String USER_UPDATED_AT = "updatedAt";
    public static final String USER_CONNECTED = "connectedToFB";



    /**
     * Parse Tasks Variables
     */
    private static ParseObject task;
    private static ParseUser taskCreatorByTask;
    private static ParseUser taskDoerByTask;
    private static ParseUser userGotten;

    private static ParseUser userWithTasks;



    /**
     * Parse Acceptance Static Methods
     */
    public static List<ParseUser> getAcceptorsByTaskID(String taskUUID) {
        allTheseAccepters.clear();

        ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery(TASK_TABLE);
        innerQuery.whereMatches(TASK_OBJECT_ID, taskUUID);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(ACCEPTANCE_TABLE);
        query.whereMatchesQuery(ACCEPTANCE_TASK, innerQuery);
        query.include(ACCEPTANCE_ACCEPTER);

        try {
            List<ParseObject> list = query.find();
            for (ParseObject po : list) allTheseAccepters.add(po.getParseUser(ACCEPTANCE_ACCEPTER));
        } catch (ParseException e) {} //TODO: this

        return allTheseAccepters;
    }

    public static void putAccepter(ParseUser currentUser, String taskUUID) {
        ParseObject po = getTaskById(taskUUID);

        ParseObject putAcceptance = new ParseObject(BasedParseUtils.ACCEPTANCE_TABLE);
        putAcceptance.put(BasedParseUtils.ACCEPTANCE_ACCEPTER, currentUser);
        putAcceptance.put(BasedParseUtils.ACCEPTANCE_TASK, po);
        putAcceptance.saveInBackground();

        incrementAccepterByTask(taskUUID);
    }

    public static String getTaskTitle(String taskUUID) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TASK_TABLE);

        ParseObject po = null;
        try {
            po = query.get(taskUUID);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        taskTitle = po.getString(TASK_TITLE);
        return taskTitle;
    }


    /**
     * Parse Category Static Methods
     */
    public static List<String> getAllCategories() {
        categoryList.clear();

        ParseQuery<ParseObject> categoryQuery = ParseQuery.getQuery(CATEGORY_TABLE);
        categoryQuery.orderByAscending(CATEGORY_ORDER);
        categoryList = new ArrayList<>();
        Log.d(TAG, "gets here");
        try {
            categoryParseObjects = categoryQuery.find();
            for (ParseObject po : categoryParseObjects)
                categoryList.add(po.getString(BasedParseUtils.CATEGORY_NAME));
        } catch (ParseException e) {
            Log.d(TAG, "This shit does not exist in Parse");
        }

        return categoryList;
    }


    /**
     * Parse Review Static Methods
     */
    public static float getAverageRating(String userUUID) {
        ParseQuery<ParseUser> firstLayerInnerQuery = ParseUser.getQuery();
        firstLayerInnerQuery.whereMatches(USER_OBJECT_ID, userUUID);

        ParseQuery<ParseObject> secondLayerInnerQuery = ParseQuery.getQuery(TASK_TABLE);
        secondLayerInnerQuery.whereMatchesQuery(TASK_DOER, firstLayerInnerQuery);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(REVIEW_TABLE);
        query.whereMatchesQuery(REVIEW_TASK, secondLayerInnerQuery);

        try {
            List<ParseObject> list = query.find();
            if (list.size() == 0) return 5;
            for (ParseObject po : list) listOfRatings.add(po.getInt(REVIEW_RATING));
        } catch (ParseException e) {
        }//TODO: this

        int rating = 0;
        for (int i : listOfRatings) rating += i;

        float returnThis = (float) rating / listOfRatings.size();

        return returnThis;
    }

    public static void putReview(String taskUUID, float rating) {
        ParseObject po = getTaskById(taskUUID);

        po.put(TASK_STATUS, TASK_STATUS_REVIEWED);

        ParseObject newPO = new ParseObject(BasedParseUtils.REVIEW_TABLE);
        newPO.put(BasedParseUtils.REVIEW_TASK, po);
        newPO.put(BasedParseUtils.REVIEW_RATING, rating);

        po.saveInBackground();
        newPO.saveInBackground();
    }


    /**
     * Parse Tasks Static Methods
     */
    public static void changeTaskToAccepted(String taskUUID, String otherUserUUID) {
        ParseObject task = getTaskById(taskUUID);

        task.put(TASK_STATUS, TASK_STATUS_ACCEPTED);
        task.put(TASK_DOER, getUserById(otherUserUUID));

        task.saveInBackground();
    }

    public static void changeTaskToCompleted(String taskUUID) {
        ParseObject task = getTaskById(taskUUID);
        task.put(TASK_STATUS, TASK_STATUS_COMPLETED);
        task.saveInBackground();
    }

    public static boolean checkIfTaskAcccepted(String taskUUID) {
        List<ParseUser> list = getAcceptorsByTaskID(taskUUID);
        boolean taskAccepted = false;

        for (ParseUser pu : list) if (ParseUser.getCurrentUser().equals(pu)) taskAccepted = true;
        return taskAccepted;
    }

    public static void expireTask(ParseObject task) {
        task.put(TASK_STATUS, TASK_STATUS_EXPIRED);
        ParsePush.unsubscribeInBackground
                (AddTaskFragment.getTaskChannel(task.getObjectId()));
        task.saveInBackground();
    }

    public static void deleteTask(String taskUUID) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TASK_TABLE);

        try {
            ParseObject task = query.get(taskUUID);
            task.put(TASK_STATUS, TASK_STATUS_DELETED);
            ParsePush.unsubscribeInBackground(AddTaskFragment.getTaskChannel(taskUUID));
            task.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.d(TAG, "object deleted");
                }
            });
        } catch (ParseException e) {
        } //TODO: this
    }

    public static List<ParseObject> getCompletedTasks(String userUUID) {
        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();

        ParseQuery<ParseUser> innerQuery = ParseUser.getQuery();
        innerQuery.whereMatches(USER_OBJECT_ID, userUUID);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TASK_TABLE);
        query.whereMatchesQuery(TASK_DOER, innerQuery);
        query.whereMatches(TASK_STATUS, TASK_STATUS_COMPLETED);

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery(TASK_TABLE);
        query2.whereMatchesQuery(TASK_DOER, innerQuery);
        query2.whereMatches(TASK_STATUS, TASK_STATUS_REVIEWED);

        queries.add(query); queries.add(query2);

        ParseQuery<ParseObject> superQuery = ParseQuery.or(queries);

        try {
            relevantCompletedTasks = superQuery.find();
            for (ParseObject po : relevantCompletedTasks) po.getParseUser(TASK_CREATOR).fetch();
        }
        catch (ParseException e) {} //TODO: this


        return relevantCompletedTasks;
    }

    public static List<ParseObject> getDraftsByUser(String userUUID) {
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereMatches(USER_OBJECT_ID, userUUID);

        ParseQuery<ParseObject> taskQuery = ParseQuery.getQuery(TASK_TABLE);
        taskQuery.whereMatches(TASK_STATUS, TASK_STATUS_DRAFT);
        taskQuery.whereMatchesQuery(TASK_CREATOR, userQuery);

        try { draftsByUser = taskQuery.find(); }
        catch (ParseException e) {} //TODO: this

        //delete this if it breaks draft to lasy to test
        if (draftsByUser.size() > 1) {
            realDraftsByUser = new ArrayList<>();
            for (ParseObject po : draftsByUser) realDraftsByUser.add(po);
            for (int i = 1; i < realDraftsByUser.size(); i++) {
                try { realDraftsByUser.get(i).delete(); }
                catch (ParseException e) {} //TODO: this
            }

            return getDraftsByUser(userUUID);
        }

        return draftsByUser;
    }

    public static List<ParseObject> getNotifiableTasksByUserID(String userUUID) {
        ParseQuery innerQuery = ParseUser.getQuery();
        innerQuery.whereContains(USER_OBJECT_ID, userUUID);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TASK_TABLE);
        query.whereMatches(TASK_STATUS, TASK_STATUS_CREATED);
        query.whereGreaterThan(TASK_NUMBER_OF_ACCEPTANCES, 0);
        query.whereMatchesQuery(TASK_CREATOR, ParseUser.getQuery()
                .whereContains(USER_OBJECT_ID, userUUID));

        try { tasksWithAcceptancesbyUser = query.find(); }
        catch (ParseException e) { e.printStackTrace(); } //TODO: this

        return tasksWithAcceptancesbyUser;
    }

    public static List<ParseObject> getPastTasks(String userUUID) {
        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();

        ParseQuery innerQuery = ParseUser.getQuery();
        innerQuery.whereMatches(USER_OBJECT_ID, userUUID);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TASK_TABLE);
        query.whereMatches(TASK_STATUS, TASK_STATUS_COMPLETED);
        query.whereMatchesQuery(TASK_CREATOR, innerQuery);

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery(TASK_TABLE);
        query2.whereMatches(TASK_STATUS, TASK_STATUS_EXPIRED);
        query2.whereMatchesQuery(TASK_CREATOR, innerQuery);

        ParseQuery<ParseObject> query3 = ParseQuery.getQuery(TASK_TABLE);
        query3.whereMatches(TASK_STATUS, TASK_STATUS_REVIEWED);
        query3.whereMatchesQuery(TASK_CREATOR, innerQuery);

        queries.add(query); queries.add(query2); queries.add(query3);
        ParseQuery<ParseObject> superQuery = ParseQuery.or(queries);

        try {
            relevantPastTasks = superQuery.find();
            for (ParseObject po : relevantPastTasks) po.getParseUser(TASK_CREATOR).fetch();
        } catch (ParseException e) {} //TODO: this

        return relevantPastTasks;
    }

    public static ParseObject getTaskById(String taskID) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TASK_TABLE);
        query.include(TASK_CREATOR);

        try { task = query.get(taskID); }
        catch (ParseException e) {} //TODO: this

        return task;
    }

    public static List<ParseObject> getTasksByCreator(String userUUID) {
        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();

        ParseQuery<ParseUser> innerQuery = ParseUser.getQuery();
        innerQuery.whereMatches(USER_OBJECT_ID, userUUID);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TASK_TABLE);
        query.whereMatches(TASK_STATUS, TASK_STATUS_CREATED);
        query.whereMatchesQuery(TASK_CREATOR, innerQuery);

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery(TASK_TABLE);
        query2.whereMatches(TASK_STATUS, TASK_STATUS_ACCEPTED);
        query2.whereMatchesQuery(TASK_CREATOR, innerQuery);

        queries.add(query); queries.add(query2);

        ParseQuery<ParseObject> superQuery = ParseQuery.or(queries);

        try {
            tasksByCreator = query.find();
        } catch (ParseException e) {
        }

        return tasksByCreator;
    }


    public static List<ParseObject> getTasksByDoer(String userUUID) {
        ParseQuery<ParseUser> innerQuery = ParseUser.getQuery();
        innerQuery.whereMatches(USER_OBJECT_ID, userUUID);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TASK_TABLE);
        query.whereMatches(TASK_STATUS, TASK_STATUS_ACCEPTED);
        query.whereMatchesQuery(TASK_DOER, innerQuery);


        try {
            tasksByDoer = query.find();
            for (ParseObject po : tasksByDoer) po.getParseUser(TASK_CREATOR).fetch();
        } catch (ParseException e) {
        }

        return tasksByDoer;
    }


    public static List<ParseObject> getTasksBySchoolAndCat
            (String userUUID, String school, String category) {

        ParseQuery<ParseUser> innerQuery = ParseUser.getQuery();
        innerQuery.whereNotEqualTo(USER_OBJECT_ID, userUUID);
        innerQuery.whereMatches(USER_SCHOOL, school);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TASK_TABLE);
        query.whereMatches(TASK_CATEGORY, category);
        query.whereMatches(TASK_STATUS, TASK_STATUS_CREATED);
        query.whereMatchesQuery(TASK_CREATOR, innerQuery);

        try { tasksBySchoolCategory = query.find(); }
        catch (ParseException e) {} //TODO: this later

        for (int i = 0; i < tasksBySchoolCategory.size(); i++) {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            Calendar date2 = Calendar.getInstance();
            date2.add(Calendar.DATE, -1);

           // SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            //idk why this is happening
            Date date1 = new Date();

            try { date1 = format.parse(tasksBySchoolCategory.get(i).getString(TASK_DATE)); }
            catch (java.text.ParseException e) {} //TODO: this later

            if (date1.compareTo(date2.getTime()) > 0) continue;

            expireTask(tasksBySchoolCategory.get(i));
            tasksBySchoolCategory.remove(i);
            i = 0;
        }

        return tasksBySchoolCategory;
    }

    /**
     * Parse User Static Methods
     */
    public static ParseUser getCreator(String taskUUID) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TASK_TABLE);
        query.include(USER_PIC_ID);
        query.include(USER_FB_AUTH);

        ParseQuery<ParseUser> query2 = ParseUser.getQuery();
        try {
            ParseObject po = query.get(taskUUID);
            taskCreatorByTask = po.getParseUser(TASK_CREATOR);
            query2.whereMatches(BasedParseUtils.USER_OBJECT_ID, taskCreatorByTask.getObjectId());
            taskCreatorByTask = query2.find().remove(0);

        } catch (ParseException e) {
            Log.d(BasedParseUtils.TAG, "Could not find Creator for task");
        }

        return taskCreatorByTask;
    }

    public static ParseUser getDoer(String taskUUID) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TASK_TABLE);
        query.include(USER_NAME);

        ParseQuery<ParseUser> query2 = ParseUser.getQuery();

        try {
            ParseObject po = query.get(taskUUID);
            taskDoerByTask = po.getParseUser(TASK_DOER);
            query2.whereMatches(BasedParseUtils.USER_OBJECT_ID, taskDoerByTask.getObjectId());
            taskDoerByTask = query2.find().remove(0);
        } catch (ParseException e) {} //TODO: this

        return taskDoerByTask;
    }

    //TODO: might need to make another query here to get the puser data, test it.
    public static ParseUser getUserById(String userUUID) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        try {
            userGotten = query.get(userUUID);
        } catch (ParseException e) {
            e.printStackTrace();
        } //TODO: this

        return userGotten;
    }

    public static void incrementAccepterByTask(String taskUUID) {
        ParseObject task = getTaskById(taskUUID);
        task.put(TASK_NUMBER_OF_ACCEPTANCES, task.getInt(TASK_NUMBER_OF_ACCEPTANCES) + 1);
        task.saveInBackground();
    }

    public static void saveUserDataField(ParseUser user, String field, String data) {
        user.put(field, data);
        user.saveInBackground();
    }

    public static Bitmap getPhotoFile(String taskUUID) {
        Bitmap image = null;
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TASK_TABLE);

        try {
            ParseObject po = query.get(taskUUID);
            ParseFile photoFile = (ParseFile) po.getParseFile(TASK_PHOTO);
            byte[] file = photoFile.getData();
            image = BitmapFactory.decodeByteArray(file, 0, file.length);

        } catch (ParseException e) {
            Log.d(BasedParseUtils.TAG, "Could not find photo for task");
        } catch (NullPointerException npe) {
            Log.e(TAG, "null pointer exception happened man", npe);
        }

        return image;

    }

    public static void setPhotoFile(ParseFile file, String taskUUID) {
        ParseObject task = BasedParseUtils.getTaskById(taskUUID);

        task.put(TASK_PHOTO, file);
        try {
            task.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        task.saveInBackground();
    }
}
