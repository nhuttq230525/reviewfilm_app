package com.example.firebase_demo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private ArrayList<Movie> favoriteMovies;
    private LinearLayout noFavoritesLayout;

    private DatabaseReference userFavoritesRef;
    private ValueEventListener favoriteIdsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        recyclerView = view.findViewById(R.id.recycler_view_favorites);
        noFavoritesLayout = view.findViewById(R.id.layout_no_favorites);

        // Khởi tạo RecyclerView
        favoriteMovies = new ArrayList<>();
        movieAdapter = new MovieAdapter(favoriteMovies, getContext());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // Hiển thị 2 cột
        recyclerView.setAdapter(movieAdapter);

        // Bắt đầu quá trình tải dữ liệu
        loadFavoriteMovies();
    }

    private void loadFavoriteMovies() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Nếu người dùng chưa đăng nhập, hiển thị màn hình trống và thông báo
            updateUI(true);
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem danh sách yêu thích", Toast.LENGTH_LONG).show();
            return;
        }

        // Tham chiếu đến nút "favorites" của người dùng hiện tại
        userFavoritesRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUser.getUid()).child("favorites");

        // Lắng nghe sự thay đổi của danh sách ID phim yêu thích
        favoriteIdsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return; // Đảm bảo fragment còn tồn tại

                ArrayList<String> favoriteMovieIds = new ArrayList<>();
                for (DataSnapshot idSnapshot : snapshot.getChildren()) {
                    favoriteMovieIds.add(idSnapshot.getKey());
                }

                if (favoriteMovieIds.isEmpty()) {
                    // Nếu không có ID nào, xóa danh sách hiện tại và hiển thị màn hình trống
                    favoriteMovies.clear();
                    movieAdapter.notifyDataSetChanged();
                    updateUI(true);
                } else {
                    // Nếu có ID, tiến hành lấy thông tin chi tiết của các phim
                    fetchMoviesDetails(favoriteMovieIds);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu.", Toast.LENGTH_SHORT).show();
                }
                Log.e("FavoriteFragment", "Lỗi lắng nghe ID yêu thích: ", error.toException());
            }
        };
        userFavoritesRef.addValueEventListener(favoriteIdsListener);
    }

    private void fetchMoviesDetails(ArrayList<String> movieIds) {
        DatabaseReference moviesRef = FirebaseDatabase.getInstance().getReference("movies");
        favoriteMovies.clear(); // Xóa danh sách cũ để nạp danh sách mới

        final int[] moviesToLoad = {movieIds.size()}; // Đếm ngược số phim cần tải

        // Đảo ngược để phim mới thêm vào được lên đầu
        Collections.reverse(movieIds);

        for (String movieId : movieIds) {
            moviesRef.child(movieId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Movie movie = snapshot.getValue(Movie.class);
                        if (movie != null) {
                            movie.setId(snapshot.getKey());
                            movie.setFavorite(true);
                            favoriteMovies.add(movie);
                        }
                    }
                    moviesToLoad[0]--; // Giảm biến đếm

                    // Khi đã tải xong tất cả, cập nhật Adapter và giao diện
                    if (moviesToLoad[0] == 0) {
                        movieAdapter.notifyDataSetChanged();
                        updateUI(favoriteMovies.isEmpty());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    moviesToLoad[0]--;
                    if (moviesToLoad[0] == 0) {
                        movieAdapter.notifyDataSetChanged();
                        updateUI(favoriteMovies.isEmpty());
                    }
                    Log.e("FavoriteFragment", "Lỗi tải chi tiết phim: " + movieId, error.toException());
                }
            });
        }
    }

    // Hàm tiện ích để cập nhật giao diện
    private void updateUI(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            noFavoritesLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noFavoritesLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Rất quan trọng: Gỡ bỏ listener khi Fragment bị hủy để tránh rò rỉ bộ nhớ
        if (userFavoritesRef != null && favoriteIdsListener != null) {
            userFavoritesRef.removeEventListener(favoriteIdsListener);
        }
    }
}
