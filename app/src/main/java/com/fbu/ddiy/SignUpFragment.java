// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpFragment extends Fragment {
    /**********************************************************************************************
     * Member Variables
     */
    private EditText username;
    private EditText password;
    private EditText confirmPassword;
    private EditText email;
    private EditText name;
    private EditText school;

    /**********************************************************************************************
     * Methods
     */
    public static SignUpFragment newInstance() { return new SignUpFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_signup_fragment, container, false);

        username = (EditText) view.findViewById(R.id.signup_username_input);
        password = (EditText) view.findViewById(R.id.signup_password_input);
        confirmPassword = (EditText) view.findViewById(R.id.signup_confirm_password_input);
        email = (EditText) view.findViewById(R.id.signup_email_input);
        name = (EditText) view.findViewById(R.id.signup_name_input);
        school = (EditText) view.findViewById(R.id.signup_school_input);

        Button createAccount = (Button) view.findViewById(R.id.create_account);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        return view;
    }

    private void createAccount() {
        //TODO:make sure all fields are filled
        //TODO:check if username exists
        //TODO:check if confirmPassword matches original password
        //TODO:username cannot be missing or blank
        //TODO: must be valid email address (check if email has been taken)
        String mUsername = username.getText().toString();
        String mPassword = password.getText().toString();
        String mConfirmPassword = confirmPassword.getText().toString();
        String mEmail = email.getText().toString();
        String mName = name.getText().toString();
        String mSchool = school.getText().toString();

        ParseUser newUser = new ParseUser();
        newUser.setUsername(mUsername);
        newUser.setPassword(mPassword);
        newUser.setEmail(mEmail);
        newUser.put(BasedParseUtils.USER_NAME, mName);
        newUser.put(BasedParseUtils.USER_SCHOOL, mSchool);
        newUser.put(BasedParseUtils.USER_CONNECTED, false);
        newUser.put(BasedParseUtils.USER_PIC_ID, "");
        newUser.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    //Go back to login fragment
                    Toast.makeText(getActivity(), "Signed in supposedly", Toast.LENGTH_SHORT)
                            .show();
                    Log.d("DDIY", "sign up worked");
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack();
                } else {
                    Log.d("DDIY", "some error occurred");
                    Log.e("DDIY", e.toString());
                }
            }
        });
    }
}
