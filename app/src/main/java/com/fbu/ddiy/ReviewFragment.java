// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class ReviewFragment extends Fragment {
    /**
     * *******************************************************************************************
     * Bundled Constants
     */
    private static String taskUUID;

    /**********************************************************************************************
     * Member Variables
     */
    Button setRatingButton;

    TextView reviewTextView;
    RatingBar taskRating;
    TextView taskTitle;
    TextView taskDetails;
    TextView taskDate;
    TextView taskCost;
    TextView taskratingText;

    /**********************************************************************************************
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
        View view = inflater.inflate(R.layout.review_fragment, container, false);

        ParseUser pu = BasedParseUtils.getDoer(taskUUID);
        ParseObject po = BasedParseUtils.getTaskById(taskUUID);

        try { pu.fetchIfNeeded(); }
        catch (ParseException e) { e.printStackTrace(); }


        reviewTextView = (TextView) view.findViewById(R.id.review_fragment_text);
        reviewTextView.setText(pu.getString(BasedParseUtils.USER_NAME) + " did your task " +
                "on: " + po.getString(BasedParseUtils.TASK_DATE)  );

        /**  taskRating = (Spinner) view.findViewById(R.id.rating_spinner);
         ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
         .createFromResource(getActivity(), R.array.ratings_array,
         android.R.layout.simple_spinner_dropdown_item);
         taskRating.setAdapter(spinnerAdapter); */

        taskRating = (RatingBar) view.findViewById(R.id.review_fragment_rating_bar);

        setRatingButton = (Button) view.findViewById(R.id.review_fragment_button);
        setRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: will this even work?
                //  int rating = 5 - taskRating.getSelectedItemPosition();
                //  BasedParseUtils.putReview(taskUUID, rating);
                float rating = taskRating.getRating();
                BasedParseUtils.putReview(taskUUID, rating);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        taskTitle = (TextView) view.findViewById(R.id.review_fragment_task_name);
        taskTitle.setText(po.getString(BasedParseUtils.TASK_TITLE));

        taskDetails = (TextView) view.findViewById(R.id.review_fragment_task_details);
        taskDetails.setText("($" + po.getString(BasedParseUtils.TASK_PRICE) + ") " + po.getString(BasedParseUtils.TASK_DETAILS));

        taskratingText = (TextView) view.findViewById(R.id.review_fragment_text2);
        taskratingText.setText("Give " + pu.getString(BasedParseUtils.USER_NAME) + " a rating:");


        return view;
    }
}
