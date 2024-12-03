package com.example.brainwave;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentMusic#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentMusic extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public static int count_playbacks = 0;

    public static final int PLAYBACKS = 15;
    public static MediaPlayer player;

    ImageView play;

    public FragmentMusic() {
        // Required empty public constructor
    }

    public static FragmentMusic newInstance(String param1, String param2) {
        FragmentMusic fragment = new FragmentMusic();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        super.onCreateView(inflater, container, savedInstanceState);
        play = view.findViewById(R.id.playmusic);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count_playbacks = 0;
                if (player == null) {
                    player = MediaPlayer.create(getActivity(), R.raw.test_delta_waves_4hz_3min_isochronic);
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if(count_playbacks >= PLAYBACKS) {
                                player.release();
                                player = null;
                                count_playbacks = 0;
                            } else {
                                count_playbacks++;
                                player.start();
                            }
                        }
                    });
                }

                if (player.isPlaying()){
                    player.pause();
                }
                else {
                    player.start();
                }
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}