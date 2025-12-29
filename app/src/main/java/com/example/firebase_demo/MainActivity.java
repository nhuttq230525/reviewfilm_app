package com.example.firebase_demo;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ views
        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Khởi tạo adapter
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // 1. Tắt tính năng vuốt ngang để trải nghiệm tốt hơn với thanh menu dưới
        viewPager.setUserInputEnabled(false);

        // 2. Giới hạn load trang để tránh lỗi
        viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);

        // Lắng nghe sự kiện khi nhấn menu
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    viewPager.setCurrentItem(0, false); // Tab 0: Trang chủ
                } else if (itemId == R.id.nav_search) {
                    viewPager.setCurrentItem(1, false); // Tab 1: Tìm kiếm
                } else if (itemId == R.id.nav_favorites) {
                    viewPager.setCurrentItem(2, false); // Tab 2: Yêu thích (Đã đẩy lên)
                } else if (itemId == R.id.nav_profile) {
                    viewPager.setCurrentItem(3, false); // Tab 3: Cá nhân (Đã đẩy lên)
                }

                return true;
            }
        });

        // Đặt trang mặc định khi mở ứng dụng
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }
}
