package com.example.firebase_demo;

import android.content.Intent;import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Sau 3 giây tự chuyển sang Login
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // 1. Khởi tạo Intent để chuyển sang LoginActivity
                // Ensure LoginActivity.class exists in your project
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

                // 2. Bắt đầu Activity mới
                startActivity(intent);

                // 3. Đóng SplashActivity để người dùng không thể quay lại bằng nút Back
                finish();
            }
        }, 3000); // 3000ms = 3 giây
    }
}
