package com.example.brainwave;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;

public class FragmentHome extends Fragment {

    public FragmentHome() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        TextView dateNow = (TextView)view.findViewById(R.id.date_now);

        Date date = (Date) Calendar.getInstance().getTime();
        String day = (String) DateFormat.format("dd", date);
        String monthString = (String) DateFormat.format("MMM", date);
        String year = (String) DateFormat.format("yyyy", date);

        String dt = monthString + " " + day + ", " + year;
        dateNow.setText(dt);

        ImageView btnTrack = (ImageView) view.findViewById(R.id.track_btn);
        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) getActivity()).loadFragment(new FragmentTracker(), false);
                ((HomeActivity) getActivity()).bottomNavigationView.setSelectedItemId(R.id.bot_tracker);
            }
        });

        return view;
    }
}