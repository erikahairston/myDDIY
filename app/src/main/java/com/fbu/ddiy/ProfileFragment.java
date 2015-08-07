// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseUser;

public class ProfileFragment extends Fragment {
    /**
     * ******************************************************************************************
     * Bundled Constants
     */
    private static String taskUUID;
    private static String userUUID;

    /**
     * ******************************************************************************************
     * Member Variables
     */
    private boolean rnJesus = false;
    private ParseUser currentUser;
    private ProfilePictureView profileImage;
    private TextView aboutMeTextView;
    private TextView collegeTextView;

    private Button acceptPersonButton;
    private Button editProfileButton;
    private TextView emailTextView;
    private RatingBar ratingBar;

    /**
     * ******************************************************************************************
     * Methods
     */
    public EditProfileFragment createEditProfileFragment() {
        return EditProfileFragment.newInstance();
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userUUID = getArguments().getString(BasedParseUtils.USER_ID);
            taskUUID = getArguments().getString(BasedParseUtils.TASK_ID);
            rnJesus = getArguments().getBoolean(BasedParseUtils.TO_ACCEPT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        profileImage = (ProfilePictureView) view.findViewById(R.id.profile_image);
        collegeTextView = (TextView) view.findViewById(R.id.college_text_view);
        emailTextView = (TextView) view.findViewById(R.id.profile_fragment_email);
        ratingBar = (RatingBar) view.findViewById(R.id.average_rating_show_text);
        aboutMeTextView = (TextView) view.findViewById(R.id.about_me_show_text);
        editProfileButton = (Button) view.findViewById(R.id.edit_profile_button);
        acceptPersonButton = (Button) view.findViewById(R.id.accept_person_button);
        new GetRating().execute();

        return view;
    }

    /**
     * ******************************************************************************************
     * Helper Methods
     */
    public class GetRating extends AsyncTask<String, Void, Float> {
        @Override
        protected Float doInBackground(String... params) {
            if (getArguments() == null) currentUser = ParseUser.getCurrentUser();
            else currentUser = BasedParseUtils.getUserById(userUUID);

            return BasedParseUtils.getAverageRating(userUUID);
        }

        @Override
        protected void onPostExecute(Float num) {
            getActivity().setTitle(currentUser.getString(BasedParseUtils.USER_NAME));

            profileImage.setProfileId(currentUser.getString("id"));
            collegeTextView.setText(currentUser.getString(BasedParseUtils.USER_SCHOOL));
            ratingBar.setRating(num);
            aboutMeTextView.setText(currentUser.getString(BasedParseUtils.USER_ABOUT_ME));

            collegeTextView.setText(currentUser.getString(BasedParseUtils.USER_SCHOOL));
            emailTextView.setText(currentUser.getString(BasedParseUtils.USER_EMAIL));

            editProfileButton.setVisibility(View.INVISIBLE);
            editProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, createEditProfileFragment())
                            .addToBackStack(null).commit();
                }
            });

            acceptPersonButton.setVisibility(View.INVISIBLE);
            if (rnJesus) {
                acceptPersonButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BasedParseUtils.changeTaskToAccepted(taskUUID, userUUID);

                        Bundle bundle = new Bundle();
                        bundle.putString(BasedParseUtils.TASK_ID, taskUUID);

                        Fragment fragment = new ContactFragment();
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                                .commit();
                    }
                });
            }

            if (getArguments() == null) editProfileButton.setVisibility(View.VISIBLE);
            else if (rnJesus) acceptPersonButton.setVisibility(View.VISIBLE);
        }
    }
}
