package com.example.firebase_demo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
    super(fragmentActivity);
}

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment(); // Trang chủ
            case 1:
                return new SearchFragment(); // Tìm kiếm
            case 2:
                return new FavoriteFragment();//Yêu thích
            case 3:
                return new ProfileFragment(); // Cá nhân
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Chỉ còn 4 tab
    }
}
