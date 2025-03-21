package com.example.brainwave.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.brainwave.Interface.SoundManager;
import com.example.brainwave.R;
import com.example.brainwave.activity.HistoryActivity;
import com.example.brainwave.activity.LoginActivity;
import com.example.brainwave.activity.PolicyActivity;
import com.example.brainwave.activity.ProfileActivity;
import com.example.brainwave.activity.SettingActivity;
import com.example.brainwave.Utils;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        return view;
    }


    private ImageView avatar;
    private TextView btn_logout, tv_username, tv_email;
    private FirebaseAuth firebaseAuth;
    private LinearLayout line_info, line_support, line_history, line_policy, line_setting;
    private SoundManager soundManager;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(avatar);
        } else {
            Glide.with(this).load(R.drawable.avatar).circleCrop().into(avatar);
        }
        tv_username.setText(user.getDisplayName());
        tv_email.setText(user.getEmail());
        btn_logout.setOnClickListener(v -> {
            soundManager.playSound();
            firebaseAuth.signOut();
            LoginManager.getInstance().logOut();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getContext(), GoogleSignInOptions.DEFAULT_SIGN_IN);
            googleSignInClient.signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
        line_info.setOnClickListener(v -> {
            soundManager.playSound();
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            startActivity(intent);
        });
        line_support.setOnClickListener(v -> {
            soundManager.playSound();
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:neuralofthings@gmail.com?subject=Góp ý về sản phẩm Awake Drive"));
            startActivity(Intent.createChooser(intent, "Chọn ứng dụng Email"));
        });
        line_history.setOnClickListener(v -> {
            soundManager.playSound();
            if(Utils.is_running){
                Toast.makeText(getContext(),"Vui lòng dừng phiên đo",Toast.LENGTH_SHORT).show();
            }else {
                Intent intent = new Intent(getContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });
        line_policy.setOnClickListener(v -> {
            soundManager.playSound();
            Intent intent = new Intent(getContext(), PolicyActivity.class);
            intent.putExtra("url", "https://www.freeprivacypolicy.com/live/80deb4b1-9c60-4378-89e1-c58388b81223");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        line_setting.setOnClickListener(v -> {
            soundManager.playSound();
            Intent intent = new Intent(getContext(), SettingActivity.class);
            startActivity(intent);
        });
    }


    private void initView(View view) {
        avatar = view.findViewById(R.id.avatar);
        tv_username = view.findViewById(R.id.tv_username);
        tv_email = view.findViewById(R.id.tv_email);
        line_info = view.findViewById(R.id.line_info);
        line_support = view.findViewById(R.id.line_support);
        line_history = view.findViewById(R.id.line_history);
        line_policy = view.findViewById(R.id.line_policy);
        btn_logout = view.findViewById(R.id.btn_logout);
        line_setting = view.findViewById(R.id.line_setting);
        firebaseAuth = FirebaseAuth.getInstance();
        soundManager = SoundManager.getInstance(getContext());
    }
}
