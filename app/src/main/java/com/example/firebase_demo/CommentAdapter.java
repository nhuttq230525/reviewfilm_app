package com.example.firebase_demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class CommentAdapter extends FirebaseRecyclerAdapter<Comment, CommentAdapter.CommentViewHolder> {

    public CommentAdapter(@NonNull FirebaseRecyclerOptions<Comment> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comment model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtUserName, txtContent;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_comment_avatar);
            txtUserName = itemView.findViewById(R.id.txt_comment_user_name);
            txtContent = itemView.findViewById(R.id.txt_comment_content);
        }

        void bind(Comment comment) {
            txtUserName.setText(comment.getUserName());
            txtContent.setText(comment.getContent());

            // Sử dụng Glide để tải ảnh đại diện
            if (comment.getUserAvatar() != null && !comment.getUserAvatar().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(comment.getUserAvatar())
                        .placeholder(R.drawable.ic_person) // Ảnh mặc định
                        .circleCrop() // Bo tròn ảnh
                        .into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_person);
            }
        }
    }
}
