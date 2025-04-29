package com.example.closetapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, signupRedirectButton;
    private FirebaseAuth auth;
    private CheckBox autoLoginCheckBox;
    private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        autoLoginCheckBox = findViewById(R.id.autoLoginCheckBox);
        prefs = getSharedPreferences("AutoLogin", MODE_PRIVATE);

        // 자동 로그인 체크 되어 있으면 바로 로그인
        if (prefs.getBoolean("autoLogin", false) && auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
        if (autoLoginCheckBox.isChecked()) {
            prefs.edit().putBoolean("autoLogin", true).apply();
        } else {
            prefs.edit().putBoolean("autoLogin", false).apply();
        }
        // 전달된 이메일이 있다면 자동 채우기
        String emailFromSignup = getIntent().getStringExtra("email");
        if (emailFromSignup != null) {
            emailEditText.setText(emailFromSignup);
        }

        emailEditText = findViewById(R.id.loginEmailEditText);
        passwordEditText = findViewById(R.id.loginPasswordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupRedirectButton = findViewById(R.id.signupRedirectButton);

        loginButton.setOnClickListener(v -> loginUser());
        signupRedirectButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
