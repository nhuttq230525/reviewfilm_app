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

        // 1. Nhận tên thể loại từ HomeFragment gửi sang
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        if (categoryName == null) categoryName = ""; // Tránh lỗi null

        // 2. Ánh xạ View
        toolbar = findViewById(R.id.toolbar_movie_list);
        recyclerView = findViewById(R.id.recycler_view_movie_list);
        progressBar = findViewById(R.id.progress_bar_movie_list);
        tvNoMovies = findViewById(R.id.tv_no_movies);

        // 3. Thiết lập Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thể loại: " + categoryName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // Bấm nút back thì đóng activity

        // 4. Thiết lập RecyclerView
        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(movieList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(movieAdapter);

        // 5. Tải dữ liệu
        loadMoviesByCategory(categoryName);
    }

    private void loadMoviesByCategory(String category) {

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoMovies.setVisibility(View.GONE);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("movies");


        Query query = databaseReference.orderByChild("genre").equalTo(category);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movieList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Movie movie = dataSnapshot.getValue(Movie.class);
                    if (movie != null) {
                        movie.setId(dataSnapshot.getKey());
                        movieList.add(movie);
                    }
                }
                Collections.reverse(movieList);


                progressBar.setVisibility(View.GONE);


                if (movieList.isEmpty()) {
                    tvNoMovies.setText("Không tìm thấy phim nào thuộc thể loại: " + category);
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
                Toast.makeText(MovieListActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
