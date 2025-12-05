package com.example.firebase_demo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import com.google.android.material.textfield.TextInputEditText;
import android.content.Intent;

public class SignupActivity extends AppCompatActivity {

    TextInputEditText textPersonName, emailEditText, passwordEditText;
    Button signButton;
    TextView createSign;
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    private static final String PREFS = "USER_DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);

        textPersonName = findViewById(R.id.textPersonName);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signButton = findViewById(R.id.signButton);
        createSign = findViewById(R.id.createSign);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        signButton.setOnClickListener(v -> registerUser());
        createSign.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class))
        );
    }

    private void registerUser() {
        String name = textPersonName.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        //  LOGIC FIREBASE THAY THẾ CHO SHARED PREFERENCES
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng ký thành công - Tự động đăng nhập và chuyển hướng
                        Toast.makeText(SignupActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                        //
                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                        // Dùng cờ để xóa các màn hình đăng ký/đăng nhập khỏi lịch sử
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        // Đăng ký thất bại (ví dụ: email đã tồn tại, mật khẩu quá yếu)
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi đăng ký không xác định.";
                        Toast.makeText(SignupActivity.this, "Đăng ký thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}