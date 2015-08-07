// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class LoginFragment extends Fragment {

    private ImageButton facebookLoginButton;
    private Button loginButton;
    private Button signUpButton;
    private EditText username;
    private EditText password;
    private LoginFragmentListener mOnLoginFragmentListener;

    private String signInUsername;
    private String signInPassword;

    CallbackManager callbackManager;


    private static final String USER_OBJECT_NAME_FIELD = "name";

    public interface LoginFragmentListener {
        public void onLoginCompleted();
        public boolean isOnLoginActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        callbackManager.onActivityResult(requestCode, resultCode, intent);

    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    //keep this here in case we need to change this
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.login_page_fragment, container, false);


        loginButton = (Button) v.findViewById(R.id.login_page_fragment_login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUsername = username.getText().toString();
                signInPassword = password.getText().toString();

                ParseUser.logInInBackground(signInUsername, signInPassword, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (parseUser != null) {
                            //Yay logged in
                            Log.d("DDIY", "logged in");
                            parseUser.put(BasedParseUtils.USER_PIC_ID, "");
                            parseUser.saveEventually();
                            Intent i = new Intent(getActivity(), ContingentActivity.class);
                            getActivity().finish();
                            startActivity(i);
                        } else {
                            Toast.makeText(getActivity(), "Incorrrect Username or Password", Toast.LENGTH_SHORT).show();
                            Log.e("DDIY", e.toString());
                        }
                    }
                });
            }
        });

        signUpButton = (Button) v.findViewById(R.id.login_page_fragment_signup_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.ddiy_activity_layout, SignUpFragment.newInstance())
                        .addToBackStack(null).commit();
            }
        });

        username = (EditText) v.findViewById(R.id.login_page_fragment_username);
        password = (EditText) v.findViewById(R.id.login_page_fragment_password);

        facebookLoginButton = (ImageButton) v.findViewById(R.id
                .login_page_fragment_facebook_button);
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setUpFBLogin();
            }
        });

        return v;
    }

    private void setUpLogin() {

    }

    private void setUpFBLogin() {

        Collection<String> permissions = new ArrayList<>();
        permissions.add("public_profile");
        permissions.add("email");
        permissions.add("user_education_history");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(
                getActivity(),
                permissions,
                new
                        LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException err) {
                                if (user == null) {
                                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                                } else if (user.isNew()) {
                                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                                    facebookGraphRequest();
                                } else {
                                    Log.d("DDIY", "User logged in through Facebook!");
                                    facebookGraphRequest();
                                }
                            }
                        });
    }

    public void facebookGraphRequest() {
        GraphRequestBatch batch = new GraphRequestBatch(
                GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject jsonObject,
                                    GraphResponse response) {

                                ParseUser parseUser = ParseUser.getCurrentUser();

                                String userUsername = jsonObject.optString("name");
                                String userName = userUsername;
                                String userGender = jsonObject.optString("gender");
                                String userEMail = jsonObject.optString("email");
                                String id = jsonObject.optString("id");
                                JSONArray schoolArray = jsonObject.optJSONArray("education");
                                JSONObject college = schoolArray.optJSONObject(schoolArray.length
                                        () - 1).optJSONObject("school");
                                String schoolName = college.optString("name");
                                parseUser.put(BasedParseUtils.USER_SCHOOL, schoolName);
                                parseUser.put(BasedParseUtils.USER_PIC_ID, id);
                                parseUser.put(BasedParseUtils.USER_USERNAME, userUsername);
                                parseUser.put(BasedParseUtils.USER_EMAIL, userEMail);
                                parseUser.put(BasedParseUtils.USER_GENDER, userGender);
                                parseUser.put(BasedParseUtils.USER_NAME, userName);
                                parseUser.put(BasedParseUtils.USER_CONNECTED, true);
                                parseUser.saveEventually();
                            }
                        }
                )
        );
        batch.addCallback(new GraphRequestBatch.Callback()

                          {
                              @Override
                              public void onBatchCompleted(GraphRequestBatch graphRequests) {
                                  mOnLoginFragmentListener = (LoginFragmentListener) getActivity();
                                  mOnLoginFragmentListener.onLoginCompleted();
                              }
                          }
        );
        batch.executeAsync();
    }
}
