package com.awakedrive.brainwave.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragmentList = new ArrayList<>();

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void addFragment(Fragment fragment) {
        fragmentList.add(fragment);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
//        switch (position){
//            case 0: return new HomeFragment();
//            case 1: return new DeviceFragment();
//            case 2: return new PlayerFragment();
//            case 3: return new AccountFragment();
//            default: return new HomeFragment();
//        }
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    @Override
    public long getItemId(int position) {
        return fragmentList.get(position).hashCode();
    }

    @Override
    public boolean containsItem(long itemId) {
        for (Fragment fragment : fragmentList) {
            if (fragment.hashCode() == itemId) return true;
        }
        return false;
    }
}
