package com.awakedrive.brainwave.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.awakedrive.brainwave.Interface.SoundManager;
import com.awakedrive.brainwave.R;

import java.util.Objects;

public class DetailActivity extends AppCompatActivity {

    private TextView deltaText, thetaText, alphaText, betaText, gammaText;
    private Toolbar toolbar;
    private SoundManager soundManager;

    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.awakedrive.brainwave.UPDATE_DATA".equals(intent.getAction())) {
                int delta = intent.getIntExtra("delta", 0);
                int theta = intent.getIntExtra("theta", 0);
                int lowAlpha = intent.getIntExtra("lowAlpha", 0);
                int highAlpha = intent.getIntExtra("highAlpha", 0);
                int lowBeta = intent.getIntExtra("lowBeta", 0);
                int highBeta = intent.getIntExtra("highBeta", 0);
                int lowGamma = intent.getIntExtra("lowGamma", 0);
                int middleGamma = intent.getIntExtra("middleGamma", 0);

                deltaText.setText("Delta: " + delta);
                thetaText.setText("Theta: " + theta);
                alphaText.setText("Alpha: " + lowAlpha + " - " + highAlpha);
                betaText.setText("Beta: " + lowBeta + " - " + highBeta);
                gammaText.setText("Gamma: " + lowGamma + " - " + middleGamma);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        initView();
        ActionToolBar();
    }

    private void initView() {
        deltaText = findViewById(R.id.deltaText);
        thetaText = findViewById(R.id.thetaText);
        alphaText = findViewById(R.id.alphaText);
        betaText = findViewById(R.id.betaText);
        gammaText = findViewById(R.id.gammaText);
        toolbar = findViewById(R.id.tool_bar);
        soundManager = SoundManager.getInstance(this);
    }

    private void ActionToolBar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Xem thêm");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            soundManager.playSound();
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(dataReceiver, new IntentFilter("com.awakedrive.brainwave.UPDATE_DATA"));
    }


    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataReceiver);
    }

}
