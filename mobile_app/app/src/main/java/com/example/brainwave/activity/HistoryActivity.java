package com.example.brainwave.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brainwave.Interface.SoundManager;
import com.example.brainwave.R;
import com.example.brainwave.adapter.SessionAdapter;
import com.example.brainwave.model.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SessionAdapter sessionAdapter;
    private List<Session> sessionList;
    private DatabaseReference databaseRef;
    private FirebaseAuth firebaseAuth;
    private TextView tv_usage_time, tv_avenger_usage, tv_message;
    private Toolbar toolbar;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initView();
        ActionToolBar();
        sessionList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            databaseRef = FirebaseDatabase.getInstance()
                    .getReference("BrainData")
                    .child(user.getUid())
                    .child("sessions");
        }
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalStatus = 0;  // Tổng giá trị status
                int count = 0;        // Số lần ghi nhận status

                // Duyệt qua tất cả các session và tính tổng status
                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                    DataSnapshot dataSnapshot = sessionSnapshot.child("data");
                    for (DataSnapshot recordSnapshot : dataSnapshot.getChildren()) {
                        if (recordSnapshot.child("status").exists()) {
                            int status = recordSnapshot.child("status").getValue(Integer.class);
                            totalStatus += status; // Cộng dồn giá trị status
                            count++;              // Đếm số lần ghi nhận status
                        }
                    }
                }

                // Kiểm tra và tính phần trăm tỉnh táo
                if (count > 0) {
                    double alertnessPercentage = (totalStatus * 100.0) / (count * 100);
                    TextView avg_status = findViewById(R.id.avg_status);
                    avg_status.setText(String.format("%.2f%%", alertnessPercentage));
                } else {
                    Log.d("Firebase", "Không có dữ liệu status để tính toán.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu", error.toException());
            }
        });


        if (user != null) {
            databaseRef = FirebaseDatabase.getInstance()
                    .getReference("BrainData")
                    .child(user.getUid())
                    .child("sessions");
        }
        if (user.getDisplayName() != null) {
            tv_message.setText(user.getDisplayName() + " quả là một chú ong chăm chỉ\nCần ngủ đủ giấc để đảm bảo khỏe mạnh");
        } else {
            tv_message.setText(user.getDisplayName() + "Awake Drive quả là một chú ong chăm chỉ\nCần ngủ đủ giấc để đảm bảo khỏe mạnh");
        }
        loadSessionHistory();
        if (sessionList != null) {
            sessionAdapter = new SessionAdapter(this, sessionList, this::openSessionDetail);
        } else {
            Toast.makeText(getApplicationContext(), "Khong co du lieu", Toast.LENGTH_SHORT).show();
        }
        recyclerView.setAdapter(sessionAdapter);

        timeuse();
    }

    private void ActionToolBar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Lịch sử đo đạc");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            soundManager.playSound();
            finish();
        });
    }

    private void initView() {
        recyclerView = findViewById(R.id.recyclerView);
        tv_usage_time = findViewById(R.id.tv_usage_time);
        tv_avenger_usage = findViewById(R.id.tv_avenger_usage);
        tv_message = findViewById(R.id.tv_message);
        toolbar = findViewById(R.id.tool_bar);
        soundManager = SoundManager.getInstance(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    private void timeuse() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalTime = 0;
                int sessionCount = 0;

                for (DataSnapshot session : snapshot.getChildren()) {
                    long startTime = session.child("info/startTime").getValue(Long.class);
                    long endTime = session.child("info/endTime").getValue(Long.class);

                    if (endTime > startTime) {
                        long duration = endTime - startTime;
                        totalTime += duration;
                        sessionCount++;
                    }
                }

                long avgTime = (sessionCount > 0) ? totalTime / sessionCount : 0;
                double totalHours = totalTime / 3600000.0;
                double avgHours = (sessionCount > 0) ? totalHours / sessionCount : 0;
                tv_usage_time.setText(String.format("%.3f giờ", totalHours));
                tv_avenger_usage.setText(String.format("%.3f giờ", avgHours));
                Log.d("FirebaseData", totalHours + avgHours + "");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Lỗi khi truy xuất dữ liệu", error.toException());
            }
        });
    }

    private void loadSessionHistory() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sessionList.clear();
                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                    DataSnapshot infoSnapshot = sessionSnapshot.child("info");
                    Long startTime = infoSnapshot.child("startTime").getValue(Long.class);
                    Long endTime = infoSnapshot.child("endTime").getValue(Long.class);
                    if (startTime != null && endTime != null) {
                        sessionList.add(new Session(sessionSnapshot.getKey(), startTime, endTime));
                    } else {
                        Log.e("Firebase", "Thiếu dữ liệu" + sessionSnapshot.getKey());
                    }
                }

                sessionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải dữ liệu", error.toException());
            }
        });
    }

    private void openSessionDetail(Session session) {
        Intent intent = new Intent(this, SessionDetailActivity.class);
        intent.putExtra("sessionId", session.getSessionId());
        startActivity(intent);
    }

    private String formatTimestamp(long timestampMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestampMillis));
    }

}