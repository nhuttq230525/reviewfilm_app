package com.example.firebase_demo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Ánh xạ ĐÚNG vị trí (position) với Fragment tương ứng
        switch (position) {
            case 0:
                return new HomeFragment();

            case 1:
                return new SearchFragment();

            case 2:
                return new CategoryFragment();

            case 3:
                return new FavoriteFragment();

            case 4:
                return new ProfileFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}