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

public class NotificationFragment extends Fragment {
    /**
     * *******************************************************************************************
     * Member Variables
     */
    private static boolean notificationsIsOpen;
    private int chuckIsGod = 0;
    private LinearLayoutManager llm;
    private List<Task> tasks = new ArrayList<>();
    private RecyclerView rv;
    private String userUUID;
    private View view;

    /**
     * *******************************************************************************************
     * Subclasses
     */
    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.TaskViewHolder> {
        List<Task> rvTasks;

        public class TaskViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout rl;
            TextView taskTitle;
            TextView taskDetails;
            TextView taskDate;
            TextView taskTime;
            ProfilePictureView taskMakerPic;


            TaskViewHolder(View itemView) {
                super(itemView);

                rl = (RelativeLayout) itemView.findViewById(R.id
                        .notification_fragment_relative_layout);

                taskTitle = (TextView)
                        itemView.findViewById(R.id.notification_fragment_task_title);

                taskDetails = (TextView) itemView.findViewById(R.id.notification_fragment_details);

                taskDate = (TextView) itemView.findViewById(R.id.notification_fragment_date);

                taskTime = (TextView) itemView.findViewById(R.id.notification_fragment_time);

                taskMakerPic = (ProfilePictureView) itemView.findViewById(R.id
                        .notification_fragment_pic);

            }
        }

        RVAdapter(List<Task> tasks) {
            rvTasks = tasks;
        }

        @Override
        public void onBindViewHolder(TaskViewHolder holder, final int position) {
            holder.rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BasedParseUtils.TASK_ID, rvTasks.get(position).taskUUID);

                    Fragment fragment = new TaskAcceptedFragment();
                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                            .addToBackStack(null).commit();
                }
            });
            holder.taskTitle.setText(rvTasks.get(position).taskTitle);
            holder.taskDetails.setText("Someone signed up to do this!");
            holder.taskDate.setText(rvTasks.get(position).taskDate);
            holder.taskTime.setText(rvTasks.get(position).taskTime);
            if (!rvTasks.get(position).taskDoerUUID.equals(""))
                holder.taskMakerPic.setProfileId(rvTasks.get(position).taskDoerUUID);
        }

        @Override
        public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.notification_fragment_card_view, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return rvTasks.size();
        }
    }

    class Task {
        String taskTitle;
        String taskUUID;
        String taskDetails;
        String taskDate;
        String taskTime;
        String taskDoerUUID;

        Task(String taskUUID, String taskTitle, String taskDetails, String taskDate, String
                taskTime, String taskDoerUUID) {
            this.taskTitle = taskTitle;
            this.taskUUID = taskUUID;
            this.taskDetails = taskDetails;
            this.taskDate = taskDate;
            this.taskTime = taskTime;
            this.taskDoerUUID = taskDoerUUID;
        }
    }

    /**
     * *******************************************************************************************
     * Methods
     */
    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationsIsOpen = true;
        userUUID = ParseUser.getCurrentUser().getObjectId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.notification_fragment, container, false);
        getActivity().setTitle("Notifications");
        llm = new LinearLayoutManager(getActivity().getApplicationContext());
        new CollectTasks().execute();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Notifications");
        new CollectTasks().execute();
    }

    /**
     * *******************************************************************************************
     * Helper Methods
     */
    public class CollectTasks extends AsyncTask<String, Void, FaultofAshley> {
        protected FaultofAshley doInBackground(String... strings) {
            List<ParseObject> taskList = BasedParseUtils.getNotifiableTasksByUserID(userUUID);
            ArrayList<String> arrayList = new ArrayList<>();

            for (ParseObject po : taskList) {
                String picID;

                if (!BasedParseUtils.getAcceptorsByTaskID(po.getObjectId()).get(0)
                        .getBoolean(BasedParseUtils.USER_CONNECTED)) picID = "";
                else {
                    ParseUser pu = BasedParseUtils.getAcceptorsByTaskID(po.getObjectId()).get(0);
                    picID = pu.getString(BasedParseUtils.USER_PIC_ID);
                }

                arrayList.add(picID);
            }

            return new FaultofAshley(taskList, arrayList);
        }

        protected void onPostExecute(FaultofAshley fault) {
            if (chuckIsGod == 0) {
                tasks.clear();

                List<ParseObject> list = fault.list;
                List<String> picIDs = fault.picID;

                for (int i = 0; i < list.size(); i++) {
                    ParseObject po = list.get(i);

                    Task newTask = new Task(po.getObjectId(), po.getString(BasedParseUtils.TASK_TITLE),
                            po.getString(BasedParseUtils.TASK_DETAILS),
                            po.getString(BasedParseUtils.TASK_DATE),
                            po.getString(BasedParseUtils.TASK_TIME), picIDs.get(i));
                    tasks.add(newTask);
                }

                rv = (RecyclerView) view.findViewById(R.id.notification_fragment_recycler_view);
                rv.setHasFixedSize(true);
                rv.setAdapter(new RVAdapter(tasks));
                rv.setLayoutManager(llm);
                chuckIsGod++;

                if (tasks.size() == 0) {
                    View help = view.findViewById(R.id.empty_view_here);
                    help.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.INVISIBLE);
                }
            } else { chuckIsGod = 0; }
        }
    }

    public class FaultofAshley {
        List<ParseObject> list;
        List<String> picID;

        FaultofAshley(List<ParseObject> list, List<String> picID) {
            this.list = list;
            this.picID = picID;
        }
    }

    public static boolean getNotificationsIsOpen() {
        return notificationsIsOpen;
    }

    public static void setNotificationsIsOpen(boolean b) {
        notificationsIsOpen = b;
    }
}
