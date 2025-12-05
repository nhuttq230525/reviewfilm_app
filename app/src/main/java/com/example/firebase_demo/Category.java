package com.example.firebase_demo;

public class Category {
    // Thuộc tính để lưu tên thể loại (ví dụ: "Hành Động", "Kinh Dị")
    private String title;

    // Thuộc tính để lưu ID của ảnh trong thư mục drawable
    private int imageResource;

    // Constructor rỗng - BẮT BUỘC phải có để Firebase hoạt động nếu cần
    public Category() {
    }

    // Constructor để chúng ta có thể tạo đối tượng dễ dàng trong code
    public Category(String title, int imageResource) {
        this.title = title;
        this.imageResource = imageResource;
    }

    // --- CÁC PHƯƠNG THỨC GETTER VÀ SETTER ---

    // Phương thức để lấy tên thể loại (Adapter sẽ dùng)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Phương thức để lấy ID ảnh (Adapter sẽ dùng)
    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
