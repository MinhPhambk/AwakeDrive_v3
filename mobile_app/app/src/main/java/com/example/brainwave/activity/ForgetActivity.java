package com.example.brainwave.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.brainwave.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText emailInput;
    private Button forgot_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        emailInput =findViewById(R.id.emailInput);
        forgot_button = findViewById(R.id.forgot_button);
        firebaseAuth = FirebaseAuth.getInstance();
        forgot_button.setOnClickListener(v -> {
            firebaseAuth.sendPasswordResetEmail(emailInput.getText().toString()).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Toast.makeText(this, "Email đã được gửi", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}