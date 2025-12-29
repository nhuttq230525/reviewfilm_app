package com.example.firebase_demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private TextView tvEmail;
    private Button btnLogout;
    private RecyclerView recyclerFavorites;
    private MovieAdapter favoriteAdapter;
    private ArrayList<Movie> favoriteMoviesList;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1. Ánh xạ View
        tvEmail = view.findViewById(R.id.tv_email);
        btnLogout = view.findViewById(R.id.btn_logout);
        recyclerFavorites = view.findViewById(R.id.recycler_favorites);

        // 2. Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // 3. Hiển thị thông tin User
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            tvEmail.setText(currentUser.getEmail());
            loadFavoriteMovies(currentUser.getUid()); // Gọi hàm tải phim
        } else {
            tvEmail.setText("Chưa đăng nhập");
        }

        // 4. Xử lý nút Đăng xuất
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            // Xóa hết các Activity cũ để không quay lại được
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    // HÀM TẢI DANH SÁCH YÊU THÍCH
    private void loadFavoriteMovies(String userId) {
        // Cấu hình RecyclerView
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        favoriteMoviesList = new ArrayList<>();
        favoriteAdapter = new MovieAdapter(favoriteMoviesList, getContext());
        recyclerFavorites.setAdapter(favoriteAdapter);

        // Bước 1: Vào Users -> userId -> favorites để lấy danh sách ID phim đã thích
        DatabaseReference favRef = databaseReference.child("Users").child(userId).child("favorites");

        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteMoviesList.clear(); // Xóa list cũ để tránh trùng lặp

                // Duyệt qua từng ID phim trong danh sách favorites
                for (DataSnapshot data : snapshot.getChildren()) {
                    String movieId = data.getKey(); // Lấy ID phim (ví dụ: movie_01)

                    // Bước 2: Với mỗi ID phim, vào bảng Movies để lấy thông tin chi tiết
                    loadMovieDetail(movieId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải danh sách", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm phụ để lấy chi tiết 1 bộ phim
    private void loadMovieDetail(String movieId) {
        DatabaseReference movieRef = databaseReference.child("Movies").child(movieId);
        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Movie movie = snapshot.getValue(Movie.class);
                if (movie != null) {
                    movie.setFavorite(true); // Đánh dấu là đã thích để hiện trái tim đỏ
                    movie.setId(snapshot.getKey()); // Đảm bảo ID chính xác
                    favoriteMoviesList.add(movie);
                    favoriteAdapter.notifyDataSetChanged(); // Cập nhật giao diện
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
