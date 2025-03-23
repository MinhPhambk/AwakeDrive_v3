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
        Bundle b = intent.getBundleExtra("Alert");
        boolean status = b.getBoolean("Status");
        if (status) {
            playHorn();
            Log.d("TAG", "Turn on alert! ");
        } else {
            stopPlayer();
        }
    }

    public void playHorn() {
        stopPlayer();
        player = new MediaPlayer();
        try {
            player.setDataSource(this, Uri.parse("https://cdn.pixabay.com/audio/2025/03/01/audio_c85ac462e6.mp"));
            player.setOnPreparedListener(mp -> {
                player.start();
                Log.d("MediaPlayer", "Playing alert sound...");
            });

            player.setOnCompletionListener(mp -> {
                mp.seekTo(0); // Quay lại đầu và phát lại
                player.start();
                Log.d("MediaPlayer", "Replay alert sound...");
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