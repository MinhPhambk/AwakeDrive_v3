package com.awakedrive.brainwave.fragment;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.awakedrive.brainwave.Interface.SoundManager;
import com.awakedrive.brainwave.R;

import java.io.IOException;

public class PlayerFragment extends Fragment {

    private float startY;
    private ImageView img_pause_play, img_player;
    private CardView music_1, music_2, music_3;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private ObjectAnimator diskAnimator;
    private SoundManager soundManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_ntthanh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        img_pause_play = view.findViewById(R.id.img_pause_play);
        img_player = view.findViewById(R.id.img_player);
        music_1 = view.findViewById(R.id.music_1);
        music_2 = view.findViewById(R.id.music_2);
        music_3 = view.findViewById(R.id.music_3);
        soundManager = SoundManager.getInstance(getContext());
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (startY - event.getY() > 200) {
                            new PlayerBottomSheetDialog().show(getChildFragmentManager(), "PlayerBottomSheet");
                        }
                        break;
                }
                return true;
            }
        });

        mediaPlayer = new MediaPlayer();

        initAnimation();
        img_pause_play.setOnClickListener(v -> {
            soundManager.playSound();
            if (isPlaying) {
                pauseMusic();
            } else {
                resumeMusic();
            }
        });

        music_1.setOnClickListener(v -> prepareAndPlayMusic(R.raw.catdoinoisau20hz));
        music_2.setOnClickListener(v -> prepareAndPlayMusic(R.raw.catdoinoisau20hz));
        music_3.setOnClickListener(v -> prepareAndPlayMusic(R.raw.catdoinoisau20hz));

    }

    private void prepareAndPlayMusic(int resId) {
        disableSongSelection();

        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(getContext(), resId);

        if (mediaPlayer != null) {
            enableSongSelection();
            playMusic(resId);
        }
    }


    public void playMusic(int resId) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer = MediaPlayer.create(getContext(), resId);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                isPlaying = true;
                startDiskAnimation();
                img_pause_play.setImageResource(R.drawable.ic_pause);
            }
        }
    }



    public void pauseMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            stopDiskAnimation();
            img_pause_play.setImageResource(R.drawable.ic_play);
        }
    }

    public void resumeMusic() {
        if (mediaPlayer == null) {
            return;
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
            startDiskAnimation();
            img_pause_play.setImageResource(R.drawable.ic_pause);
        }
    }

    private void initAnimation() {
        diskAnimator = ObjectAnimator.ofFloat(img_player, "rotation", 0f, 360f);
        diskAnimator.setDuration(5000); // 5s/vòng
        diskAnimator.setRepeatCount(ValueAnimator.INFINITE);
        diskAnimator.setInterpolator(new LinearInterpolator());
    }

    private void startDiskAnimation() {
        if (diskAnimator == null) {
            initAnimation();
        }
        if (!diskAnimator.isRunning()) {
            diskAnimator.start();
        }
    }

    private void stopDiskAnimation() {
        if (diskAnimator != null) {
            diskAnimator.cancel(); // Dừng và reset animation
            img_player.setRotation(0); // Đặt lại vị trí ban đầu để tránh lỗi xoay bị giật
        }
    }

    private void disableSongSelection() {
        music_1.setEnabled(false);
        music_2.setEnabled(false);
        music_3.setEnabled(false);
    }

    private void enableSongSelection() {
        music_1.setEnabled(true);
        music_2.setEnabled(true);
        music_3.setEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
