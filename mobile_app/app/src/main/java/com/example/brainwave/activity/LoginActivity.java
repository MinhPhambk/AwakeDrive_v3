package com.example.brainwave.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.brainwave.R;

public class LoginActivity extends AppCompatActivity {
    private TextView registerText, forgotPassword;
    private Button loginButton;
    private EditText email_login, password_login;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        onclick_login();
        onclick_forgot();
        onclick_register();
        ontouch_pass();
    }

    private void onclick_register() {
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void onclick_forgot() {
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ForgetActivity.class);
            startActivity(intent);
        });
    }

    private void onclick_login() {
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

    }


    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    private void ontouch_pass() {
        password_login.setOnTouchListener((v, event) -> {
            togglePasswordVisibility(event, password_login);
            return false;
        });
    }

    private void togglePasswordVisibility(MotionEvent event, EditText pass) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int drawableEnd = 2; // Position of drawableEnd
            if (event.getRawX() >= (pass.getRight() - pass.getCompoundDrawables()[drawableEnd].getBounds().width())) {
                isPasswordVisible = !isPasswordVisible; // Toggle state
                Typeface currentTypeface = pass.getTypeface();
                pass.setInputType(isPasswordVisible
                        ? android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        : android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

                pass.setTypeface(currentTypeface);

                // Update drawable icon
                pass.setCompoundDrawablesWithIntrinsicBounds(
                        null, null,
                        isPasswordVisible ? getResources().getDrawable(R.drawable.ic_eye) :
                                getResources().getDrawable(R.drawable.ic_eye_close),
                        null
                );

                // Move cursor to the end
                pass.setSelection(pass.getText().length());
            }
        }
    }

    private void initView() {
        registerText = findViewById(R.id.registerText);
        forgotPassword = findViewById(R.id.forgotPassword);
        loginButton = findViewById(R.id.loginButton);
        email_login = findViewById(R.id.email_login);
        password_login = findViewById(R.id.password_login);
    }
}