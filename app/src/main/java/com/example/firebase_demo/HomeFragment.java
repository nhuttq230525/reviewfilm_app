package com.example.firebase_demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

// --- (1) THÊM CÁC IMPORT CẦN THIẾT ---
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HomeFragment extends Fragment {

    private ViewPager2 bannerViewPager;
    private BannerAdapter bannerAdapter;
    private ArrayList<Movie> hotMoviesList;

    private RecyclerView recyclerViewAllMovies;
    private MovieAdapter allMoviesAdapter;
    private ArrayList<Movie> allMoviesList;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private Set<String> favoriteMovieIds = new HashSet<>();
    private DatabaseReference userFavoritesRef;
    private ValueEventListener favoriteIdsListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Thiết lập Banner ---
        bannerViewPager = view.findViewById(R.id.banner_view_pager);
        hotMoviesList = new ArrayList<>();
        bannerAdapter = new BannerAdapter(getContext(), hotMoviesList);
        bannerViewPager.setAdapter(bannerAdapter);
        setupBannerEffects();

        // --- Thiết lập RecyclerView cho Tất cả Phim ---
        recyclerViewAllMovies = view.findViewById(R.id.recyclerView_movies);
        allMoviesList = new ArrayList<>();
        allMoviesAdapter = new MovieAdapter(allMoviesList, getContext());
        recyclerViewAllMovies.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAllMovies.setAdapter(allMoviesAdapter);

        // --- (3) THAY ĐỔI TRÌNH TỰ TẢI DỮ LIỆU ---
        // Lấy danh sách ID yêu thích trước, sau đó mới tải các danh sách phim.
        loadFavoriteIdsAndThenMovies();
    }
    private void loadFavoriteIdsAndThenMovies() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Nếu người dùng chưa đăng nhập, tải phim luôn mà không cần check yêu thích
        if (currentUser == null) {
            loadHotMovies();
            loadAllMovies();
            return;
        }

        // Tham chiếu đến nút "favorites" của người dùng hiện tại
        userFavoritesRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUser.getUid()).child("favorites");

        // Dùng addValueEventListener để lắng nghe sự thay đổi real-time
        favoriteIdsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteMovieIds.clear();
                for (DataSnapshot idSnapshot : snapshot.getChildren()) {
                    favoriteMovieIds.add(idSnapshot.getKey());
                }
                // Sau khi có danh sách ID, tiến hành tải phim (hoặc tải lại để cập nhật)
                loadHotMovies();
                loadAllMovies();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Nếu có lỗi, vẫn tải phim bình thường
                Log.e("HomeFragment", "Lỗi tải danh sách yêu thích: ", error.toException());
                loadHotMovies();
                loadAllMovies();
            }
        };
        userFavoritesRef.addValueEventListener(favoriteIdsListener);
    }


    private void setupBannerEffects() {
        // ... (Giữ nguyên không đổi)
        bannerViewPager.setOffscreenPageLimit(3);
        bannerViewPager.setClipToPadding(false);
        bannerViewPager.setClipChildren(false);
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        bannerViewPager.setPageTransformer(transformer);
    }

    private void loadHotMovies() {
        DatabaseReference moviesRef = FirebaseDatabase.getInstance().getReference("movies");
        Query query = moviesRef.orderByChild("isHot").equalTo(true);

        query.addListenerForSingleValueEvent(new ValueEventListener() { // Dùng Single event để không bị lặp lại
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                hotMoviesList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Movie movie = dataSnapshot.getValue(Movie.class);
                    if (movie != null) {
                        movie.setId(dataSnapshot.getKey());

                        // --- (4) KIỂM TRA YÊU THÍCH CHO PHIM HOT ---
                        if (favoriteMovieIds.contains(movie.getId())) {
                            movie.setFavorite(true);
                        }

                        hotMoviesList.add(movie);
                    }
                }
                bannerAdapter.notifyDataSetChanged();

                if (!hotMoviesList.isEmpty()) {
                    startBannerAutoScroll();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // ... (Giữ nguyên không đổi)
                if(getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải banner", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadAllMovies() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("movies");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() { // Dùng Single event để không bị lặp lại
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                allMoviesList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Movie movie = dataSnapshot.getValue(Movie.class);
                    if (movie != null) {
                        movie.setId(dataSnapshot.getKey());

                        // --- (5) KIỂM TRA YÊU THÍCH CHO TẤT CẢ PHIM ---
                        if (favoriteMovieIds.contains(movie.getId())) {
                            movie.setFavorite(true);
                        }

                        allMoviesList.add(movie);
                    }
                }
                Collections.reverse(allMoviesList);
                allMoviesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // ... (Giữ nguyên không đổi)
                if(getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải danh sách phim", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startBannerAutoScroll() {
        // ... (Giữ nguyên không đổi)
        bannerHandler.removeCallbacks(bannerRunnable);
        bannerRunnable = () -> {
            int currentItem = bannerViewPager.getCurrentItem();
            if (bannerAdapter.getItemCount() > 0) {
                int nextItem = (currentItem + 1) % bannerAdapter.getItemCount();
                bannerViewPager.setCurrentItem(nextItem, true);
            }
            bannerHandler.postDelayed(bannerRunnable, 3000);
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);
    }

    // --- (6) THÊM CÁC HÀM VÒNG ĐỜI ĐỂ QUẢN LÝ LISTENER ---
    @Override
    public void onPause() {
        super.onPause();
        bannerHandler.removeCallbacks(bannerRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hotMoviesList != null && !hotMoviesList.isEmpty()) {
            startBannerAutoScroll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Gỡ bỏ listener để tránh rò rỉ bộ nhớ khi Fragment bị hủy
        if (userFavoritesRef != null && favoriteIdsListener != null) {
            userFavoritesRef.removeEventListener(favoriteIdsListener);
        }
    }
}
