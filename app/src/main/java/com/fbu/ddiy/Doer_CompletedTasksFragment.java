// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class Doer_CompletedTasksFragment extends Fragment {
    /**
     * ******************************************************************************************
     * Member Variables
     */
    private boolean wentThrough = false;
    private List<Task> tasks = new ArrayList<>();
    private RecyclerView mDoerCompleteTasksRecyclerView;
    private LinearLayoutManager llm;
    private static String userUUID;
    private View view;

    /**
     * ******************************************************************************************
     * SubClasses
     */
    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.TaskViewHolder> {
        List<Task> rvTasks;

        public class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView taskTitle;
            TextView taskStatus;
            TextView taskDetails;
            TextView taskPrice;
            ProfilePictureView taskMakerPic;
            RelativeLayout rl;
            public Task currentItem;

            TaskViewHolder(View itemView) {
                super(itemView);

                rl = (RelativeLayout) itemView.findViewById(R.id
                        .complete_tasks_fragment_relative_layout);
                rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle myBundle = new Bundle();
                        myBundle.putString(BasedParseUtils.TASK_ID, currentItem.taskUUID);
                        myBundle.putBoolean(BasedParseUtils.TO_ACCEPT, false);

                        Fragment fragment = new TaskFragment();
                        fragment.setArguments(myBundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                                .addToBackStack(null).commit();
                    }
                });

                taskTitle = (TextView)
                        itemView.findViewById(R.id.complete_tasks_fragment_task_title);
                taskStatus = (TextView) itemView.findViewById(R.id.complete_tasks_fragment_status);
                taskDetails = (TextView) itemView.findViewById(R.id.complete_tasks_fragment_detail);
                taskPrice = (TextView) itemView.findViewById(R.id.complete_tasks_fragment_price);
                taskMakerPic = (ProfilePictureView) itemView.findViewById(R.id
                        .complete_tasks_fragment_pic);
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
            holder.taskTitle.setText(rvTasks.get(position).taskTitle);
            holder.taskDetails.setText(rvTasks.get(position)
                    .taskDetails);
            if (!rvTasks.get(position).taskMakerUUID.equals(""))
                holder.taskMakerPic.setProfileId(rvTasks.get(position).taskMakerUUID);
            holder.taskStatus.setText(rvTasks.get(position).taskStatus);
            holder.taskPrice.setText("$" + rvTasks.get(position).taskPrice);
            holder.currentItem = rvTasks.get(position);
        }

        @Override
        public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.doer_complete_tasks_fragment_card_view, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return rvTasks.size();
        }
    }

    class Task {
        String taskUUID;
        String taskTitle;
        String taskDetails;
        String taskStatus;
        String taskMakerUUID;
        String taskPrice;


        Task(String taskUUID, String taskTitle, String taskDetails, String taskMakerUUID, String
                taskStatus, String taskPrice) {
            this.taskUUID = taskUUID;
            this.taskTitle = taskTitle;
            this.taskDetails = taskDetails;
            this.taskMakerUUID = taskMakerUUID;
            this.taskStatus = taskStatus;
            this.taskPrice = taskPrice;
        }
    }

    /**
     * ******************************************************************************************
     * Methods
     */
    public static Doer_CompletedTasksFragment newInstance() {
        return new Doer_CompletedTasksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userUUID = ParseUser.getCurrentUser().getObjectId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.doer_complete_tasks_fragment, container, false);
        llm = new LinearLayoutManager(container.getContext());

        getActivity().setTitle("Tasks You've Done");
        new CollectTasks().execute();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new CollectTasks().execute();
    }

    /**
     * ******************************************************************************************
     * Helper Methods
     */
    public class CollectTasks extends AsyncTask<String, Void, List<ParseObject>> {
        protected List<ParseObject> doInBackground(String... strings) {
            return BasedParseUtils.getCompletedTasks(userUUID);
        }

        protected void onPostExecute(List<ParseObject> list) {
            tasks.clear();

            if (wentThrough) {
                for (ParseObject po : list) {
                    String temp = po.getString(BasedParseUtils
                            .TASK_CREATOR_PHOTO);
                    Task newTask = new Task(po.getObjectId(),
                            po.getString(BasedParseUtils.TASK_TITLE),
                            po.getString(BasedParseUtils.TASK_DETAILS),
                            po.getString(BasedParseUtils.TASK_CREATOR_PHOTO),
                            po.getString(BasedParseUtils.TASK_STATUS),
                            po.getString(BasedParseUtils.TASK_PRICE));
                    tasks.add(newTask);
                }

                mDoerCompleteTasksRecyclerView = (RecyclerView) view
                        .findViewById(R.id.doer_complete_tasks_recycler_view);

                mDoerCompleteTasksRecyclerView.setHasFixedSize(true);
                RVAdapter adapter = new RVAdapter(tasks);
                mDoerCompleteTasksRecyclerView.setAdapter(adapter);
                mDoerCompleteTasksRecyclerView.setLayoutManager(llm);

                if (tasks.size() == 0) {
                    View help = view.findViewById(R.id.doer_complete_fragment_empty_view);
                    help.setVisibility(View.VISIBLE);
                    mDoerCompleteTasksRecyclerView.setVisibility(View.INVISIBLE);
                }

                wentThrough = !wentThrough;
            } else wentThrough = !wentThrough;
        }
    }
}
