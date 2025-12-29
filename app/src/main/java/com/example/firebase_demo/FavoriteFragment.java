package com.example.firebase_demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;import android.widget.Toast;

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

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerFavorites;
    private TextView tvEmpty;
    private MovieAdapter favoriteAdapter;
    private ArrayList<Movie> favoriteMoviesList;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        recyclerFavorites = view.findViewById(R.id.recycler_favorites);
        tvEmpty = view.findViewById(R.id.tv_empty_favorites);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        setupRecyclerView();
        loadFavoriteMovies();

        return view;
    }

    private void setupRecyclerView() {
        favoriteMoviesList = new ArrayList<>();
        favoriteAdapter = new MovieAdapter(favoriteMoviesList, getContext());
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerFavorites.setAdapter(favoriteAdapter);
    }

    private void loadFavoriteMovies() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            tvEmpty.setText("Vui lòng đăng nhập để xem yêu thích");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        // 1. Lấy danh sách ID phim yêu thích từ node Users
        DatabaseReference favRef = databaseReference.child("Users").child(currentUser.getUid()).child("favorites");

        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteMoviesList.clear(); // Xóa list cũ để cập nhật mới

                if (!snapshot.exists()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    favoriteAdapter.notifyDataSetChanged();
                    return;
                }

                tvEmpty.setVisibility(View.GONE);

                // 2. Duyệt qua từng ID và lấy thông tin chi tiết phim
                for (DataSnapshot data : snapshot.getChildren()) {
                    String movieId = data.getKey();
                    loadMovieDetail(movieId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMovieDetail(String movieId) {
        DatabaseReference movieRef = databaseReference.child("movies").child(movieId); // Lưu ý: kiểm tra xem node trên Firebase là "movies" hay "Movies"

        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Movie movie = snapshot.getValue(Movie.class);
                if (movie != null) {
                    movie.setId(snapshot.getKey());
                    movie.setFavorite(true); // Đánh dấu tim đỏ

                    // Thêm vào list và cập nhật giao diện
                    // (Có thể kiểm tra trùng lặp nếu cần, nhưng logic clear() ở trên đã xử lý phần nào)
                    favoriteMoviesList.add(movie);
                    favoriteAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
