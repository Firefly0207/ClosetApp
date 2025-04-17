package com.example.closetapp;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, nameEditText;
    private Button signupButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Firebase 인증 시스템
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //회원가입 구조
        emailEditText = findViewById(R.id.signupEmailEditText);
        passwordEditText = findViewById(R.id.signupPasswordEditText);
        nameEditText = findViewById(R.id.signupNameEditText);
        signupButton = findViewById(R.id.signupButton);

        // 회원가입 버튼
        signupButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();

            if (!validateInputs(email, password, name)) return;

            registerUser(email, password, name);
        });
    }

    private boolean validateInputs(String email, String password, String name) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("유효한 이메일을 입력해주세요.");
            emailEditText.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("비밀번호는 6자 이상이어야 합니다.");
            passwordEditText.requestFocus();
            return false;
        }

        if (name.isEmpty()) {
            nameEditText.setError("이름을 입력해주세요.");
            nameEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser(String email, String password, String name) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = auth.getCurrentUser().getUid();

                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("email", email);
                    userInfo.put("name", name);
                    userInfo.put("created_at", Timestamp.now());

                    db.collection("users").document(uid)
                            .set(userInfo)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(SignupActivity.this, "회원가입 완료!", Toast.LENGTH_SHORT).show();
                                finish(); // 로그인 화면으로 돌아감
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(SignupActivity.this, "사용자 정보 저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                })
                .addOnFailureListener(e -> {
                    String msg;
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        msg = "이미 존재하는 이메일입니다.";
                    } else {
                        msg = "회원가입 실패: " + e.getMessage();
                    }
                    Toast.makeText(SignupActivity.this, msg, Toast.LENGTH_LONG).show();
                });
    }
}
