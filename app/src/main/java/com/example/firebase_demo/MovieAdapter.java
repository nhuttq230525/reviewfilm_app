package com.example.firebase_demo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Thư viện để lấy màu
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth; // Thư viện để xác thực và lấy người dùng
import com.google.firebase.auth.FirebaseUser; // Thư viện để lấy người dùng
import com.google.firebase.database.DatabaseReference; // Thư viện để tương tác với Realtime DB
import com.google.firebase.database.FirebaseDatabase; // Thư viện để tương tác với Realtime DB

import java.util.ArrayList;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final ArrayList<Movie> movieList;
    private final Context context;

    public MovieAdapter(ArrayList<Movie> movieList, Context context) {
        this.movieList = movieList;
        this.context = context;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.txtTitle.setText(movie.getTitle());
        holder.txtDescription.setText(movie.getDescription());

        Glide.with(context)
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.imgPoster);

        setupReadMore(holder.txtDescription, holder.txtReadMore);

        holder.txtWatchVideo.setOnClickListener(v -> {
            String videoUrl = movie.getVideoUrl();
            // Kiểm tra xem có URL video không
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                // Bắt buộc:
                intent.putExtra("MOVIE_ID", movie.getId());
                intent.putExtra("VIDEO_URL", videoUrl);

                // Khuyến khích (để hiển thị đầy đủ thông tin):
                intent.putExtra("MOVIE_TITLE", movie.getTitle());
                // Giả sử Movie object của bạn có các trường này. Nếu không, bạn có thể truyền chuỗi rỗng ""
                intent.putExtra("MOVIE_YEAR", movie.getYear()); // Cần có movie.getYear()
                intent.putExtra("MOVIE_DURATION", movie.getDuration()); // Cần có movie.getDuration()
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Link phim không có sẵn", Toast.LENGTH_SHORT).show();
            }
        });

        // --- PHẦN CODE CHỨC NĂNG YÊU THÍCH
        if (movie.isFavorite()) {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            holder.btnFavorite.setColorFilter(ContextCompat.getColor(context, R.color.color_favorite_pink));
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            holder.btnFavorite.setColorFilter(ContextCompat.getColor(context, R.color.white));
        }

        holder.btnFavorite.setOnClickListener(v -> {
            toggleFavoriteStatus(movie, position);
        });
    }
    private void toggleFavoriteStatus(Movie movie, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (currentUser == null) {
            Toast.makeText(context, "Vui lòng đăng nhập để sử dụng chức năng này", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tham chiếu đến nút "favorites" của người dùng trên Firebase
        // Cấu trúc: Users -> {UserID} -> favorites -> {MovieID}
        DatabaseReference userFavoritesRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(currentUser.getUid())
                .child("favorites")
                .child(movie.getId()); // ID của phim là khóa

        if (movie.isFavorite()) {
            // Nếu phim đang được yêu thích -> Bỏ yêu thích (xóa khỏi Firebase)
            userFavoritesRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    movie.setFavorite(false);
                    notifyItemChanged(position); // Cập nhật chỉ item này, hiệu quả hơn
                    Toast.makeText(context, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Nếu phim chưa được yêu thích -> Thêm vào yêu thích (lưu giá trị true)
            userFavoritesRef.setValue(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    movie.setFavorite(true);
                    notifyItemChanged(position); // Cập nhật chỉ item này
                    Toast.makeText(context, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void setupReadMore(TextView txtDescription, TextView txtReadMore) {
        txtDescription.post(() -> {
            if (txtDescription.getLineCount() > 3) {
                txtReadMore.setVisibility(View.VISIBLE);
                txtDescription.setMaxLines(3);
                txtReadMore.setOnClickListener(v -> {
                    if (txtDescription.getMaxLines() == 3) {
                        txtDescription.setMaxLines(Integer.MAX_VALUE);
                        txtReadMore.setText("Thu gọn");
                    } else {
                        txtDescription.setMaxLines(3);
                        txtReadMore.setText("Xem thêm");
                    }
                });
            } else {
                txtReadMore.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }
    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        ImageView btnFavorite;
        TextView txtTitle, txtDescription, txtReadMore, txtWatchVideo;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtReadMore = itemView.findViewById(R.id.txtReadMore);
            txtWatchVideo = itemView.findViewById(R.id.txtWatchVideo);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}
//MovieAdapter