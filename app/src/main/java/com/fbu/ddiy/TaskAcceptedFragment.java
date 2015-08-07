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
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class TaskAcceptedFragment extends Fragment {
    /**
     * ******************************************************************************************
     * Bundled Constants
     */
    String taskUUID;

    /**
     * ******************************************************************************************
     * Member Variables
     */
    ArrayList<Person> persons = new ArrayList<>();
    private LinearLayoutManager llm;
    private RecyclerView rv;
    private View view;

    /**
     * *******************************************************************************************
     * Subclasses
     */
    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder> {
        List<Person> rvPersons;

        public class PersonViewHolder extends RecyclerView.ViewHolder {
            ProfilePictureView profilePic;
            RatingBar doerRating;
            RelativeLayout rl;
            TextView doerName;

            PersonViewHolder(View itemView) {
                super(itemView);
                rl = (RelativeLayout) itemView.findViewById(R.id
                        .cardview_relative_layout_task_accepted_fragment);
                profilePic = (ProfilePictureView)
                        itemView.findViewById(R.id.task_accepted_fragment_pic);
                doerName = (TextView)
                        itemView.findViewById(R.id.task_accepted_fragment_name);
                doerRating = (RatingBar) itemView.findViewById(R.id.task_accepted_fragment_rating);
            }
        }

        RVAdapter(List<Person> persons) {
            rvPersons = persons;
        }

        @Override
        public void onBindViewHolder(PersonViewHolder holder, final int position) {
            holder.rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();

                    bundle.putString(BasedParseUtils.USER_ID, rvPersons.get(position).taskuuid);
                    bundle.putString(BasedParseUtils.TASK_ID, taskUUID);
                    bundle.putBoolean(BasedParseUtils.TO_ACCEPT, true);

                    Fragment fragment = new ProfileFragment();
                    fragment.setArguments(bundle);

                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                            .addToBackStack(null).commit();
                }
            });

            if (rvPersons.get(position).picId != null && !rvPersons.get(position).picId.equals(""))
                holder.profilePic.setProfileId(rvPersons.get(position).picId);
            holder.doerName.setText(rvPersons.get(position).name);
            holder.doerRating.setRating(rvPersons.get(position).rating);
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.task_accepted_fragment_cardview, parent, false);
            return new PersonViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return rvPersons.size();
        }
    }

    class Person {
        String name;
        String picId;
        String taskuuid;
        Float rating;

        Person(String name, String picId, String uuid, Float rating) {
            this.name = name;
            this.picId = picId;
            this.taskuuid = uuid;
            this.rating = rating;
        }
    }

    /**
     * ******************************************************************************************
     * Methods
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskUUID = getArguments().getString(BasedParseUtils.TASK_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.task_accepted_fragment, container, false);

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
    public class CollectTasks extends AsyncTask<String, Void, List<Person>> {
        @Override
        protected List<Person> doInBackground(String... params) {
            List<Person> list = new ArrayList<>();
            List<ParseUser> pUs = BasedParseUtils.getAcceptorsByTaskID(taskUUID);

            for (ParseUser pu : pUs) {
                String picID;
                if (ParseFacebookUtils.isLinked(pu)) picID = "";
                else picID = pu.getString(BasedParseUtils.USER_PIC_ID);

                Person newPerson = new Person(pu.getString(BasedParseUtils.USER_NAME),
                        picID, pu.getObjectId(),
                        BasedParseUtils.getAverageRating(pu.getObjectId()));
                list.add(newPerson);
            }

            return list;
        }

        @Override
        protected void onPostExecute(List<Person> list) {
            persons.clear();
            for (Person pu : list) persons.add(pu);
            rv = (RecyclerView) view.findViewById(R.id.task_accepted_recycler_view);
            rv.setHasFixedSize(true);
            rv.setAdapter(new RVAdapter(persons));
            rv.setLayoutManager(llm);
        }
    }
}
