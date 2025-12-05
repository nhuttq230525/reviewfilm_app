package com.example.firebase_demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CategoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private ArrayList<Category> categoryList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout fragment_category.xml
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ RecyclerView từ layout
        recyclerView = view.findViewById(R.id.recyclerView_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo danh sách và adapter
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(getContext(), categoryList);
        recyclerView.setAdapter(categoryAdapter);

        // Tạo dữ liệu cho các thể loại
        loadCategories();
    }

    private void loadCategories() {
        categoryList.clear();

        // Thêm các thể loại vào danh sách.
        // QUAN TRỌNG: Bạn phải có các ảnh với tên tương ứng trong thư mục /res/drawable
        // Ví dụ: action.png, horror.png,...
        categoryList.add(new Category("Hành Động", R.drawable.bg_action));
        categoryList.add(new Category("Kinh Dị", R.drawable.bg_horror));
        categoryList.add(new Category("Tình Cảm", R.drawable.bg_romance));
        categoryList.add(new Category("Hài Hước", R.drawable.bg_comedy));
        categoryList.add(new Category("Hoạt Hình", R.drawable.bg_animation));


        // Thông báo cho adapter biết dữ liệu đã thay đổi để nó cập nhật lại giao diện
        categoryAdapter.notifyDataSetChanged();
    }
}

