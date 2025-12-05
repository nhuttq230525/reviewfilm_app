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

        // 1. Tắt tính năng vuốt để người dùng chỉ điều hướng bằng BottomNavigationView.
        //    Cách này đơn giản và hiệu quả hơn việc xử lý đồng bộ hai chiều.
        viewPager.setUserInputEnabled(false);

        // 2. Đặt giới hạn chỉ tải trang hiện tại (quan trọng nhất để giải quyết lỗi focus).
        //    Lệnh này ngăn ViewPager tạo các Fragment bên cạnh, loại bỏ xung đột.
        viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);


        // Lắng nghe sự kiện khi người dùng nhấn vào một item trên BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                // Sử dụng 'false' để chuyển trang ngay lập tức, không cần hiệu ứng vuốt
                if (itemId == R.id.nav_home) {
                    viewPager.setCurrentItem(0, false);
                } else if (itemId == R.id.nav_search) {
                    viewPager.setCurrentItem(1, false);
                } else if (itemId == R.id.nav_category) {
                    viewPager.setCurrentItem(2, false);
                } else if (itemId == R.id.nav_favorites) {
                    viewPager.setCurrentItem(3, false);
                } else if (itemId == R.id.nav_profile) {
                    viewPager.setCurrentItem(4, false);
                }
                return true;
            }
        });
        // Đặt trang mặc định khi mở ứng dụng
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

    }
}
