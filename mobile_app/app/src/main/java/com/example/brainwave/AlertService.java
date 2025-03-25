package com.example.brainwave;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class AlertService extends IntentService {

    public static MediaPlayer player;

    public AlertService() {
        super("AlertService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            Log.e("AlertService", "Intent is null");
            return;
        }

        Bundle b = intent.getBundleExtra("Alert");
        if (b == null) {
            Log.e("AlertService", "Bundle is null");
            return;
        }

        boolean status = b.getBoolean("Status", false);
        Log.d("AlertService", "Status: " + status);

        if (status) {
            playHorn();
        } else {
            stopPlayer();
        }
    }



    public void playHorn() {
        stopPlayer();
        player = new MediaPlayer();
        try {
            String soundUrl = "https://cdn.pixabay.com/audio/2025/03/01/audio_c85ac462e6.mp3";
            player.setDataSource(this, Uri.parse(soundUrl));

            player.setOnPreparedListener(mp -> {
                Log.d("MediaPlayer", "Starting playback...");
                player.start();
            });

            player.setOnErrorListener((mp, what, extra) -> {
                Log.e("MediaPlayer", "Error: " + what + ", " + extra);
                return true; // Ngăn lỗi khác xảy ra
            });

            player.prepareAsync();
        } catch (Exception e) {
            Log.e("MediaPlayer", "Error initializing player", e);
        }
    }



    private void stopPlayer() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
            Log.d("MediaPlayer", "Player released");
        }
    }
}