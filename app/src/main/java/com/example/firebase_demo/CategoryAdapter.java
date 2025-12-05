package com.example.firebase_demo;

import android.content.Context;
import android.content.Intent;import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final ArrayList<Category> categoryList;

    public CategoryAdapter(Context context, ArrayList<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp layout item_category.xml mà bạn vừa tạo
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        // Lấy đối tượng Category ở vị trí hiện tại
        Category category = categoryList.get(position);

        // Gán dữ liệu cho các View
        holder.categoryTitle.setText(category.getTitle());

        // Dùng Glide để hiển thị ảnh từ drawable
        Glide.with(context)
                .load(category.getImageResource())
                .into(holder.categoryImage);

        // --- Xử lý sự kiện click vào một thể loại ---
        holder.itemView.setOnClickListener(v -> {
            // Tạo một Intent để mở MovieListActivity
            Intent intent = new Intent(context, MovieListActivity.class);

            // Gửi tên của thể loại được click qua cho MovieListActivity
            intent.putExtra("CATEGORY_NAME", category.getTitle());

            // Bắt đầu Activity mới
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // ViewHolder để giữ các View của item_category.xml
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImage;
        TextView categoryTitle;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các View từ layout
            categoryImage = itemView.findViewById(R.id.category_image);
            categoryTitle = itemView.findViewById(R.id.category_title);
        }
    }
}
