package com.example.firebase_demo;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Transaction;
import android.content.Intent;
public class VideoPlayerActivity extends AppCompatActivity {

    private MaterialButton btnAddFavorite, btnShare;
    private TextView tvLikesCount;
    private ExoPlayer player;
    private PlayerView playerView;
    private ProgressBar progressBar;
    private ImageView backButton;
    private DatabaseReference movieLikesRef;
    private DatabaseReference userFavoriteStatusRef;
    private boolean isCurrentlyFavorite = false;
    private TextView tvVideoTitle, tvYear, tvDuration;
    private RecyclerView recyclerViewComments;
    private EditText edtCommentInput;
    private ImageView btnSendComment;
    private CommentAdapter commentAdapter;

    private String videoUrl, movieTitle, movieId, movieYear, movieDuration;
    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference commentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_video_player);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Lấy dữ liệu từ Intent
        if (!getIntentData()) {
            // Nếu không có dữ liệu cần thiết, đóng Activity
            Toast.makeText(this, "Lỗi: Không có dữ liệu phim.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ tất cả views từ layout mới
        bindViews();

        // Gán dữ liệu cho các TextViews
        setupMovieInfo();

        // Khởi tạo trình phát video
        initializePlayer();

        // Thiết lập hệ thống bình luận
        setupCommentSection();

        // Xử lý sự kiện nhấn nút
        setupClickListeners();

        // Khởi tạo các tham chiếu Firebase cho chức năng yêu thích
        setupFavoriteReferences();

        // Lắng nghe trạng thái yêu thích của người dùng và số lượt thích của phim
        checkInitialFavoriteStatus();
    }

    private boolean getIntentData() {
        videoUrl = getIntent().getStringExtra("VIDEO_URL");
        movieTitle = getIntent().getStringExtra("MOVIE_TITLE");
        movieId = getIntent().getStringExtra("MOVIE_ID"); // Rất quan trọng để lưu bình luận
        movieYear = getIntent().getStringExtra("MOVIE_YEAR");
        movieDuration = getIntent().getStringExtra("MOVIE_DURATION");

        return movieId != null && !movieId.isEmpty() && videoUrl != null && !videoUrl.isEmpty();
    }


    private void bindViews() {

        playerView = findViewById(R.id.video_view);
        progressBar = findViewById(R.id.progress_bar);
        backButton = findViewById(R.id.back_button);


        tvVideoTitle = findViewById(R.id.tv_video_title);
        tvYear = findViewById(R.id.tv_year);
        tvDuration = findViewById(R.id.tv_duration);


        recyclerViewComments = findViewById(R.id.recycler_view_comments);
        edtCommentInput = findViewById(R.id.edt_comment_input);
        btnSendComment = findViewById(R.id.btn_send_comment);

        btnAddFavorite = findViewById(R.id.btn_add_favorite);
        btnShare = findViewById(R.id.btn_share);
        tvLikesCount = findViewById(R.id.tv_likes_count);
    }

    private void setupFavoriteReferences() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && movieId != null) {

            movieLikesRef = FirebaseDatabase.getInstance().getReference("Movies").child(movieId).child("likesCount");


            userFavoriteStatusRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(currentUser.getUid()).child("favorites").child(movieId);
        }
    }

    private void checkInitialFavoriteStatus() {
        // 1. thay đổi của số lượt thích và cập nhật UI
        if (movieLikesRef != null) {
            movieLikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long likes = snapshot.getValue(Long.class);
                    if (likes != null) {
                        tvLikesCount.setText(formatLikesCount(likes));
                    } else {
                        tvLikesCount.setText("0");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        // 2.  trạng thái yêu thích của người dùng và cập nhật nút
        if (userFavoriteStatusRef != null) {
            userFavoriteStatusRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    isCurrentlyFavorite = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    updateFavoriteButtonUI(isCurrentlyFavorite);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void setupMovieInfo() {
        tvVideoTitle.setText(movieTitle);
        tvYear.setText(movieYear);
        tvDuration.setText(movieDuration);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> onBackPressed());
        btnSendComment.setOnClickListener(v -> postComment());
        btnAddFavorite.setOnClickListener(v -> toggleFavorite());
        btnShare.setOnClickListener(v -> shareMovie());
    }


    private void toggleFavorite() {
        if (userFavoriteStatusRef == null || movieLikesRef == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thực hiện", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sử dụng Transaction để đảm bảo tính toàn vẹn dữ liệu khi nhiều người cùng thích
        movieLikesRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Long likes = currentData.getValue(Long.class);
                if (likes == null) {
                    likes = 0L;
                }

                if (isCurrentlyFavorite) {
                    // Đang thích -> Bỏ thích
                    likes--;
                } else {
                    // Chưa thích -> Thích
                    likes++;
                }
                currentData.setValue(likes);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (committed) {
                    // Sau khi bộ đếm đã cập nhật thành công, cập nhật trạng thái của người dùng
                    if (isCurrentlyFavorite) {
                        userFavoriteStatusRef.removeValue(); // Bỏ thích
                    } else {
                        userFavoriteStatusRef.setValue(true); // Thích
                    }
                }
            }
        });
    }


    private void shareMovie() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        String shareBody = "Hãy xem bộ phim \"" + movieTitle + "\" trên ứng dụng của chúng tôi!\n" + videoUrl;
        String shareSubject = "Giới thiệu phim hay: " + movieTitle;

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

        startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
    }


    // THÊM HÀM MỚI NÀY (Hàm tiện ích để định dạng số lượt thích)
    private String formatLikesCount(long count) {
        if (count < 1000) {
            return String.valueOf(count);
        } else if (count < 1_000_000) {
            return String.format("%.1fk", count / 1000.0);
        } else {
            return String.format("%.1fM", count / 1_000_000.0);
        }
    }


    // THÊM HÀM MỚI NÀY (Hàm tiện ích để cập nhật giao diện nút)
    private void updateFavoriteButtonUI(boolean isFavorite) {
        if (isFavorite) {
            btnAddFavorite.setText("Đã thích");
            btnAddFavorite.setIconResource(R.drawable.ic_favorite_filled);
            // Có thể đổi màu ở đây nếu muốn
            // btnAddFavorite.setIconTint(ContextCompat.getColor(this, R.color.color_favorite_pink));
        } else {
            btnAddFavorite.setText("Yêu thích");
            btnAddFavorite.setIconResource(R.drawable.ic_favorite_border);
            // btnAddFavorite.setIconTint(ContextCompat.getColor(this, R.color.white));
        }
    }
    private void postComment() {
        String commentText = edtCommentInput.getText().toString().trim();
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Người dùng ẩn danh";
        String userAvatar = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "";
        long timestamp = System.currentTimeMillis();

        Comment comment = new Comment(userId, userName, userAvatar, commentText, timestamp);

        // Đẩy bình luận lên Firebase
        commentsRef.push().setValue(comment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(VideoPlayerActivity.this, "Đăng bình luận thành công", Toast.LENGTH_SHORT).show();
                edtCommentInput.setText(""); // Xóa nội dung đã nhập
                recyclerViewComments.smoothScrollToPosition(commentAdapter.getItemCount()); // Cuộn xuống bình luận mới nhất
            } else {
                Toast.makeText(VideoPlayerActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCommentSection() {
        // Trỏ đến nhánh bình luận của phim này trong Firebase
        commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(movieId);

        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));

        // Query để lấy bình luận, sắp xếp theo timestamp
        Query query = commentsRef.orderByChild("timestamp");

        FirebaseRecyclerOptions<Comment> options = new FirebaseRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class)
                .build();

        commentAdapter = new CommentAdapter(options);
        recyclerViewComments.setAdapter(commentAdapter);
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                progressBar.setVisibility(playbackState == Player.STATE_BUFFERING ? View.VISIBLE : View.GONE);
            }
        });

        player.prepare();
        player.play();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bắt đầu lắng nghe dữ liệu từ Firebase
        if (commentAdapter != null) {
            commentAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Dừng lắng nghe để tiết kiệm tài nguyên
        if (commentAdapter != null) {
            commentAdapter.stopListening();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
//VideoPlayerActivity