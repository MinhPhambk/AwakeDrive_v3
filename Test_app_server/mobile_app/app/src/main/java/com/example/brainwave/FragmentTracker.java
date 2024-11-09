package com.example.brainwave;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentTracker extends Fragment {

    private static final int REQUEST_CODE = 99;

    CardView measuringCard;
    CardView focusingCard;
    CardView monitoringCard;
    CardView demoCard;
    CardView gameCard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tracker, container, false);

        measuringCard = view.findViewById(R.id.measuringCard);
        focusingCard = view.findViewById(R.id.focusingCard);
        monitoringCard = view.findViewById(R.id.monitoringCard);
        demoCard = view.findViewById(R.id.demoCard);
        gameCard = view.findViewById(R.id.gameCard);

        measuringCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BluetoothAdapterActivity.class);
                startActivity(intent);
            }
        });

        focusingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AttentionActivity.class);
                startActivity(intent);
            }
        });

        monitoringCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ModelsActivity.class);
                startActivity(intent);
            }
        });

        demoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LabeledActivity.class);
                startActivity(intent);
            }
        });

        gameCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UpdateActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}