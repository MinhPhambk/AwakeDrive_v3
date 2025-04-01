package com.awakedrive.brainwave.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.awakedrive.brainwave.Interface.SoundManager;
import com.awakedrive.brainwave.R;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {
    private SoundManager soundManager;
    private SeekBar volumeSeekBar;
    private Toolbar toolbar;
    private Button sound1, sound2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        ActionToolBar();
        sound1.setOnClickListener(v -> {
            soundManager.changeSound(this, R.raw.bt_click);
        });
        sound2.setOnClickListener(v -> {
            soundManager.changeSound(this, R.raw.bt_click2);
        });

        float savedVolume = getSharedPreferences("settings", MODE_PRIVATE).getFloat("button_volume", 1.0f);
        volumeSeekBar.setProgress((int) (savedVolume * 100));

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float newVolume = progress / 100f;
                soundManager.changeVolume(newVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private void initView() {
        soundManager = SoundManager.getInstance(this);
        sound1 = findViewById(R.id.sound1);
        sound2 = findViewById(R.id.sound2);
        toolbar = findViewById(R.id.tool_bar);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
    }

    private void ActionToolBar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Cài đặt");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            soundManager.playSound();
            finish();
        });
    }
}