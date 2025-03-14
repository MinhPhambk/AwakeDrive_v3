package com.example.brainwave.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.brainwave.Interface.SoundManager;
import com.example.brainwave.R;

public class EmailConfirmActivity extends AppCompatActivity {
    private TextView email_confirm;
    private Button loginButton;
    private ConstraintLayout background;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_confirm);
        loginButton = findViewById(R.id.loginButton);
        email_confirm = findViewById(R.id.email_confirm);
        background = findViewById(R.id.background);
        soundManager = SoundManager.getInstance(this);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            background.setBackgroundColor(Color.BLACK);
        } else {
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.gradient_background));
        }
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            email_confirm.setText("Chúng tôi đã gửi email xác nhận đến email của bạn \"" + email + "\" ,vui lòng kiểm tra địa chỉ email (kể cả thư mục spam)");
        }
        loginButton.setOnClickListener(v -> {
            soundManager.playSound();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}