package com.example.firebase_demo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Movie> hotMovies;
    private Context context;

    // Constructor để nhận dữ liệu từ HomeFragment
    public BannerAdapter(Context context, List<Movie> hotMovies) {
        this.context = context;
        this.hotMovies = hotMovies;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item_banner.xml mà chúng ta đã tạo
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Movie movie = hotMovies.get(position);

        // Dùng Glide để tải ảnh poster từ URL vào ImageView
        Glide.with(context)
                .load(movie.getPosterUrl())
                .into(holder.bannerImage);

        // Xử lý sự kiện khi người dùng nhấn vào một banner phim
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("videoUrl", movie.getVideoUrl());
            intent.putExtra("title", movie.getTitle());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        // Trả về số lượng phim hot
        return hotMovies.size();
    }

    // Lớp ViewHolder để giữ các view của một item
    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.banner_image);
        }
    }
}
    