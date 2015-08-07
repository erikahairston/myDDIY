// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.List;

public class CategoryFragment extends Fragment {
    /**
     * *******************************************************************************************
     * Member Variables
     */
    LinearLayout categoryFragmentLinearLayout;

    /**
     * *******************************************************************************************
     * Methods
     */
    public static CategoryFragment newInstance() {
        return new CategoryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_fragment, container, false);
        getActivity().setTitle("Categories");
        categoryFragmentLinearLayout = (LinearLayout) view.findViewById(R.id.button_holder);

        new GetCategories().execute();

        return view;
    }

    /**
     * ******************************************************************************************
     * Helper Classes
     */
    public class GetCategories extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... params) {
            return BasedParseUtils.getAllCategories();
        }

        protected void onPostExecute(List<String> categories) {
            for (String cat : categories) {
                if (getActivity() != null) {
                    Button button = new Button(getActivity());
                    final String genesis = cat;
                    LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams
                            (LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);

                    lllp.setMargins(0, 1, 0, 0);
                    button.setLayoutParams(lllp);
                    button.setText(cat);
                    button.setHeight(367);
                    button.setTextColor(Color.BLACK);
                    button.setTextSize(20);

                    button.setBackgroundResource(R.drawable.cat_button_selector);

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle myBundle = new Bundle();
                            myBundle.putString(BasedParseUtils.CAT_NAME, genesis);

                            Fragment fragment = new ListFragment();
                            fragment.setArguments(myBundle);
                            FragmentManager fragmentManager = getFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                                    .addToBackStack(null).commit();
                        }
                    });

                    categoryFragmentLinearLayout.addView(button);
                }
            }
        }
    }
}
