package com.example.closetapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClothDetailActivity extends AppCompatActivity {

    private ImageView clothImageView;
    private TextView clothInfoTextView, clothDetailTextView;
    private Button deleteButton;
    private FirebaseFirestore db;
    private String clothId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth_detail);

        db = FirebaseFirestore.getInstance();

        // 툴바
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 뷰 바인딩
        clothImageView = findViewById(R.id.clothImageView);
        clothInfoTextView = findViewById(R.id.clothInfoTextView);
        clothDetailTextView = findViewById(R.id.clothDetailTextView);

        // 인텐트 데이터
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");
        String clothId = intent.getStringExtra("clothId");
        String info = intent.getStringExtra("info");

        Cloth cloth = (Cloth) intent.getSerializableExtra("cloth");

        if (cloth != null) {
            String detailText = String.format(
                    "- 분류: %s\n- 해시태그: %s\n- 세탁 정보: %s\n- 원단: %s\n- 관리법: %s\n- 마지막 착용일: %s",
                    cloth.getCategory(),
                    cloth.getTags(),
                    cloth.getWashInfo(),
                    cloth.getFabric(),
                    cloth.getCareInstructions(),
                    cloth.getLastWornDate() != null ? cloth.getLastWornDate() : "기록 없음"
            );
            clothDetailTextView.setText(detailText);
        }

        Glide.with(this).load(imageUrl).into(clothImageView);
        clothInfoTextView.setText(info);

        FloatingActionButton fabDelete = findViewById(R.id.fabDeleteCloth);
        fabDelete.setOnClickListener(v -> deleteCloth());

        FloatingActionButton fabEdit = findViewById(R.id.fabEditCloth);
        fabEdit.setOnClickListener(v -> {
            Intent editintent = new Intent(this, ClothRegisterActivity.class); // or ClothEditActivity if separated
            intent.putExtra("clothId", clothId);
            // cloth 객체 전체 넘기려면 Serializable or Parcelable 구현 필요
            startActivity(intent);
        });

        // 하단 네비게이션
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_closet); // 현재 페이지 표시
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_daily) {
                startActivity(new Intent(this, DailyFitActivity.class));
            } else if (id == R.id.nav_community) {
                startActivity(new Intent(this, CommunityActivity.class));
            } else if (id == R.id.nav_mypage) {
                startActivity(new Intent(this, MyPageActivity.class));
            }
            return true;
        });
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
                    finish();
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
