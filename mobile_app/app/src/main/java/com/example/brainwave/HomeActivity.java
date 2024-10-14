package com.example.brainwave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
//import com.example.brainwave.databinding
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.example.brainwave.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    public BottomNavigationView bottomNavigationView;

    private static final int REQUEST_CODE = 99;

    CardView measuringCard;
    CardView focusingCard;
    CardView relaxingCard;
    CardView monitoringCard;
    CardView demoCard;
    CardView gameCard;

    public void loadFragment(Fragment fragment, boolean isAppInitialized) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (isAppInitialized) {
            fragmentTransaction.add(R.id.frame_layout, fragment);
        }
        else {
            fragmentTransaction.replace(R.id.frame_layout, fragment);
        }

        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_CODE);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bot_home:
                        loadFragment(new FragmentHome(), false);
                        break;
                    case R.id.bot_music:
                        loadFragment(new FragmentMusic(), false);
                        break;
                    case R.id.bot_tracker:
                        loadFragment(new FragmentTracker(), false);
                        break;
                    case R.id.bot_about:
                        loadFragment(new FragmentAbout(), false);
                        break;
                }
                return true;
            }
        });

        loadFragment(new FragmentHome(), true);

//        measuringCard = findViewById(R.id.measuringCard);
//        focusingCard = findViewById(R.id.focusingCard);
//        relaxingCard = findViewById(R.id.relaxingCard);
//        monitoringCard = findViewById(R.id.monitoringCard);
//        demoCard = findViewById(R.id.demoCard);
//        gameCard = findViewById(R.id.gameCard);

//        measuringCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {new Intent(HomeActivity.this, Bluet
//                Intent intent = oothAdapterActivity.class);
//                startActivity(intent);
//            }
//        });

//        focusingCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(HomeActivity.this, AttentionActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        relaxingCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(HomeActivity.this, SleepActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        monitoringCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(HomeActivity.this, ModelsActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        demoCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(HomeActivity.this, LabeledActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        gameCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(HomeActivity.this, UpdateActivity.class);
//                startActivity(intent);
//            }
//        });
    }
}