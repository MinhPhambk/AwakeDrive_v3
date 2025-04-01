package com.awakedrive.brainwave.activity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.awakedrive.brainwave.Interface.SoundManager;
import com.awakedrive.brainwave.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private ImageView avatar, edit_pass;
    private EditText pass;
    private FirebaseAuth firebaseAuth;
    private TextView tv_name, tv_email;
    private Toolbar toolbar;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initView();
        ActionToolBar();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            pass.setTextColor(Color.BLACK);
            edit_pass.setColorFilter(Color.WHITE);
        }
        if (user != null) {
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(avatar);
            } else {
                Glide.with(this).load(R.drawable.baseline_account_circle_24).circleCrop().into(avatar);
            }            tv_name.setText(user.getDisplayName());
            tv_email.setText(user.getEmail());
        }

        edit_pass.setOnClickListener(v -> {
            soundManager.playSound();
            checkProviderAndChangePassword();
        });
    }

    private void ActionToolBar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Thông tin cá nhân");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            soundManager.playSound();
            finish();
        });
    }

    private void checkProviderAndChangePassword() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isGoogleOrFacebook = false;
        for (UserInfo profile : user.getProviderData()) {
            String providerId = profile.getProviderId();
            if ("google.com".equals(providerId) || "facebook.com".equals(providerId)) {
                isGoogleOrFacebook = true;
                break;
            }
        }

        if (isGoogleOrFacebook) {
            Toast.makeText(this, "Tài khoản đăng nhập bằng Google/Facebook không thể đổi mật khẩu!", Toast.LENGTH_LONG).show();
        } else {
            showChangePasswordDialog();
        }
    }

    private void changePassword(String oldPassword, String newPassword) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xác thực lại người dùng trước khi đổi mật khẩu
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi khi đổi mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Mật khẩu cũ không chính xác!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi Mật Khẩu");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        builder.setView(view);

        EditText edtOldPassword = view.findViewById(R.id.edt_old_password);
        EditText edtNewPassword = view.findViewById(R.id.edt_new_password);
        EditText edtConfirmPassword = view.findViewById(R.id.edt_confirm_password);
        Button btnConfirm = view.findViewById(R.id.btn_confirm_change);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnConfirm.setOnClickListener(v -> {
            soundManager.playSound();
            String oldPassword = edtOldPassword.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (newPassword.equals(confirmPassword)) {
                changePassword(oldPassword, newPassword);
                dialog.dismiss();  // Đóng dialog sau khi đổi mật khẩu
            } else {
                Toast.makeText(ProfileActivity.this, "Mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView() {
        avatar = findViewById(R.id.avatar);
        firebaseAuth = FirebaseAuth.getInstance();
        tv_name = findViewById(R.id.tv_name);
        tv_email = findViewById(R.id.tv_email);
        edit_pass = findViewById(R.id.edit_pass);
        pass = findViewById(R.id.pass);
        toolbar = findViewById(R.id.tool_bar);
        soundManager = SoundManager.getInstance(this);
    }
}
