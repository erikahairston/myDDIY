// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;


public class TaskFragment extends Fragment {

    private static final int REQUEST_PHOTO = 2;
    public static final String TASK_PHOTO = "Task_Photo";

    /**
     * *******************************************************************************************
     * Bundled Constants
     */
    private static Boolean toAccept;
    private static String taskUUID;


    /**
     * *******************************************************************************************
     * Member Variables
     */
    private Button acceptTaskButton;
    private Button deleteTaskButton;
    private ParseObject task;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private ProfilePictureView profilePic;
    private TextView aboutTextView;
    private TextView categoryTextView;
    private Toolbar toolbar;

    private TextView nameTextView;
    private TextView priceTextView;
    private TextView timeTextView;
    private TextView taskDate;
    private TextView taskTitleTextView;
    private TextView detailTextView;
    private Bitmap bitmapToArray;
    private File mPhotoFile;
    private ParseFile mParsePhoto;

    private boolean photoTaken;

    /**
     * *******************************************************************************************
     * Methods
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Bundle receivedArgs = getArguments();
        taskUUID = receivedArgs.getString(BasedParseUtils.TASK_ID);
        task = BasedParseUtils.getTaskById(taskUUID);
        toAccept = receivedArgs.getBoolean(BasedParseUtils.TO_ACCEPT, true);
        mPhotoFile = getPhotoFile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.task_fragment, container, false);

        getActivity().setTitle("Task Details");

        PackageManager packageManager = getActivity().getPackageManager();


        mPhotoButton = (ImageButton) view.findViewById(R.id.task_camera);

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;

        mPhotoButton.setEnabled(canTakePhoto);
        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            captureImage.putExtra("photo taken", true);
        }

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
        mPhotoView = (ImageView) view.findViewById(R.id.task_fragment_image);
        updatePhotoView();


        acceptTaskButton = (Button) view.findViewById(R.id.accept_task_button);
        acceptTaskButton.setVisibility(View.INVISIBLE);

//        deleteTaskButton = (Button) view.findViewById(R.id.delete_task_button);
        /** deleteTaskButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
        BasedParseUtils.deleteTask(taskUUID);
        FragmentManager fm = getFragmentManager();
        fm.popBackStack();
        }
        }); */

        if (ParseUser.getCurrentUser().getObjectId().equals(BasedParseUtils.getCreator(taskUUID)
                .getObjectId())) {
            acceptTaskButton.setVisibility(View.INVISIBLE);
//            deleteTaskButton.setVisibility(View.VISIBLE);
            mPhotoButton.setVisibility(View.VISIBLE);

        }

        if (toAccept && !BasedParseUtils.checkIfTaskAcccepted(taskUUID))
            acceptTaskButton.setVisibility(View.VISIBLE);

        acceptTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParsePush push = new ParsePush();
                push.setChannel(AddTaskFragment.getTaskChannel(taskUUID.toString()));
                push.setMessage("Someone has accepted your task!");
                push.sendInBackground();

                ParsePush.subscribeInBackground(getDoerChannel(taskUUID.toString()), new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        //Log.e("TAG", "Subscribed doer to parse");
                    }
                });

                BasedParseUtils.putAccepter(ParseUser.getCurrentUser(), taskUUID);

                Fragment fragment = new CategoryFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                        .addToBackStack(null).commit();
            }
        });

        //      toolbar = (Toolbar) view.findViewById(R.id.my_task_frag_toolbar);
        //    ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        // ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled
        //       (true);


        // ((AppCompatActivity)getActivity()).getSupportActionBar().setBackgroundDrawable();


        taskDate = (TextView) view.findViewById(R.id.task_fragment_date);
        taskDate.setText(task.getString(BasedParseUtils.TASK_DATE));

        nameTextView = (TextView) view.findViewById(R.id.task_fragment_name);
        nameTextView.setText(BasedParseUtils.getCreator(taskUUID)
                .getString(BasedParseUtils.USER_NAME));

        priceTextView = (TextView) view.findViewById(R.id.price_task_detail);
        priceTextView.setText(task.getString(BasedParseUtils.TASK_PRICE));

        taskTitleTextView = (TextView) view.findViewById(R.id.task_fragment_task_title);
        taskTitleTextView.setText(task.getString(BasedParseUtils.TASK_TITLE));


        detailTextView = (TextView) view.findViewById(R.id.task_fragment_detail);
        detailTextView.setText(task.getString(BasedParseUtils.TASK_DETAILS));

        timeTextView = (TextView) view.findViewById(R.id.task_fragment_time);
        timeTextView.setText(task.getString(BasedParseUtils.TASK_TIME));

        return view;
    }

    public static String getDoerChannel(String taskUUID) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("task_doer_");
        stringBuilder.append(taskUUID);
        String taskDoerChannel = stringBuilder.toString();
        return taskDoerChannel;
    }

    private String formDateTime() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(task.getString(BasedParseUtils.TASK_DATE));
        stringBuilder.append(" ");
        stringBuilder.append(task.getString(BasedParseUtils.TASK_TIME));

        String dateTime = stringBuilder.toString();

        return dateTime;
    }

    public String getPhotoFilename() {
        return "IMG_" + taskUUID.toString() + ".jpg";
    }

    public File getPhotoFile() {
        File externalFilesDir = getActivity()
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFilesDir == null) {
            return null;
        }
        return new File(externalFilesDir, getPhotoFilename());
    }

    private void updatePhotoView() {

        if (photoTaken) {
            bitmapToArray = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            saveScaledPhoto();
        }
        mPhotoView.setImageBitmap(BasedParseUtils.getPhotoFile(taskUUID));


        //TODO: figure out how to rotate it so it stays up and down when taken vertically
         /*      if (bitmap.getWidth() < bitmap.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedScaledyTaskImage = Bitmap.createBitmap(PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity()), 0,
                    0, bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true);

                mPhotoView.setImageBitmap(rotatedScaledyTaskImage);
            } else {*/


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PHOTO) {
            photoTaken = getActivity().getIntent().getBooleanExtra("photo taken", true);
            updatePhotoView();
        }
    }

    private void saveScaledPhoto() {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmapToArray.compress(Bitmap.CompressFormat.JPEG, 20, bos);

        byte[] scaledData = bos.toByteArray();

        mParsePhoto = new ParseFile(BasedParseUtils.TASK_PHOTO, scaledData);
        mParsePhoto.saveInBackground(new SaveCallback() {

            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(),
                            "Error saving: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                    BasedParseUtils.setPhotoFile(mParsePhoto, taskUUID);
                    mPhotoView.setImageBitmap(BasedParseUtils.getPhotoFile(taskUUID));

                }
            }
        });

    }


}
