package com.example.brainwave.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brainwave.R;
import com.example.brainwave.adapter.DataAdapter;
import com.example.brainwave.model.BrainData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SessionDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DataAdapter dataAdapter;
    private List<BrainData> brainDataList;
    private DatabaseReference databaseRef;
    private String sessionId;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        sessionId = getIntent().getStringExtra("sessionId");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        brainDataList = new ArrayList<>();
        dataAdapter = new DataAdapter(this, brainDataList);
        recyclerView.setAdapter(dataAdapter);
        if (user != null) {
            databaseRef = FirebaseDatabase.getInstance()
                    .getReference("BrainData")
                    .child(user.getUid())
                    .child("sessions")
                    .child(sessionId)
                    .child("data");
            loadSessionData();
        }

    }

    private void loadSessionData() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                brainDataList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    BrainData brainData = dataSnapshot.getValue(BrainData.class);
                    brainDataList.add(brainData);
                }
                dataAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải dữ liệu", error.toException());
            }
        });
    }
}
