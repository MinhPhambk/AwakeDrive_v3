package com.example.brainwave.Interface;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.example.brainwave.R;

import java.io.IOException;

public class SoundManager {
    private static SoundManager instance;
    private SoundPool soundPool;
    private int soundID;
    private SharedPreferences preferences;
    private float volumeLevel = 1.0f;

    private SoundManager(Context context) {
        preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        volumeLevel = preferences.getFloat("button_volume", 1.0f);
        loadSound(context);
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    private void loadSound(Context context) {
        // Lấy giá trị ID của âm thanh từ SharedPreferences
        int soundResId = preferences.getInt("button_sound", R.raw.bt_click2);

        // Kiểm tra xem tài nguyên có tồn tại không
        try {
            context.getResources().openRawResourceFd(soundResId).close();
            soundID = soundPool.load(context, soundResId, 1);
        } catch (Resources.NotFoundException e) {
            // Nếu không tìm thấy âm thanh, đặt về giá trị mặc định
            soundResId = R.raw.bt_click2;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("button_sound", soundResId);
            editor.apply();
            soundID = soundPool.load(context, soundResId, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void playSound() {
        if (soundPool != null) {
            soundPool.play(soundID, volumeLevel, volumeLevel, 0, 0, 1);
        }
    }

    public void changeSound(Context context, int newSoundResId) {
        try {
            context.getResources().openRawResourceFd(newSoundResId).close();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("button_sound", newSoundResId);
            editor.apply();

            // Giải phóng SoundPool cũ và tải âm thanh mới
            soundPool.release();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .build();

            soundID = soundPool.load(context, newSoundResId, 1);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void changeVolume(float newVolume) {
        volumeLevel = newVolume;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("button_volume", newVolume);
        editor.apply();
    }
}
