package com.example.brainwave.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.brainwave.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private TextView txt_delta;
    private FirebaseAuth auth;
    private FirebaseDatabase databaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        txt_delta = findViewById(R.id.txt_delta);
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance();
        getAllBrainData();
    }

    private void getAllBrainData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("BrainData").child(uid);

            // Lắng nghe sự thay đổi theo thời gian thực
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        StringBuilder dataBuilder = new StringBuilder();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            long timestamp = Long.parseLong(data.child("timestamp").getValue(String.class));
                            String format = formatTimestamp(timestamp);
                            int delta = data.child("delta").getValue(Integer.class);
                            int highAlpha = data.child("highAlpha").getValue(Integer.class);
                            int highBeta = data.child("highBeta").getValue(Integer.class);
                            int lowBeta = data.child("lowBeta").getValue(Integer.class);
                            int lowGamma = data.child("lowGamma").getValue(Integer.class);
                            int lowAlpha = data.child("lowalpha").getValue(Integer.class);
                            int middleGamma = data.child("middleGamma").getValue(Integer.class);
                            int theta = data.child("theta").getValue(Integer.class);

                            // Thêm dữ liệu vào StringBuilder để hiển thị toàn bộ dữ liệu
                            dataBuilder.append("📅 Timestamp: ").append(format).append("\n")
                                    .append("🔹 Delta: ").append(delta).append("\n")
                                    .append("🔹 High Alpha: ").append(highAlpha).append("\n")
                                    .append("🔹 High Beta: ").append(highBeta).append("\n")
                                    .append("🔹 Low Beta: ").append(lowBeta).append("\n")
                                    .append("🔹 Low Gamma: ").append(lowGamma).append("\n")
                                    .append("🔹 Low Alpha: ").append(lowAlpha).append("\n")
                                    .append("🔹 Middle Gamma: ").append(middleGamma).append("\n")
                                    .append("🔹 Theta: ").append(theta).append("\n")
                                    .append("━━━━━━━━━━━━━━━━━━━━━━\n");
                        }

                        // Hiển thị tất cả dữ liệu lên TextView
                        txt_delta.setText(dataBuilder.toString());
                    } else {
                        txt_delta.setText("Không có dữ liệu!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Lỗi khi lấy dữ liệu", error.toException());
                }
            });
        } else {
            txt_delta.setText("Người dùng chưa đăng nhập!");
        }
    }

    private String formatTimestamp(long timestampMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestampMillis));
    }

}