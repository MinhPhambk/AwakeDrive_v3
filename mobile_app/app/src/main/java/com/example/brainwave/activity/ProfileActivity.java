package com.example.brainwave.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.brainwave.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {
    private ImageView avatar;
    private FirebaseAuth firebaseAuth;
    private TextView tv_name, tv_email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initView();
        FirebaseUser user=firebaseAuth.getCurrentUser();
        Glide.with(getApplicationContext()).load(user.getPhotoUrl()).circleCrop().into(avatar);
        tv_name.setText("Tên đầy đủ: "+user.getDisplayName());
        tv_email.setText("Địa chỉ Email: "+user.getEmail());
    }

    private void initView() {
        avatar = findViewById(R.id.avatar);
        firebaseAuth = FirebaseAuth.getInstance();
        tv_name = findViewById(R.id.tv_name);
        tv_email = findViewById(R.id.tv_email);
    }
}