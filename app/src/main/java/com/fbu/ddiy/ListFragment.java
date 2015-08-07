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

/**
 * finished
 */
public class ListFragment extends Fragment {
    /**
     * ******************************************************************************************
     * Bundled Constants
     */
    private static String category;


    /**
     * ******************************************************************************************
     * Member Variables
     */
    private ArrayList<Task> tasks = new ArrayList<>();
    private static String school;
    private static String userUUID;
    private int times = 0;

    private RecyclerView recyclerView;
    private LinearLayoutManager llm;
    private View view;

    /**
     * ******************************************************************************************
     * SubClasses
     */
    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.TaskViewHolder> {
        List<Task> rvTasks;

        public class TaskViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout rl;
            TextView taskTitle;
            TextView taskDetails;
            TextView taskDate;
            TextView taskTime;
            ProfilePictureView taskUserPic;
            public Task currentItem;

            TaskViewHolder(View itemView) {
                super(itemView);
                rl = (RelativeLayout) itemView.findViewById(R.id
                        .cardview_relative_layout_list_fragment);
                rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle myBundle = new Bundle();
                        myBundle.putString(BasedParseUtils.TASK_ID, currentItem.uuid);

                        Fragment fragment = new TaskFragment();
                        fragment.setArguments(myBundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                                .addToBackStack(null).commit();
                    }
                });

                taskTitle = (TextView)
                        itemView.findViewById(R.id.list_fragment_task_name);
                taskDetails = (TextView) itemView.findViewById(R.id.list_fragment_task_detail);
                taskDate = (TextView) itemView.findViewById(R.id.list_fragment_task_date);
                taskTime = (TextView) itemView.findViewById(R.id.list_fragment_task_time);
                taskUserPic = (ProfilePictureView) itemView.findViewById(R.id
                        .list_fragment_pic);
            }
        }

        RVAdapter(List<Task> tasks) {
            rvTasks = tasks;
        }

        @Override
        public void onBindViewHolder(TaskViewHolder holder, final int position) {
            holder.taskTitle.setText(rvTasks.get(position).taskTitle);
            holder.taskDate.setText(rvTasks.get(position).taskDate);
            holder.taskTime.setText(rvTasks.get(position).taskTime);
            holder.taskDetails.setText(rvTasks.get(position).taskDetails);

            holder.taskUserPic.setProfileId(rvTasks.get(position).taskMakerUUID);

            holder.currentItem = rvTasks.get(position);
        }

        @Override
        public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_fragment_card_view, parent, false);
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
        String taskMakerUUID;

        Task(String uuid, String taskTitle, String taskDate, String taskTime, String taskDetails,
             String taskMakerUUID) {
            this.uuid = uuid;
            this.taskTitle = taskTitle;
            this.taskDate = taskDate;
            this.taskTime = taskTime;
            this.taskDetails = taskDetails;
            this.taskMakerUUID = taskMakerUUID;
        }
    }

    /**
     * ******************************************************************************************
     * Methods
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle receivedBundle = getArguments();

        category = receivedBundle.getString(BasedParseUtils.CAT_NAME);
        school = ParseUser.getCurrentUser().getString(BasedParseUtils.USER_SCHOOL);
        userUUID = ParseUser.getCurrentUser().getObjectId();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.list_fragment, container, false);
        llm = new LinearLayoutManager(container.getContext());
        new CollectTasks().execute();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Categories");
        tasks.clear();
        new CollectTasks().execute();
    }

    /**
     * *******************************************************************************************
     * Helper Methods
     */
    public class CollectTasks extends AsyncTask<String, Void, List<Task>> {
        protected List<Task> doInBackground(String... strings) {
            List<Task> tasks = new ArrayList<>();

            List<ParseObject> list =
                    BasedParseUtils.getTasksBySchoolAndCat(userUUID, school, category);

            for (ParseObject po : list) {
                Task newTask = new Task(po.getObjectId(), po.getString(BasedParseUtils.TASK_TITLE),
                        po.getString(BasedParseUtils.TASK_DATE), po.getString(BasedParseUtils
                        .TASK_TIME), po.getString(BasedParseUtils.TASK_DETAILS), po.getString
                        (BasedParseUtils.TASK_CREATOR_PHOTO));
                tasks.add(newTask);
            }
            return tasks;
        }

        protected void onPostExecute(List<Task> list) {
            if (times == 0) times++;
            else {
                tasks.clear();
                for (Task po : list) tasks.add(po);
                recyclerView = (RecyclerView) view.findViewById(R.id.list_fragment_recycler_view);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(new RVAdapter(tasks));
                recyclerView.setLayoutManager(llm);
                times = 0;

                if (tasks.size() == 0) {
                    View help = view.findViewById(R.id.list_fragment_empty_view);
                    help.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}
