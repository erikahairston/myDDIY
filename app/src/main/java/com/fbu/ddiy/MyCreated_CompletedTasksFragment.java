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

public class MyCreated_CompletedTasksFragment extends Fragment {
    /**
     * ******************************************************************************************
     * Member Variables
     */
    private LinearLayoutManager llm;
    private List<Task> tasks = new ArrayList<>();
    private RecyclerView rv;
    private String userUUID;
    private View view;

    /**
     * ******************************************************************************************
     * Subclasses
     */
    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.TaskViewHolder> {
        List<Task> rvTasks;

        public class TaskViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout rl;
            TextView taskTitle;
            TextView taskStatus;
            TextView taskDetail;
            ProfilePictureView taskDoerPic;
            public Task currentItem;

            TaskViewHolder(View itemView) {
                super(itemView);

                rl = (RelativeLayout) itemView.findViewById
                        (R.id.cardview_relative_layout_complete_tasks_fragment);
                taskTitle = (TextView)
                        itemView.findViewById(R.id.mycomplete_tasks_fragment_name);
                taskStatus = (TextView) itemView.findViewById(R.id
                        .mycomplete_tasks_fragment_status);
                taskDetail = (TextView) itemView.findViewById(R.id
                        .mycomplete_tasks_fragment_detail);

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

            holder.rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    Fragment fragment;
                    bundle.putString(BasedParseUtils.TASK_ID, rvTasks.get(position).uuid);

                    ParseObject task = BasedParseUtils.getTaskById(rvTasks.get(position).uuid);

                    if (task.getString(BasedParseUtils.TASK_STATUS)
                            .equals(BasedParseUtils.TASK_STATUS_COMPLETED) &&
                            task.getParseUser(BasedParseUtils.TASK_CREATOR)
                                    .equals(ParseUser.getCurrentUser()))
                        fragment = new ReviewFragment();
                    else {
                        bundle.putBoolean(BasedParseUtils.TO_ACCEPT, false);
                        fragment = new TaskFragment();
                    }

                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                            .addToBackStack(null).commit();
                }
            });


            holder.currentItem = rvTasks.get(position);
            holder.taskStatus.setText(rvTasks.get(position).taskStatus);
            holder.taskDetail.setText("Completed by " + rvTasks.get(position).taskDoerName + ". " +
                    "Give this person a review.");

        }

        @Override
        public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mycreated_completed_tasks_fragment_card_view, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return rvTasks.size();
        }
    }

    static class Task {
        String uuid;
        String taskTitle;
        String taskStatus;
        String taskDetail;
        String taskDoerName;

        Task(String uuid, String taskTitle, String taskStatus, String taskDetail, String
                taskDoerName) {
            this.uuid = uuid;
            this.taskTitle = taskTitle;
            this.taskStatus = taskStatus;
            this.taskDetail = taskDetail;
            this.taskDoerName = taskDoerName;
        }
    }

    /**
     * *******************************************************************************************
     * Methods
     */
    public static MyCreated_CompletedTasksFragment newInstance() {
        return new MyCreated_CompletedTasksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userUUID = ParseUser.getCurrentUser().getObjectId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.mycreated_completed_tasks_fragment, container, false);

        getActivity().setTitle("Past Created Tasks");
        llm = new LinearLayoutManager(container.getContext());
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
    public class CollectTasks extends AsyncTask<String, Void, List<Task>> {
        @Override
        protected List<Task> doInBackground(String... params) {
            List<Task> list = new ArrayList<>();
            List<ParseObject> parseObjects = BasedParseUtils.getPastTasks(userUUID);

            for (ParseObject po : parseObjects) {

                Task newTask = new Task(po.getObjectId(),
                        po.getString(BasedParseUtils.TASK_TITLE),
                        po.getString(BasedParseUtils.TASK_STATUS),
                        po.getString(BasedParseUtils.TASK_DETAILS),
                        BasedParseUtils.getDoer(po.getObjectId()).getString(BasedParseUtils
                                .USER_NAME));
                list.add(newTask);
            }

            return list;
        }

        @Override
        protected void onPostExecute(List<Task> list) {
            tasks.clear();
            for (Task po : list) tasks.add(po);
            rv = (RecyclerView) view.findViewById(R.id.completed_tasks_recycler_view);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(llm);
            rv.setAdapter(new RVAdapter(tasks));


            if (tasks.size() == 0) {
                View help = view.findViewById(R.id.empty_view);
                help.setVisibility(View.VISIBLE);
                rv.setVisibility(View.INVISIBLE);
            }
        }
    }
}
