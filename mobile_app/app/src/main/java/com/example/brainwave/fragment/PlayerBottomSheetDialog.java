package com.example.brainwave.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.brainwave.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PlayerBottomSheetDialog extends BottomSheetDialogFragment {
    private LinearLayout music_1;
    private MediaPlayer mediaPlayer;
    private ImageView bnt;
    private TextView music_time;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music2_ntthanh, container, false);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Lấy BottomSheet
        View view = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);

        music_1 = getDialog().findViewById(R.id.music_1);
        bnt = getDialog().findViewById(R.id.bnt);
        music_time = getDialog().findViewById(R.id.music_time);
        mediaPlayer=new MediaPlayer();
        try {
            mediaPlayer.setDataSource("https://s4-media1.study4.com/media/tez_media/sound/eco_toeic_1000_test_1_1.mp3");
            mediaPlayer.prepare();
            music_time.setText(formatTime(mediaPlayer.getDuration()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        music_1.setOnClickListener(v -> {
            selectSong("https://s4-media1.study4.com/media/tez_media/sound/eco_toeic_1000_test_1_1.mp3");
        });
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            view.setLayoutParams(layoutParams);

            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(view);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
    private void selectSong(String songUrl) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof PlayerFragment) {
            ((PlayerFragment) parentFragment).playMusic(songUrl);
        }
        dismiss();
    }
    private String formatTime(int millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }
}
