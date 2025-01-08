package com.example.brainwave.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.brainwave.R;


public class RegisterActivity extends AppCompatActivity {
    private EditText edt_password, edt_confirm_password;
    private TextView tv_already_have_account;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        ontouch_pass();
        ontouch_confirm_pass();
        onlick_already_have_account();
    }

    private void onlick_already_have_account() {
        tv_already_have_account.setOnClickListener(v -> {
            Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ontouch_confirm_pass() {
        edt_password.setOnTouchListener((v, event) -> {
            togglePasswordVisibility(event, edt_password);
            return false; // Not handled
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ontouch_pass() {
        edt_confirm_password.setOnTouchListener((v, event) -> {
            togglePasswordVisibility(event, edt_confirm_password);
            return false; // Not handled
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
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
        edt_password = findViewById(R.id.edt_password);
        edt_confirm_password = findViewById(R.id.edt_confirm_password);
        tv_already_have_account=findViewById(R.id.tv_already_have_account);
    }
}