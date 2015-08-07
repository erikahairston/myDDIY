// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddTaskFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    /**
     * *******************************************************************************************
     * Constants
     */
    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;

    /**
     * *******************************************************************************************
     * Member Variables
     */
    private boolean savedThroughCheck;
    private Button addTaskButton;
    private Button mDateButton;
    private Date mDate;
    private EditText taskDetails;
    private EditText taskPrice;
    private EditText taskTitle;
    private Spinner mSpinner;
    private Spinner taskCategories;
    private Spinner taskTimeFrames;
    private String mTimeFrame;
    private static ParseObject draftTask;
    private ParseObject saveTask;

    private static ParseObject currentTask;

    /**********************************************************************************************
     * Methods
     */
    public static AddTaskFragment newInstance() { return new AddTaskFragment(); }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            setDate(date);
            updateDate();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        savedThroughCheck = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_task_fragment, container, false);
        ParseUser parseUser = ParseUser.getCurrentUser();
        savedThroughCheck = false;
        taskTitle = (EditText) v.findViewById(R.id.task_title_edit_text);
        taskDetails = (EditText) v.findViewById(R.id.task_details_edit_text);
        taskPrice = (EditText) v.findViewById(R.id.task_price_edit_text);
        mDateButton = (Button) v.findViewById(R.id.datePicker);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(getDate());
                dialog.setTargetFragment(AddTaskFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        taskTimeFrames = ((Spinner) v.findViewById(R.id.time_frames));
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
                .createFromResource(getActivity(), R.array.task_time_frame,
                        android.R.layout.simple_spinner_dropdown_item);
        taskTimeFrames.setAdapter(spinnerAdapter);
        taskTimeFrames.setOnItemSelectedListener(this);

        taskCategories = (Spinner) v.findViewById(R.id.category_spinner);
        ArrayList<String> spinnerCategoryArray = new ArrayList<String>(BasedParseUtils
                .getAllCategories());
        spinnerCategoryArray.add(0, "Choose a Category");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, spinnerCategoryArray);
        taskCategories.setAdapter(spinnerArrayAdapter);

        ArrayList<ParseObject> drafts = (ArrayList<ParseObject>) BasedParseUtils
                .getDraftsByUser(parseUser
                .getObjectId());

        if (drafts.size() > 0) {
            ParseObject mostRecentDraft = drafts.get(0);
            if (!mostRecentDraft.getString(BasedParseUtils.TASK_CATEGORY)
                    .equals("Choose a Category"))
                taskCategories.setSelection(getPositionOfSpinnerForDraft(
                    taskCategories,
                    mostRecentDraft.getString(BasedParseUtils.TASK_CATEGORY)));
            if (!mostRecentDraft.getString(BasedParseUtils.TASK_TIME).equals("Pick A Time Frame"))
                taskTimeFrames.setSelection(getPositionOfSpinnerForDraft(
                    taskTimeFrames,
                    mostRecentDraft.getString(BasedParseUtils.TASK_TIME)));
            if (mostRecentDraft.getString(BasedParseUtils.TASK_TITLE).length() > 0)
                taskTitle.setText(mostRecentDraft.getString(BasedParseUtils.TASK_TITLE));
            if (mostRecentDraft.getString(BasedParseUtils.TASK_DETAILS).length() > 0)
                taskDetails.setText(mostRecentDraft.getString(BasedParseUtils.TASK_DETAILS));
            if (mostRecentDraft.getString(BasedParseUtils.TASK_PRICE).length() > 0)
                taskPrice.setText(mostRecentDraft.getString(BasedParseUtils.TASK_PRICE));
            if (!mostRecentDraft.getString(BasedParseUtils.TASK_DATE).equals("Choose Date"))
                //TODO: change the date picker to that date when click on date button
                mDateButton.setText(mostRecentDraft.getString(BasedParseUtils.TASK_DATE));
        }
        return v;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mTimeFrame = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.save_task:
                savedThroughCheck = true;
                saveTask();
                return true;
            case R.id.close_add_task:
                //TODO:save as draft
                savedThroughCheck = true;
                if (hasFilledOutFields()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.dialog_add_task_text)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    savedThroughCheck = false;
                                    saveDraft();
                                    getActivity().finish();
                                }
                            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AddTaskFragment.deleteFormFromDialog();
                                    getActivity().finish();
                                }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                } else getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        saveDraft();
    }

    /**********************************************************************************************
     * Helper Methods
     */
    public static String getTaskChannel(String taskUUID) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("task_object_");
        stringBuilder.append(taskUUID);
        String taskChannel = stringBuilder.toString();
        return taskChannel;
    }



    private boolean datePassed(String text) {
        //DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Calendar date2 = Calendar.getInstance();
        date2.add(Calendar.DATE, -1);

        Date date1 = null;
        try { date1 = format.parse(text); }
        catch (java.text.ParseException e) { e.printStackTrace(); }

        if (date1.compareTo(date2.getTime()) >= 0) return false;
        else return true;
    }

    public static void deleteFormFromDialog() {
        if (BasedParseUtils.getDraftsByUser(ParseUser.getCurrentUser().getObjectId()).size() >
                0) {
            currentTask = BasedParseUtils.getDraftsByUser(ParseUser.getCurrentUser()
                    .getObjectId()).remove(0);
            currentTask.put(BasedParseUtils.TASK_STATUS, BasedParseUtils.TASK_STATUS_DELETED);
            currentTask.saveEventually();
        }
    }

    public Date getDate() {
        if (mDate == null) mDate = new Date();
        return mDate;
    }

    private int getPositionOfSpinnerForDraft(Spinner b, String c) {
        for (int x = 0; x < b.getAdapter().getCount(); x++)
            if (b.getAdapter().getItem(x).toString().equals(c)) return x;
        return 0;
    }

    public boolean hasEmptyFields() {
        if (taskCategories.getSelectedItem().toString().equals("Choose a Category") ||
                taskTimeFrames.getSelectedItem().toString().equals("Pick A Time Frame")
                || taskTitle.getText().length() == 0 || taskDetails.getText().length() ==
                0 || taskPrice.getText().length() == 0 || mDateButton.getText().equals
                ("Choose Date")) return true;
        else return false;
    }

    public boolean hasFilledOutFields() {
        if (!taskCategories.getSelectedItem().toString().equals("Choose a Category") ||
                !taskTimeFrames.getSelectedItem().toString().equals("Pick A Time Frame")
                || taskTitle.getText().length() > 0 || taskDetails.getText().length() >
                0 || taskPrice.getText().length() > 0 || !mDateButton.getText().equals
                ("Choose Date")) return true;
        else return false;
    }

    private void saveDraft() {
        if (hasFilledOutFields() && !savedThroughCheck) {
            if (BasedParseUtils.getDraftsByUser(ParseUser.getCurrentUser().getObjectId()).size() >
                    0)
                draftTask = BasedParseUtils.getDraftsByUser(ParseUser.getCurrentUser()
                        .getObjectId()).get(0);
            else
                draftTask = new ParseObject(BasedParseUtils.TASK_TABLE);

            draftTask.put(
                    BasedParseUtils.TASK_STATUS,
                    BasedParseUtils
                            .TASK_STATUS_DRAFT);
            draftTask.put(BasedParseUtils.TASK_CREATOR, ParseUser.getCurrentUser());
            draftTask.put(BasedParseUtils.TASK_NUMBER_OF_ACCEPTANCES, 0);

            draftTask.put(BasedParseUtils.TASK_TITLE, taskTitle.getText().toString());
            draftTask.put(BasedParseUtils.TASK_DETAILS, taskDetails.getText().toString());
            draftTask.put(BasedParseUtils.TASK_PRICE, taskPrice.getText().toString());
            draftTask.put(BasedParseUtils.TASK_CATEGORY, taskCategories.getSelectedItem().toString());
            draftTask.put(BasedParseUtils.TASK_DATE, mDateButton.getText().toString());
            int temp = taskTimeFrames.getSelectedItemPosition();
            draftTask.put(
                    BasedParseUtils.TASK_TIME,
                    BasedParseUtils.TIME_ARRAY
                            [taskTimeFrames.getSelectedItemPosition()]);
            try {
                draftTask.save();
            } catch (ParseException e) {
                Log.d("DDIY", "GDI");
            }
        }
    }

    private void saveTask() {
        if (hasEmptyFields()) {
            Toast.makeText(getActivity(), "Please Fill In All Fields", Toast.LENGTH_SHORT)
                    .show();
        } else if (!mDateButton.getText().equals
                ("Choose Date") && datePassed(mDateButton.getText().toString())) {
            Toast.makeText(getActivity(), "Please Choose a Valid Date", Toast.LENGTH_SHORT);
        } else {
            if (BasedParseUtils.getDraftsByUser(ParseUser.getCurrentUser().getObjectId()).size() >
                    0) {
                saveTask = BasedParseUtils.getDraftsByUser(ParseUser.getCurrentUser()
                        .getObjectId()).get(0);
            } else {
                saveTask = new ParseObject(BasedParseUtils.TASK_TABLE);
            }
            saveTask.put(BasedParseUtils.TASK_TITLE, taskTitle.getText().toString());
            saveTask.put(BasedParseUtils.TASK_DETAILS, taskDetails.getText().toString());
            saveTask.put(BasedParseUtils.TASK_PRICE, taskPrice.getText().toString());
            saveTask.put(BasedParseUtils.TASK_STATUS, BasedParseUtils.TASK_STATUS_CREATED);
            saveTask.put(BasedParseUtils.TASK_NUMBER_OF_ACCEPTANCES, 0);
            saveTask.put(BasedParseUtils.TASK_CATEGORY, taskCategories.getSelectedItem().toString());
            saveTask.put(BasedParseUtils.TASK_CREATOR, ParseUser.getCurrentUser());
            saveTask.put(BasedParseUtils.TASK_DATE, mDateButton.getText().toString());
            saveTask.put(
                    BasedParseUtils.TASK_TIME,
                    BasedParseUtils.TIME_ARRAY
                            [taskTimeFrames.getSelectedItemPosition()]);
            saveTask.put(
                "Task_Creator_Pic_Id",
                ParseUser.getCurrentUser().getString
                    (BasedParseUtils.USER_PIC_ID));

            try {
                saveTask.save();
                ParsePush.subscribeInBackground(getTaskChannel(saveTask.getObjectId().toString()));

            } catch (ParseException e) {
                Log.d("DDIY", "GDI");
            }

            Toast.makeText(getActivity(), "Task Saved", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

    }

    public void setDate(Date date) { mDate = date; }

    private void updateDate() {
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
        String real_date = dateFormat.format(getDate());
        mDateButton.setText(real_date);
    }
}
