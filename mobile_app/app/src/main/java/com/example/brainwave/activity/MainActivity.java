package com.example.brainwave.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.brainwave.R;
import com.example.brainwave.adapter.ViewPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private BottomNavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        viewPager2=findViewById(R.id.view_pager);
        navigationView=findViewById(R.id.nav_view);
        ViewPagerAdapter pagerAdapter=new ViewPagerAdapter(this);
        viewPager2.setAdapter(pagerAdapter);
        initControl();
    }

    private void initControl() {
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
                } else if (position == 1) {
                    navigationView.getMenu().findItem(R.id.nav_device).setChecked(true);
                } else if (position == 2) {
                    navigationView.getMenu().findItem(R.id.nav_player).setChecked(true);
                }else if (position == 3) {
                    navigationView.getMenu().findItem(R.id.nav_account).setChecked(true);
                }
            }
        });
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    viewPager2.setCurrentItem(0, false);
                } else if (itemId == R.id.nav_device) {
                    viewPager2.setCurrentItem(1, false);
                } else if (itemId == R.id.nav_player) {
                    viewPager2.setCurrentItem(2, false);
                }else if (itemId == R.id.nav_account) {
                    viewPager2.setCurrentItem(3, false);
                }
                return true;
            }
        });
    }

}