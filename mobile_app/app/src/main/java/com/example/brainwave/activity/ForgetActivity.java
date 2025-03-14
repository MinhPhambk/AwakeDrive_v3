package com.example.brainwave.activity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.brainwave.Interface.SoundManager;
import com.example.brainwave.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText emailInput;
    private Button forgot_button;
    private ConstraintLayout background;
    private SoundManager soundManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        emailInput =findViewById(R.id.emailInput);
        forgot_button = findViewById(R.id.forgot_button);
        firebaseAuth = FirebaseAuth.getInstance();
        background = findViewById(R.id.background);
        soundManager = SoundManager.getInstance(this);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            background.setBackgroundColor(Color.BLACK);
        } else {
            background.setBackground(ContextCompat.getDrawable(this,R.drawable.gradient_background));
        }
        forgot_button.setOnClickListener(v -> {
            soundManager.playSound();
            firebaseAuth.sendPasswordResetEmail(emailInput.getText().toString()).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Toast.makeText(this, "Email đã được gửi", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}