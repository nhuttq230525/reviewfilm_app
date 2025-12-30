package com.example.firebase_demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView tvEmail, tvFavoriteCount, tvWatchTimeStatus, tvWatchMinutes;
    private EditText edtDisplayName;
    private Button btnSaveName, btnLogout;

    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1. Ánh xạ View
        tvEmail = view.findViewById(R.id.tv_email_profile);

        tvFavoriteCount = view.findViewById(R.id.tv_favorite_count);
        tvWatchTimeStatus = view.findViewById(R.id.tv_watch_time_status);
        tvWatchMinutes = view.findViewById(R.id.tv_watch_minutes);
        edtDisplayName = view.findViewById(R.id.edt_display_name);
        btnSaveName = view.findViewById(R.id.btn_save_name);
        btnLogout = view.findViewById(R.id.btn_logout_profile);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            tvEmail.setText(currentUser.getEmail());


            userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);


            loadUserProfile();
            loadFavoriteCount();
        } else {

            tvEmail.setText("Chưa đăng nhập");
            btnSaveName.setEnabled(false);
        }


        btnSaveName.setOnClickListener(v -> saveDisplayName());


        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            // Chuyển về màn hình đăng nhập và xóa lịch sử activity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }


    private void loadUserProfile() {
        if (userRef == null) return;

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;


                if (snapshot.child("displayName").exists()) {
                    String name = snapshot.child("displayName").getValue(String.class);
                    edtDisplayName.setText(name);
                }


                long minutes = 0;
                if (snapshot.child("watchTimeMinutes").exists()) {

                    Object value = snapshot.child("watchTimeMinutes").getValue();
                    if (value instanceof Long) {
                        minutes = (Long) value;
                    } else if (value instanceof String) {
                        try {
                            minutes = Long.parseLong((String) value);
                        } catch (NumberFormatException e) {
                            minutes = 0;
                        }
                    }
                }
                updateWatchTimeUI(minutes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Hàm tải số lượng phim yêu thích
    private void loadFavoriteCount() {
        if (userRef == null) return;

        DatabaseReference favRef = userRef.child("favorites");
        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                long count = snapshot.getChildrenCount();
                tvFavoriteCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // Hàm cập nhật giao diện thời gian xem và cảnh báo
    private void updateWatchTimeUI(long minutes) {
        tvWatchMinutes.setText(minutes + " phút");

        if (minutes > 180) {
            tvWatchTimeStatus.setText("CẢNH BÁO!");
            // Đổi màu đỏ
            tvWatchTimeStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvWatchTimeStatus.setText("An toàn");
            // Đổi màu xanh
            tvWatchTimeStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    // Hàm lưu tên hiển thị lên Firebase
    private void saveDisplayName() {
        String newName = edtDisplayName.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userRef != null) {
            userRef.child("displayName").setValue(newName)
                    .addOnSuccessListener(aVoid -> {
                        if (getContext() != null)
                            Toast.makeText(getContext(), "Đã lưu tên thành công!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null)
                            Toast.makeText(getContext(), "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
