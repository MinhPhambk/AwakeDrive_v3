package com.example.brainwave.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import com.example.brainwave.R;
import com.example.brainwave.activity.HistoryActivity;
import com.example.brainwave.activity.LoginActivity;
import com.example.brainwave.activity.ProfileActivity;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

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
    private LinearLayout line_info, line_support, line_history;

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
        btn_logout = view.findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
        line_info.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            startActivity(intent);
        });
        line_support.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:tdungvippro112@gmail.com"));
            startActivity(Intent.createChooser(intent, "Chọn ứng dụng Email"));
        });
        line_history.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), HistoryActivity.class);
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
        firebaseAuth = FirebaseAuth.getInstance();
    }
}
