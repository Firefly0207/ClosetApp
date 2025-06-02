package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MyPageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    EditText userName;
    ImageView profileImage;
    Button logoutBtn, saveNameBtn;
    Uri imageUri;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        userName = findViewById(R.id.userName);
        profileImage = findViewById(R.id.profileImage);
        logoutBtn = findViewById(R.id.logoutBtn);
        saveNameBtn = findViewById(R.id.saveNameBtn);

        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // 앱 실행 시 저장된 닉네임 불러오기
        String savedName = prefs.getString("username", null);
        if (savedName != null) {
            userName.setText(savedName);
        }

        // 닉네임 저장 버튼 클릭 시 저장
        saveNameBtn.setOnClickListener(v -> {
            String newName = userName.getText().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("username", newName);
            editor.apply();
            Toast.makeText(this, "닉네임이 저장되었습니다", Toast.LENGTH_SHORT).show();
        });

        // 프로필 이미지 클릭 시 이미지 선택
        profileImage.setOnClickListener(v -> openImagePicker());

        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }
}
