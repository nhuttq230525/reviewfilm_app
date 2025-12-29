package com.example.firebase_demo;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class MovieListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private ArrayList<Movie> movieList;

    private ProgressBar progressBar;
    private TextView tvNoMovies;
    private Toolbar toolbar;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        if (categoryName == null || categoryName.isEmpty()) {
            Toast.makeText(this, "Không nhận được tên thể loại", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu không có dữ liệu
            return;
        }
        toolbar = findViewById(R.id.toolbar_movie_list);
        recyclerView = findViewById(R.id.recycler_view_movie_list);
        progressBar = findViewById(R.id.progress_bar_movie_list);
        tvNoMovies = findViewById(R.id.tv_no_movies);

        // --- Thiết lập Toolbar ---
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thể loại: " + categoryName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút Back
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());  // Xử lý khi nhấn nút Back

        // --- Thiết lập RecyclerView ---
        movieList = new ArrayList<>();


        //  SỬA LỖI Ở ĐÂY: Dùng hàm khởi tạo 2 tham số mới
        movieAdapter = new MovieAdapter(movieList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(movieAdapter);

        // --- Tải dữ liệu từ Firebase ---
        loadMoviesByCategory(categoryName);
    }

    private void loadMoviesByCategory(String category) {
        progressBar.setVisibility(View.VISIBLE);
        tvNoMovies.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("movies");
        Query query = databaseReference.orderByChild("category").equalTo(category);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movieList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Movie movie = dataSnapshot.getValue(Movie.class);
                    if (movie != null) {
                        // Vẫn lấy ID để phòng trường hợp cần dùng sau này
                        movie.setId(dataSnapshot.getKey());
                        movieList.add(movie);
                    }
                }

                Collections.reverse(movieList);
                progressBar.setVisibility(View.GONE);

                if (movieList.isEmpty()) {
                    tvNoMovies.setText("Chưa có phim cho thể loại này");
                    tvNoMovies.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvNoMovies.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    movieAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MovieListActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
//MovieListActivity