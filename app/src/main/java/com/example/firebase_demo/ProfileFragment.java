package com.example.firebase_demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileFragment extends Fragment {

    // Views
    private ImageView imgAvatar;
    private TextView txtUserName, txtUserEmail, txtFavoriteCount, txtNickname;
    private Button btnLogout;
    private RelativeLayout layoutNickname;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference userDbRef;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Ánh xạ Views
        imgAvatar = view.findViewById(R.id.img_avatar);
        txtUserName = view.findViewById(R.id.txt_user_name);
        txtUserEmail = view.findViewById(R.id.txt_user_email);
        txtFavoriteCount = view.findViewById(R.id.txt_favorite_count);
        btnLogout = view.findViewById(R.id.btn_logout);
        txtNickname = view.findViewById(R.id.txt_nickname);
        layoutNickname = view.findViewById(R.id.layout_nickname);

        if (currentUser == null) {
            goToLoginActivity();
            return;
        }

        userDbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        // Khởi tạo Image Picker Launcher để lấy kết quả chọn ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            // Sau khi chọn ảnh, xử lý và lưu bằng Base64
                            handleImageSelectionAsBase64(imageUri);
                        }
                    }
                }
        );

        // Tải thông tin người dùng
        loadUserProfile(currentUser);

        // Thiết lập sự kiện click
        btnLogout.setOnClickListener(v -> logoutUser());
        imgAvatar.setOnClickListener(v -> launchImagePicker());
        layoutNickname.setOnClickListener(v -> showEditNicknameDialog());
    }

    private void loadUserProfile(FirebaseUser currentUser) {
        // 1. Hiển thị thông tin cơ bản từ Firebase Auth
        txtUserName.setText(currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty() ? currentUser.getDisplayName() : "Chưa có tên");
        txtUserEmail.setText(currentUser.getEmail());

        // 2. Lấy thông tin từ Realtime Database (Nickname, Avatar Base64, số phim yêu thích)
        userDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                // Lấy nickname
                if (snapshot.child("nickname").exists()) {
                    txtNickname.setText(snapshot.child("nickname").getValue(String.class));
                } else {
                    txtNickname.setText("Chưa có");
                }

                // Lấy và hiển thị avatar từ chuỗi Base64
                if (snapshot.child("avatarBase64").exists()) {
                    String imageBase64 = snapshot.child("avatarBase64").getValue(String.class);
                    byte[] imageData = Base64.decode(imageBase64, Base64.DEFAULT);
                    Glide.with(ProfileFragment.this).load(imageData).placeholder(R.drawable.ic_person).into(imgAvatar);
                } else {
                    // Nếu không có avatar, dùng ảnh mặc định
                    imgAvatar.setImageResource(R.drawable.ic_person);
                }

                // Lấy số lượng phim yêu thích
                long favoriteCount = snapshot.child("favorites").getChildrenCount();
                txtFavoriteCount.setText(String.valueOf(favoriteCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
                txtFavoriteCount.setText("0");
                txtNickname.setText("Lỗi");
            }
        });
    }

    // Hàm mở thư viện ảnh
    private void launchImagePicker() {
        // --- BẮT ĐẦU SỬA ---
        // Thay đổi cách gọi Intent để đảm bảo tương thích tốt hơn
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleImageSelectionAsBase64(Uri imageUri) {
        try {
            Toast.makeText(getContext(), "Đang xử lý ảnh...", Toast.LENGTH_SHORT).show();

            // 1. Chuyển Uri thành Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);

            // 2. Nén ảnh lại để giảm kích thước (rất quan trọng)
            Bitmap resizedBitmap = resizeBitmap(bitmap, 400); // Nén ảnh xuống chiều rộng tối đa 400px

            // 3. Chuyển Bitmap đã nén thành mảng byte
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos); // Nén chất lượng 85%
            byte[] imageData = baos.toByteArray();

            // 4. Mã hóa mảng byte thành chuỗi Base64
            String imageBase64 = Base64.encodeToString(imageData, Base64.DEFAULT);

            // 5. Lưu chuỗi Base64 vào Realtime Database
            userDbRef.child("avatarBase64").setValue(imageBase64)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi cập nhật ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            Toast.makeText(getContext(), "Không thể đọc được file ảnh.", Toast.LENGTH_SHORT).show();
            Log.e("ProfileFragment", "Lỗi xử lý ảnh", e);
        }
    }

    // Hàm tiện ích để resize Bitmap
    public Bitmap resizeBitmap(Bitmap source, int maxLength) {
        try {
            if (source.getHeight() >= source.getWidth()) {
                if (source.getHeight() <= maxLength) return source; // Không cần resize
                int newHeight = maxLength;
                int newWidth = (int) (source.getWidth() * ((float) newHeight / source.getHeight()));
                return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
            } else {
                if (source.getWidth() <= maxLength) return source; // Không cần resize
                int newWidth = maxLength;
                int newHeight = (int) (source.getHeight() * ((float) newWidth / source.getWidth()));
                return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
            }
        } catch (Exception e) {
            return source;
        }
    }

    // Hàm hiển thị hộp thoại chỉnh sửa nickname
    private void showEditNicknameDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // Bạn có thể thêm Style ở đây
        builder.setTitle("Thay đổi biệt danh");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Nhập biệt danh mới");
        String currentNickname = txtNickname.getText().toString();
        if (!currentNickname.equals("Chưa có")) {
            input.setText(currentNickname);
        }
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newNickname = input.getText().toString().trim();
            if (!newNickname.isEmpty()) {
                userDbRef.child("nickname").setValue(newNickname)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã cập nhật biệt danh", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Các hàm còn lại giữ nguyên
    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        goToLoginActivity();
    }

    private void goToLoginActivity() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
