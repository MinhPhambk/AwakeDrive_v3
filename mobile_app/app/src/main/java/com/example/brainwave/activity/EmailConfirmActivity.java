package com.example.brainwave.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.brainwave.R;

public class EmailConfirmActivity extends AppCompatActivity {
    private TextView email_confirm;
    private Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_confirm);
        loginButton=findViewById(R.id.loginButton);
        email_confirm=findViewById(R.id.email_confirm);
        String email=getIntent().getStringExtra("email");
        if(email!=null){
            email_confirm.setText("Chúng tôi đã gửi email xác nhận đến email của bạn \""+email+"\" ,vui lòng kiểm tra địa chỉ email (kể cả thư mục spam)");
        }
        loginButton.setOnClickListener(v -> {
            Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}