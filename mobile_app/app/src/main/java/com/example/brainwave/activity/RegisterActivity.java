package com.example.brainwave.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.brainwave.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class RegisterActivity extends AppCompatActivity {
    private EditText edt_password, edt_confirm_password, edt_last_name, edt_first_name, edt_email;
    private TextView tv_already_have_account;
    private boolean isPasswordVisible = false;
    private Button btn_register;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        ontouch_pass();
        ontouch_confirm_pass();
        onlick_already_have_account();
        btn_register.setOnClickListener(v -> signUp());
    }

    private void signUp() {
        firebaseAuth = FirebaseAuth.getInstance();
        String first_name = edt_first_name.getText().toString();
        String last_name = edt_last_name.getText().toString();
        String email = edt_email.getText().toString();
        String pass = edt_password.getText().toString();
        String re_pass = edt_confirm_password.getText().toString();
        if(!TextUtils.isEmpty(first_name) && !TextUtils.isEmpty(last_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(re_pass)){
            if(pass.equals(re_pass)){
                firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verificationTask -> {
                                            if (verificationTask.isSuccessful()) {
                                                Intent intent=new Intent(getApplicationContext(),EmailConfirmActivity.class);
                                                intent.putExtra("email",email);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Lỗi khi gửi email xác nhận!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(first_name+" "+last_name)
                                        .build();

                                user.updateProfile(profileChangeRequest);
                            }
//                            Toast.makeText(getApplicationContext(),"Đăng kí thành công", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"Email đã tồn tại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                Toast.makeText(getApplicationContext(),"Mật khẩu không trùng khớp", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        }
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
        edt_first_name=findViewById(R.id.edt_first_name);
        edt_last_name=findViewById(R.id.edt_last_name);
        edt_email=findViewById(R.id.edt_email);
        btn_register=findViewById(R.id.btn_register);
    }
}