// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseUser;


/**
 *
 */
public class EditProfileFragment extends Fragment {
    /**********************************************************************************************
     * Member Variables
     */
    private EditText aboutMeEditText;
    private EditText emailEditText;
    private EditText phoneNumberEditText;
    private EditText usernameEditText;
    private EditText schoolEditText;
    private EditText nameEditText;
    private ParseUser currentUser;

    /**********************************************************************************************
     * Methods
     */

    public static EditProfileFragment newInstance() {
        return new EditProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_fragment, container, false);

        nameEditText = (EditText) view.findViewById(R.id.name_text_view_edit);
        usernameEditText = (EditText) view.findViewById(R.id.username_text_view_edit);
        emailEditText = (EditText) view.findViewById(R.id.email_edit_text);
        schoolEditText = (EditText) view.findViewById(R.id.college_text_view_edit);
        aboutMeEditText = (EditText) view.findViewById(R.id.about_me_edit_text);
        phoneNumberEditText = (EditText) view.findViewById(R.id.phone_number_edit_text);
        Button saveButton = (Button) view.findViewById(R.id.edit_profile_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { new SaveProfile().execute(); }
        });

        new UpdateEditProfile().execute();
        return view;
    }

    /**********************************************************************************************
     * Helper Classes
     */
    public class SaveProfile extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String saveAboutMe = aboutMeEditText.getText().toString();
            String saveUsername = usernameEditText.getText().toString();
            String saveSchool = schoolEditText.getText().toString();
            String savePhoneNumber = phoneNumberEditText.getText().toString();
            String saveEmail = emailEditText.getText().toString();
            String saveName = nameEditText.getText().toString();

            BasedParseUtils.saveUserDataField(currentUser,
                    BasedParseUtils.USER_ABOUT_ME, saveAboutMe);
            BasedParseUtils.saveUserDataField(currentUser,
                    BasedParseUtils.USER_USERNAME, saveUsername);
            BasedParseUtils.saveUserDataField(currentUser, BasedParseUtils.USER_SCHOOL, saveSchool);
            BasedParseUtils.saveUserDataField(currentUser,
                    BasedParseUtils.USER_PHONE_NUMBER, savePhoneNumber);
            BasedParseUtils.saveUserDataField(currentUser, BasedParseUtils.USER_EMAIL, saveEmail);
            BasedParseUtils.saveUserDataField(currentUser, BasedParseUtils.USER_NAME, saveName);

            return "";
        }

        @Override
        protected void onPostExecute(String string) {
            if (ParseApplication.getIsLoginActivityIsRunning()) {
                ParseApplication.setIsLoginActivityRunning(false);
                Intent i = new Intent(getActivity(), ContingentActivity.class);
                getActivity().finish();
                startActivity(i);
            } else {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
                fm.beginTransaction().replace(R.id.content_frame,
                        ProfileFragment.newInstance()).commit();
            }
        }
    }


    public class UpdateEditProfile extends AsyncTask<String, Void, ParseUser> {
        @Override
        protected ParseUser doInBackground(String... params) {
            return (currentUser = ParseUser.getCurrentUser());
        }

        @Override
        protected void onPostExecute(ParseUser parseUser) {
            nameEditText.setText(parseUser.getString(BasedParseUtils.USER_NAME));
            usernameEditText.setText(parseUser.getString(BasedParseUtils.USER_USERNAME));
            emailEditText.setText(parseUser.getString(BasedParseUtils.USER_EMAIL));
            schoolEditText.setText(parseUser.getString(BasedParseUtils.USER_SCHOOL));
            aboutMeEditText.setText(parseUser.getString(BasedParseUtils.USER_ABOUT_ME));
            phoneNumberEditText.setText(parseUser.getString(BasedParseUtils.USER_PHONE_NUMBER));
        }
    }
}
