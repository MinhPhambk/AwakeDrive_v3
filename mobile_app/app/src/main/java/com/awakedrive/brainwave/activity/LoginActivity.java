package com.awakedrive.brainwave.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.awakedrive.brainwave.Interface.SoundManager;
import com.awakedrive.brainwave.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private TextView registerText, forgotPassword;
    private Button loginButton;
    private GoogleSignInClient googleSignInClient;
    private EditText email_login, password_login;
    private ImageView img_google, img_facebook;
    private boolean isPasswordVisible = false;
    FirebaseAuth firebaseAuth;
    private static final int RC_SIGN_IN = 100;
    private CallbackManager callbackManager;
    private ConstraintLayout contrain_layout;
    private SoundManager soundManager;
    private static final int PERMISSION_REQUEST_CODE = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        onclick_login();
        onclick_forgot();
        onclick_register();
        ontouch_pass();
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            contrain_layout.setBackgroundColor(Color.BLACK);
        } else {
            contrain_layout.setBackground(ContextCompat.getDrawable(this, R.drawable.gradient_background));
        }
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();

            if (currentAccessToken != null && !currentAccessToken.isExpired()) {
                // Người dùng đã đăng nhập bằng Facebook
                handleFacebookAccessToken(currentAccessToken);

                // Nếu cần chuyển sang MainActivity sau khi xử lý Facebook login, thêm:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else if (user.isEmailVerified()) {
                // Người dùng đã đăng nhập bằng Firebase email/password và đã xác thực email
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                Log.d("name_login", user.getDisplayName());
            }
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        img_google.setOnClickListener(v -> {
            if (!hasBluetoothPermissions()) {
                Toast.makeText(this, "Vui lòng cấp đủ quyền để đăng nhập", Toast.LENGTH_SHORT).show();
            } else {
                signInWithGoogle();
            }
        });

        callbackManager = CallbackManager.Factory.create();
        img_facebook.setOnClickListener(v -> {
            if (!hasBluetoothPermissions()) {
                Toast.makeText(this, "Vui lòng cấp đủ quyền để đăng nhập", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
//                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
            }
        });
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
                String facebookToken = loginResult.getAccessToken().getToken();
                Log.d("FacebookToken", "Token: " + facebookToken);
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Đăng nhập bị hủy", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Toast.makeText(getApplicationContext(), "Lỗi đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void onclick_register() {
        registerText.setOnClickListener(v -> {
            soundManager.playSound();
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void onclick_forgot() {
        forgotPassword.setOnClickListener(v -> {
            soundManager.playSound();
            Intent intent = new Intent(getApplicationContext(), ForgetActivity.class);
            startActivity(intent);
        });
    }

    private void onclick_login() {
        firebaseAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(v -> {
            if (!hasBluetoothPermissions()) {
                Toast.makeText(this, "Vui lòng cấp đủ quyền để đăng nhập", Toast.LENGTH_SHORT).show();
            } else {
                soundManager.playSound();
                String pass = password_login.getText().toString();
                String email = email_login.getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {
                    firebaseAuth.signInWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    if (user.isEmailVerified()) {
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Email chưa được xác thực", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(), "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                                }
                            });

                } else {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }
            }
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

    private void signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Log.d("Google_token", account.getIdToken());
                        if (user != null) {
                            Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Login successful but no user info found", Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                String facebookToken = token.getToken();
                Log.d("FacebookToken", "Token: " + facebookToken);
                if (user != null) {
                    Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Login successful but no user info found", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Log.d("FirebaseAuth", "Lỗi đăng nhập Firebase: " + task.getException().getMessage());
                Toast.makeText(getApplicationContext(), "Email trùng đã tồn tại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.e("GoogleSignIn", "Google sign-in failed", e);
                Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show();
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Kiểm tra quyền BLUETOOTH_SCAN và BLUETOOTH_CONNECT trên Android 12+ (API >= 31)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        // Kiểm tra quyền ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Nếu có quyền cần yêu cầu, xin quyền
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                Toast.makeText(this, "Tất cả quyền đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Một số quyền bị từ chối! Không thể sử dụng Bluetooth.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean hasBluetoothPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }

    private void initView() {
        registerText = findViewById(R.id.registerText);
        forgotPassword = findViewById(R.id.forgotPassword);
        loginButton = findViewById(R.id.loginButton);
        email_login = findViewById(R.id.email_login);
        password_login = findViewById(R.id.password_login);
        img_facebook = findViewById(R.id.img_facebook);
        img_google = findViewById(R.id.img_google);
        img_facebook = findViewById(R.id.img_facebook);
        contrain_layout = findViewById(R.id.contrain_layout);
        soundManager = SoundManager.getInstance(this);
        checkAndRequestPermissions();
    }
}