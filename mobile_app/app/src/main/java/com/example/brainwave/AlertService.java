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

    public void playHorn(){
        stopPlayer();
        player = MediaPlayer.create(this, Uri.parse("https://cdn.pixabay.com/audio/2025/03/01/audio_c85ac462e6.mp"));
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                playMusic();
                player.start();
            }
        });
        player.start();
    }

    private void stopPlayer() {
        if (player != null) {
            player.release();
            player = null;
//            Toast.makeText(this, "MediaPlayer released", Toast.LENGTH_SHORT).show();
        }
    }


}