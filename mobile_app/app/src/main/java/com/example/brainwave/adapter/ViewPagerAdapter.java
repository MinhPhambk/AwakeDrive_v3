package com.example.brainwave.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.brainwave.fragment.AccountFragment;
import com.example.brainwave.fragment.DeviceFragment;
import com.example.brainwave.fragment.HomeFragment;
import com.example.brainwave.fragment.PlayerFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new HomeFragment();
            case 1: return new DeviceFragment();
            case 2: return new PlayerFragment();
            case 3: return new AccountFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
