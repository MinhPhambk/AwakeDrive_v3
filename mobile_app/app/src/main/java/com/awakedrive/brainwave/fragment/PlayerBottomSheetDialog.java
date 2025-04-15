package com.awakedrive.brainwave.fragment;

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

import com.awakedrive.brainwave.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PlayerBottomSheetDialog extends BottomSheetDialogFragment {
    private LinearLayout music_1, music_2;
    private ImageView bnt;
    private TextView music_time, music_time_2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music2_ntthanh, container, false);
    }
    @Override
    public void onStart() {
        super.onStart();

        View view = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);

        // Ánh xạ các view
        music_1 = getDialog().findViewById(R.id.music_1);
        music_2 = getDialog().findViewById(R.id.music_2);
        bnt = getDialog().findViewById(R.id.bnt);
        music_time = getDialog().findViewById(R.id.music_time);
        music_time_2 = getDialog().findViewById(R.id.music_time_2);

        prepareMusic(music_time, R.raw.catdoinoisau20hz, music_1);
        prepareMusic(music_time_2, R.raw.isochronic_tones_alert, music_2);



        // Xử lý sự kiện click chọn nhạc
        music_1.setOnClickListener(v -> selectSong(R.raw.catdoinoisau20hz));
        music_2.setOnClickListener(v -> selectSong(R.raw.isochronic_tones_alert));

        // Mở rộng BottomSheet
        if (getView() != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            view.setLayoutParams(layoutParams);

            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(view);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void prepareMusic(TextView timeView, int resId, LinearLayout musicButton) {
        MediaPlayer player = MediaPlayer.create(getContext(), resId);

        if (player != null) {
            timeView.setText(formatTime(player.getDuration()));
            player.release();
            musicButton.setEnabled(true);
            musicButton.setAlpha(1.0f);
        } else {
            musicButton.setEnabled(false);
            musicButton.setAlpha(0.5f);
        }
    }



    private void selectSong(int resId) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof PlayerFragment) {
            ((PlayerFragment) parentFragment).playMusic(resId);
        }
        dismiss();
    }


    private String formatTime(int millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

}
