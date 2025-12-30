package com.example.firebase_demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView recyclerView;
    private LinearLayout noResultsLayout;
    private MovieAdapter movieAdapter;
    private ArrayList<Movie> allMovies;
    private ArrayList<Movie> searchResults;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        searchEditText = view.findViewById(R.id.search_edit_text);
        recyclerView = view.findViewById(R.id.recycler_view_search_results);
        noResultsLayout = view.findViewById(R.id.layout_no_results);


        allMovies = new ArrayList<>();
        searchResults = new ArrayList<>();
        movieAdapter = new MovieAdapter(searchResults, getContext());


        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // Hiển thị 2 cột
        recyclerView.setAdapter(movieAdapter);


        loadAllMoviesFromFirebase();


        setupSearchEditText();
    }

    private void loadAllMoviesFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("movies");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allMovies.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Movie movie = dataSnapshot.getValue(Movie.class);
                    if (movie != null) {
                        allMovies.add(movie);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setupSearchEditText() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                searchRunnable = () -> performSearch(s.toString());
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });
    }

    private void performSearch(String query) {
        searchResults.clear();

        if (query.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noResultsLayout.setVisibility(View.GONE);
            movieAdapter.notifyDataSetChanged();
            return;
        }

        String lowerCaseQuery = query.toLowerCase().trim();

        for (Movie movie : allMovies) {

            if (movie.getTitle() != null) {

                String movieTitle = movie.getTitle().toLowerCase();


                if (movieTitle.contains(lowerCaseQuery)) {
                    searchResults.add(movie);
                }
            }
        }



        if (searchResults.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noResultsLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noResultsLayout.setVisibility(View.GONE);
        }
        movieAdapter.notifyDataSetChanged();
    }
}
