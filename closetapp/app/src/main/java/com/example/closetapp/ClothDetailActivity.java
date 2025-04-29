package com.example.closetapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClothDetailActivity extends AppCompatActivity {

    private ImageView clothImageView;
    private TextView clothInfoTextView;
    private Button deleteButton, homeButton, addClothButton, gotoMatchButton;

    private FirebaseFirestore db;
    private String clothId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth_detail);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        clothImageView = findViewById(R.id.clothImageView);
        clothInfoTextView = findViewById(R.id.clothInfoTextView);
        deleteButton = findViewById(R.id.deleteButton);

        homeButton = findViewById(R.id.homeButton);
        addClothButton = findViewById(R.id.addClothButton);
        gotoMatchButton = findViewById(R.id.gotoMatchButton);

        // 받아온 데이터
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");
        String info = intent.getStringExtra("info");
        clothId = intent.getStringExtra("clothId");

        // 이미지, 텍스트 세팅
        Glide.with(this).load(imageUrl).into(clothImageView);
        clothInfoTextView.setText(info);

        deleteButton.setOnClickListener(v -> deleteCloth());

        // 하단 버튼
        homeButton.setOnClickListener(v -> finish());
        addClothButton.setOnClickListener(v -> startActivity(new Intent(this, ClothRegisterActivity.class)));
        gotoMatchButton.setOnClickListener(v -> startActivity(new Intent(this, MatchActivity.class)));
    }

    private void deleteCloth() {
        if (clothId == null) {
            Toast.makeText(this, "옷 ID 없음", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("clothes").document(clothId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "옷 삭제 완료!", Toast.LENGTH_SHORT).show();
                    finish(); // 삭제 후 닫기
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
