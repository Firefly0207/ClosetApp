package com.example.closetapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private SharedPreferences prefs;

    private EditText emailEditText, passwordEditText;
    private CheckBox autoLoginCheckBox;
    private Button loginButton, signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase 인증 시스템
        auth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("AutoLoginPrefs", MODE_PRIVATE);

        //회원가입 각 구역
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        autoLoginCheckBox = findViewById(R.id.autoLoginCheckBox);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        // 자동 로그인 시도
        String savedEmail = prefs.getString("email", null);
        String savedPassword = prefs.getString("password", null);

        if (savedEmail != null && savedPassword != null) {
            login(savedEmail, savedPassword);
        }

        //로그인 버튼
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (autoLoginCheckBox.isChecked()) {
                prefs.edit()
                        .putString("email", email)
                        .putString("password", password)
                        .apply();
            }

            login(email, password);
        });

        //회원가입 버튼
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void login(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
