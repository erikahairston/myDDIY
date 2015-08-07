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
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MyCreated_InCompleteTasksFragment extends Fragment {
    /**
     * *******************************************************************************************
     * Bundled Constants
     */
    private LayoutInflater mInflater;
    private LinearLayoutManager llm;
    private String userUUID;
    private RecyclerView rv;
    private ViewGroup mContainer;
    private View view;

    /**
     * *******************************************************************************************
     * SubClasses
     */
    public static MyCreated_InCompleteTasksFragment newInstance() {
        return new MyCreated_InCompleteTasksFragment();
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.TaskViewHolder> {
        List<Task> rvTasks;


        public class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView taskTitle;
            TextView taskDetail;
            TextView taskDate;
            TextView taskTime;
            SwipeLayout rv;
            ImageView delete;
            public Task currentItem;


            TaskViewHolder(View itemView) {
                super(itemView);
                rv = (SwipeLayout) itemView.findViewById(R.id
                        .swipe);
                rv.addDrag(SwipeLayout.DragEdge.Left, rv.findViewById(R.id.trash));

                rv.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle myBundle = new Bundle();
                        myBundle.putString(BasedParseUtils.TASK_ID, currentItem.uuid);
                        myBundle.putBoolean(BasedParseUtils.TO_ACCEPT, false);

                        Fragment fragment = new TaskFragment();
                        fragment.setArguments(myBundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                                .addToBackStack(null).commit();
                    }
                });


                taskTitle = (TextView)
                        itemView.findViewById(R.id.incomplete_tasks_fragment_name);
                taskDetail = (TextView) itemView.findViewById(R.id
                        .incomplete_tasks_fragment_detail);
                taskDate = (TextView) itemView.findViewById(R.id.incomplete_tasks_fragment_date);
                taskTime = (TextView) itemView.findViewById(R.id.incomplete_tasks_time);
                delete = (ImageView) itemView.findViewById(R.id.trash_button);
            }
        }

        public RVAdapter(List<Task> tasks) {
            rvTasks = tasks;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onBindViewHolder(TaskViewHolder holder, final int position) {
            holder.taskTitle.setText(rvTasks.get(position).taskTitle);
            holder.taskDate.setText(rvTasks.get(position).taskDate);
            holder.taskTime.setText(rvTasks.get(position).taskTime);
            holder.taskDetail.setText(rvTasks.get(position).taskDetails);
            holder.currentItem = rvTasks.get(position);
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BasedParseUtils.deleteTask(rvTasks.get(position).uuid);
                    new CollectTasks().execute();
                }
            });
        }

        @Override
        public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(getActivity()).inflate(
                    R.layout.mycreated_incomplete_tasks_fragment_card_view,
                    parent,
                    false);
            return new TaskViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return rvTasks.size();
        }
    }

    class Task {
        String uuid;
        String taskTitle;
        String taskDate;
        String taskTime;
        String taskDetails;

        Task(String uuid, String taskTitle, String taskDate, String taskTime, String taskDetails) {
            this.uuid = uuid;
            this.taskTitle = taskTitle;
            this.taskDate = taskDate;
            this.taskTime = taskTime;
            this.taskDetails = taskDetails;
        }
    }

    /**
     * *******************************************************************************************
     * Member Variables
     */
    private List<Task> tasks = new ArrayList<>();

    /**
     * *******************************************************************************************
     * Methods
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userUUID = ParseUser.getCurrentUser().getObjectId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.mycreated_incomplete_tasks_fragment, container,
                false);
        rv = (RecyclerView) view.findViewById(R.id.incomplete_tasks_recycler_view);
        llm = new LinearLayoutManager(container.getContext());
        getActivity().setTitle("My Created Tasks");
        new CollectTasks().execute();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("My Created Tasks");
        new CollectTasks().execute();
    }

    /*********************************************************************************************
     * Helper Methods
     */
    public class CollectTasks extends AsyncTask<String, Void, List<Task>> {
        @Override
        protected List<Task> doInBackground(String... params) {
            List<Task> list = new ArrayList<>();

            List<ParseObject> pOs = BasedParseUtils.getTasksByCreator(userUUID);

            for (ParseObject po : pOs) {
                Task newTask = new Task(po.getObjectId(), po.getString(BasedParseUtils.TASK_TITLE),
                        po.getString(BasedParseUtils.TASK_DATE), po.getString(BasedParseUtils
                        .TASK_TIME), po.getString(BasedParseUtils.TASK_DETAILS));
                list.add(newTask);
            }

            return list;
        }

        @Override
        protected void onPostExecute(List<Task> list) {
            tasks.clear();
            for (Task po : list) tasks.add(po);
            rv.setHasFixedSize(true);
            rv.setAdapter(new RVAdapter(tasks));
            rv.setLayoutManager(llm);

            if (tasks.size() == 0) {
                View help = view.findViewById(R.id.empty_view);
                help.setVisibility(View.VISIBLE);
                rv.setVisibility(View.INVISIBLE);
            }
        }
    }
}
