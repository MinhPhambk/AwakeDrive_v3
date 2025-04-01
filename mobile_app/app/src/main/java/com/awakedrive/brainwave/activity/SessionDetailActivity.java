package com.awakedrive.brainwave.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.awakedrive.brainwave.R;
import com.awakedrive.brainwave.adapter.DataAdapter;
import com.awakedrive.brainwave.model.BrainData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SessionDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DataAdapter dataAdapter;
    private List<BrainData> brainDataList;
    private DatabaseReference databaseRef;
    private String sessionId;
    private FirebaseAuth firebaseAuth;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        sessionId = getIntent().getStringExtra("sessionId");
        toolbar = findViewById(R.id.tool_bar);
        ActionToolBar();
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
    private void ActionToolBar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Chi tiết đo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
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
