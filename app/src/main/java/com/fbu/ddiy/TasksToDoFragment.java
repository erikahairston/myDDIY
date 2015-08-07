// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by erikahairston on 7/21/15. This fragment attatches to VENMO
 */
public class TasksToDoFragment extends Fragment {
    /**
     * *******************************************************************************************
     * Member Variables
     */
    private LinearLayoutManager llm;
    private List<Task> tasks;
    private Resources res;
    private RecyclerView mDoerInCompleteTasksRecyclerView;
    private View view;
    int pos;

    /**
     * *******************************************************************************************
     * Bundled Constants
     */
    private static String userUUID;
    /**
     * *******************************************************************************************
     * Constants
     */
    private static final int RESULT_CANCELED = 0;
    private static final int RESULT_OK = -1;
    private static final int VENMO_REQUEST_CODE = 1;

    private static final String CHARGE = "charge";


    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.TaskViewHolder> {
        List<Task> rvTasks;

        public class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView taskTitle;
            TextView taskStatus;
            TextView taskMakerName;
            TextView taskPrice;
            ProfilePictureView taskMakerPic;
            RelativeLayout rl;

            TaskViewHolder(View itemView) {
                super(itemView);

                rl = (RelativeLayout) itemView
                        .findViewById(R.id
                                .tasks_todo_rl);
                taskTitle = (TextView)
                        itemView.findViewById(R.id.tasks_todo_title);
                taskStatus = (TextView) itemView.findViewById(R.id.tasks_todo_status);
                taskMakerName = (TextView) itemView.findViewById(R.id.tasks_todo_detail);
                taskPrice = (TextView) itemView.findViewById(R.id.tasks_todo_price_request);
                taskMakerPic = (ProfilePictureView) itemView.findViewById(R.id.tasks_todo_pic);

            }
        }

        RVAdapter(List<Task> tasks) {
            rvTasks = tasks;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onBindViewHolder(TaskViewHolder holder, final int position) {
            pos = position;
            //If you are done doing the task, you click the rl to open venmo
            holder.rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (VenmoLibrary.isVenmoInstalled(getActivity().getApplicationContext())) {
                        BasedParseUtils.changeTaskToCompleted(rvTasks.get(position).taskUUID);
                        startTransactionInApp(CHARGE, rvTasks.get(pos).taskUUID);

                        //go back to home fragment
                        Fragment fragment = new CategoryFragment();
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                                .commit();
                    } else {
                    } //TODO: make alternate plan (gets ride of security issue)
                }
            });

            holder.taskMakerPic.setProfileId(rvTasks.get(position).taskMakerPicID);
            holder.taskTitle.setText(rvTasks.get(position).taskTitle);
            holder.taskStatus.setText(rvTasks.get(position).taskStatus);
            holder.taskMakerName.setText("Request payment from " + rvTasks.get(position).taskMakerName);
            holder.taskPrice.setText(rvTasks.get(position).taskPrice);

        }

        @Override
        public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.tasks_todo_fragment_card_view, parent, false);
            TaskViewHolder tvh = new TaskViewHolder(view);
            return tvh;
        }

        @Override
        public int getItemCount() {
            return rvTasks.size();
        }
    }

    class Task {
        String taskUUID;
        String taskTitle;
        String taskMakerName;
        String taskStatus;
        String taskMakerPicID;
        String taskPrice;


        Task(String taskUUID, String taskTitle, String taskMakerName, String taskMakerPicID,
             String taskStatus, String taskPrice) {
            this.taskUUID = taskUUID;
            this.taskTitle = taskTitle;
            this.taskMakerName = taskMakerName;
            this.taskMakerPicID = taskMakerPicID;
            this.taskStatus = taskStatus;
            this.taskPrice = taskPrice;
        }
    }

    public static TasksToDoFragment newInstance() {
        return new TasksToDoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userUUID = ParseUser.getCurrentUser().getObjectId();
        tasks = new ArrayList<>();
        res = getResources();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tasks_todo_fragment, container, false);
        getActivity().setTitle("Tasks To Do");
        llm = new LinearLayoutManager(getActivity());

        new CollectTasks().execute();


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new CollectTasks().execute();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case VENMO_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {
                    String signedrequest = intent.getStringExtra("signedrequest");
                    if (signedrequest != null) {
                        VenmoLibrary.VenmoResponse response = (new VenmoLibrary())
                                .validateVenmoPaymentResponse(signedrequest,
                                        res.getString(R.string.venmo_secret));
                    } else {
                        String error_message = intent.getStringExtra("error_message");
                    }
                } else if (resultCode == RESULT_CANCELED) {
                }
                break;
            }
        }
    }

    /**
     * *******************************************************************************************
     * Helper Methods
     */
    public class CollectTasks extends AsyncTask<String, Void, List<Task>> {
        @Override
        protected List<Task> doInBackground(String... params) {
            List<Task> list = new ArrayList<>();
            List<ParseObject> parseObjects = BasedParseUtils.getTasksByDoer(userUUID);

            for (ParseObject po : parseObjects) {
                String picID;
                /**        if (po.getParseUser(BasedParseUtils.TASK_CREATOR)
                 .getBoolean(BasedParseUtils.USER_CONNECTED)) picID = "";
                 else picID = po.getParseUser(BasedParseUtils.TASK_CREATOR)
                 .getString(BasedParseUtils.USER_PIC_ID);
                 */
                Task newTask = new Task(po.getObjectId(), po.getString(BasedParseUtils
                        .TASK_TITLE), po.getParseUser(BasedParseUtils.TASK_CREATOR)
                        .getString(BasedParseUtils.USER_NAME), po.getString(BasedParseUtils
                        .TASK_CREATOR_PHOTO),
                        po.getString(BasedParseUtils.TASK_STATUS),
                        po.getString(BasedParseUtils.TASK_PRICE));

                list.add(newTask);
            }

            return list;
        }

        @Override
        protected void onPostExecute(List<Task> list) {
            tasks.clear();
            for (Task po : list) tasks.add(po);
            mDoerInCompleteTasksRecyclerView = (RecyclerView) view
                    .findViewById(R.id.doer_incomplete_tasks_recycler_view);
            mDoerInCompleteTasksRecyclerView.setHasFixedSize(true);
            mDoerInCompleteTasksRecyclerView.setAdapter(new RVAdapter(tasks));
            mDoerInCompleteTasksRecyclerView.setLayoutManager(llm);

            if (tasks.size() == 0) {
                View help = view.findViewById(R.id.empty_view);
                help.setVisibility(View.VISIBLE);
                mDoerInCompleteTasksRecyclerView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void doTransactionInApp(String recipient, String amount, String note, String txn) {
        try {
            Intent venmoIntent = VenmoLibrary.openVenmoPayment
                    (res.getString(R.string.venmo_app_id_int), "DDIY", recipient, amount, note, txn);
            startActivityForResult(venmoIntent, VENMO_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException e) {
            Log.d(BasedParseUtils.TAG, "This returned an error with Venmo");
        }
    }

    private void startTransactionInApp(String txn, String taskUUID) {
        ParseObject task = BasedParseUtils.getTaskById(taskUUID);

        String recipient = task.getParseUser(BasedParseUtils.TASK_CREATOR)
                .getString(BasedParseUtils.USER_PHONE_NUMBER);

        if (recipient == null) {
            recipient = task.getParseUser(BasedParseUtils.TASK_CREATOR)
                    .getString(BasedParseUtils.USER_EMAIL);
        }

        String amount = task.getString(BasedParseUtils.TASK_PRICE);
        String note = task.getString(BasedParseUtils.TASK_TITLE);

        doTransactionInApp(recipient, amount, note, txn);
    }
}
