package com.example.brainwave.fragment;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.brainwave.R;

import java.io.IOException;

public class PlayerFragment extends Fragment {

    private float startY;
    private ImageView img_pause_play, img_player;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private ObjectAnimator diskAnimator;

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
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endY = event.getY();
                        if (startY - endY > 200) {
                            // Hiển thị BottomSheetDialogFragment
                            PlayerBottomSheetDialog bottomSheetDialog = new PlayerBottomSheetDialog();
                            bottomSheetDialog.show(getChildFragmentManager(), bottomSheetDialog.getTag());
                        }
                        break;
                }
                return true;
            }
        });
        mediaPlayer = new MediaPlayer();

        initAnimation();
        img_pause_play.setOnClickListener(v -> {
            if (isPlaying) {
                pauseMusic();
            } else {
                resumeMusic();
            }
        });
    }

    public void playMusic(String songUrl) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            startDiskAnimation();
            img_pause_play.setImageResource(R.drawable.ic_pause);
        } catch (IOException e) {
            e.printStackTrace();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
