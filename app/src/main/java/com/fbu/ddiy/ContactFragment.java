// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseUser;

public class ContactFragment extends Fragment {
    /**
     * *******************************************************************************************
     * Bundled Constants
     */
    private static String taskUUID;

    /**
     * *******************************************************************************************
     * Member Variables
     */
    Button callButton;
    Button doneButton;
    Button textButton;
    TextView bzzbzzTextView;
    TextView contactTextView;
    ProfilePictureView personToContact;


    View view;
    ParseUser doneBy;
    String name;
    String number;

    /**
     * *******************************************************************************************
     * Methods
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle receivedBundle = getArguments();
        taskUUID = receivedBundle.getString(BasedParseUtils.TASK_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contact_fragment, container, false);

        getNameAndNumber();

        bzzbzzTextView = (TextView) view.findViewById(R.id.you_are_matched_text_view);
        bzzbzzTextView.setText("BzzBzz, You've Got A Match");

        contactTextView = (TextView) view.findViewById(R.id.contact_fragment_text);
        contactTextView.setText("Contact " + name);

        personToContact = (ProfilePictureView) view.findViewById(R.id.contact_profile_pic);
        personToContact.setProfileId(doneBy.getString(BasedParseUtils.USER_PIC_ID));

        callButton = (Button) view.findViewById(R.id.call_button);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePhoneCall();
            }
        });

        doneButton = (Button) view.findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new CategoryFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                        .commit();
            }
        });

        textButton = (Button) view.findViewById(R.id.text_button);
        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage();
            }
        });

        contactTextView = (TextView) view.findViewById(R.id.contact_fragment_text);
        contactTextView.setText("Contact " + name);

        return view;
    }

    /**
     * *******************************************************************************************
     * Helper Methods
     */
    public void initiatePhoneCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivity(intent);
    }

    public void getNameAndNumber() {
        doneBy = BasedParseUtils.getDoer(taskUUID);
        name = doneBy.getString(BasedParseUtils.USER_NAME);
        number = doneBy.getString(BasedParseUtils.USER_PHONE_NUMBER);
    }

    public void sendTextMessage() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + number));
        intent.putExtra("sms_body", "Thanks for accepting my task, " +
                BasedParseUtils.getTaskTitle(taskUUID) + "! My plan is...");
        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivity(intent);
    }
}
