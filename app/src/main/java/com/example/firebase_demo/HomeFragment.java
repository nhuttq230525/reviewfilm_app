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

    // --- Khai báo cho phần Banner ---
    private ViewPager2 bannerViewPager;
    private BannerAdapter bannerAdapter;
    private ArrayList<Movie> hotMoviesList;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;

    // --- Khai báo cho phần Danh sách phim ---
    private RecyclerView recyclerViewAllMovies;
    private MovieAdapter allMoviesAdapter;
    private ArrayList<Movie> allMoviesList;

    // --- Khai báo cho phần Yêu thích ---
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

        // --- 1. Thiết lập Banner ---
        bannerViewPager = view.findViewById(R.id.banner_view_pager);
        if (bannerViewPager != null) {
            hotMoviesList = new ArrayList<>();
            bannerAdapter = new BannerAdapter(getContext(), hotMoviesList);
            bannerViewPager.setAdapter(bannerAdapter);
            setupBannerEffects();
        }

        // --- 2. Thiết lập RecyclerView cho Tất cả Phim ---
        // Lưu ý: Kiểm tra lại ID trong XML vừa sửa ở bước 1 xem có khớp không
        recyclerViewAllMovies = view.findViewById(R.id.recycler_view_home);

        allMoviesList = new ArrayList<>();
        allMoviesAdapter = new MovieAdapter(allMoviesList, getContext());
        recyclerViewAllMovies.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAllMovies.setAdapter(allMoviesAdapter);

        // --- 3. Tải dữ liệu ---
        loadFavoriteIdsAndThenMovies();
    }

    // --- CÁC HÀM LOGIC CŨ GIỮ NGUYÊN ---

    private void loadFavoriteIdsAndThenMovies() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            loadHotMovies();
            loadAllMovies();
            return;
        }

        userFavoritesRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUser.getUid()).child("favorites");

        favoriteIdsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteMovieIds.clear();
                for (DataSnapshot idSnapshot : snapshot.getChildren()) {
                    favoriteMovieIds.add(idSnapshot.getKey());
                }
                loadHotMovies();
                loadAllMovies();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Lỗi tải danh sách yêu thích: ", error.toException());
                loadHotMovies();
                loadAllMovies();
            }
        };
        userFavoritesRef.addValueEventListener(favoriteIdsListener);
    }

    private void setupBannerEffects() {
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

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                hotMoviesList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Movie movie = dataSnapshot.getValue(Movie.class);
                    if (movie != null) {
                        movie.setId(dataSnapshot.getKey());
                        if (favoriteMovieIds.contains(movie.getId())) {
                            movie.setFavorite(true);
                        }
                        hotMoviesList.add(movie);
                    }
                }
                if (bannerAdapter != null) {
                    bannerAdapter.notifyDataSetChanged();
                }

                if (!hotMoviesList.isEmpty()) {
                    startBannerAutoScroll();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadAllMovies() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("movies");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                allMoviesList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Movie movie = dataSnapshot.getValue(Movie.class);
                    if (movie != null) {
                        movie.setId(dataSnapshot.getKey());
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
                if(getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải danh sách phim", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startBannerAutoScroll() {
        bannerHandler.removeCallbacks(bannerRunnable);
        bannerRunnable = () -> {
            if (bannerViewPager != null && bannerAdapter != null && bannerAdapter.getItemCount() > 0) {
                int currentItem = bannerViewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % bannerAdapter.getItemCount();
                bannerViewPager.setCurrentItem(nextItem, true);
            }
            bannerHandler.postDelayed(bannerRunnable, 3000);
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);
    }

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
        if (userFavoritesRef != null && favoriteIdsListener != null) {
            userFavoritesRef.removeEventListener(favoriteIdsListener);
        }
    }
}
