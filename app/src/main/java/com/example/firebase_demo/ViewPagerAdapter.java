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
                // Nếu bạn đã xóa file FavoriteFragment thì dùng ProfileFragment hoặc tạo lại FavoriteFragment
                // Theo yêu cầu của bạn là "giữ chức năng yêu thích", nếu bạn có file FavoriteFragment.java thì return nó ở đây.
                // Nếu chưa có, bạn cần tạo file FavoriteFragment.java để hiển thị danh sách yêu thích.
                return new FavoriteFragment();
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
