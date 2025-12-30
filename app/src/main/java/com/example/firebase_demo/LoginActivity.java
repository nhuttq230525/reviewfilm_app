package com.example.firebase_demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout txtEmail, txtPassword;
    Button btnLogin;
    FirebaseAuth auth;
    TextView txtCreateSign;
    CheckBox cbRememberMe;


    public static final String PREFS_NAME = "LoginPrefs";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASS = "password";
    public static final String KEY_REMEMBER = "remember";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtEmail = findViewById(R.id.emailLayout);
        txtPassword = findViewById(R.id.passwordLayout);
        btnLogin = findViewById(R.id.loginButton);
        txtCreateSign = findViewById(R.id.createSign);
        cbRememberMe = findViewById(R.id.rememberCheckBox);
        auth = FirebaseAuth.getInstance();
        loadLoginCredentials();
        btnLogin.setOnClickListener(v -> loginUser());
        txtCreateSign.setOnClickListener(v -> {
            navigateToSignUp();
        });
    }

    private void loginUser() {
        // Lấy nội dung người dùng nhập
        String email = txtEmail.getEditText().getText().toString().trim();
        String pass = txtPassword.getEditText().getText().toString().trim();

        // Kiểm tra nhập rỗng
        if (email.isEmpty()) {
            txtEmail.setError("Email không được để trống");
            return;
        } else {
            txtEmail.setError(null);
        }

        if (pass.isEmpty()) {
            txtPassword.setError("Mật khẩu không được để trống");
            return;
        } else {
            txtPassword.setError(null);
        }
        // 2. Xử lý lưu thông tin đăng nhập khi nhấn nút Login
        if (cbRememberMe.isChecked()) {
            // Nếu người dùng chọn "Ghi nhớ", lưu thông tin
            saveLoginCredentials(email, pass, true);
        } else {
            clearLoginCredentials();
        }
        // firebase
        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Sai thông tin đăng nhập!", Toast.LENGTH_SHORT).show();
                });
    }
    // Hàm lưu thông tin đăng nhập
    private void saveLoginCredentials(String email, String password, boolean remember) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASS, password);
        editor.putBoolean(KEY_REMEMBER, remember);
        editor.apply();
    }

    // Hàm tải thông tin đăng nhập
    private void loadLoginCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean shouldRemember = sharedPreferences.getBoolean(KEY_REMEMBER, false);

        if (shouldRemember) {
            String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
            String savedPass = sharedPreferences.getString(KEY_PASS, "");

            txtEmail.getEditText().setText(savedEmail);
            txtPassword.getEditText().setText(savedPass);
            cbRememberMe.setChecked(true);
        }
    }

    // Hàm xóa thông tin đăng nhập đã lưu
    private void clearLoginCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASS);
        editor.remove(KEY_REMEMBER);
        editor.apply(); // Lưu thay đổi
    }
    private void navigateToSignUp() {
        // Giả định class Đăng ký của bạn là SignupActivity
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
        // KHÔNG dùng finish() ở đây, để người dùng có thể quay lại Login
    }
}
//LoginActivity