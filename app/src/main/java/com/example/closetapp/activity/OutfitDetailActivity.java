package com.example.closetapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.closetapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.closetapp.adapter.ClothAdapter;
import com.example.closetapp.model.Outfit;
import com.example.closetapp.model.Cloth;

public class OutfitDetailActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String currentUserId;
    private String outfitId;

    private TextView dateTextView;
    private TextView descriptionTextView;
    private ImageButton favoriteButton;
    private RecyclerView clothesRecyclerView;
    private ClothAdapter clothesAdapter;
    private List<Cloth> clothesList;

    private Outfit currentOutfit;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_detail);

        // Firebase 초기화
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // 인텐트에서 outfitId 가져오기
        outfitId = getIntent().getStringExtra("outfitId");
        if (outfitId == null) {
            Toast.makeText(this, "코디 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // UI 요소 초기화
        dateTextView = findViewById(R.id.dateTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        favoriteButton = findViewById(R.id.favoriteButton);
        clothesRecyclerView = findViewById(R.id.clothesRecyclerView);

        // 날짜 포맷 설정
        dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);

        // 옷 목록 설정
        clothesList = new ArrayList<>();
        clothesAdapter = new ClothAdapter(this, clothesList);
        clothesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        clothesRecyclerView.setAdapter(clothesAdapter);

        // 즐겨찾기 버튼
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        // 코디 정보 로드
        loadOutfitDetails();
    }

    private void loadOutfitDetails() {
        db.collection("outfits").document(outfitId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentOutfit = documentSnapshot.toObject(Outfit.class);
                        if (currentOutfit != null) {
                            currentOutfit.setId(documentSnapshot.getId());
                            updateUI();
                            loadClothes();
                        }
                    } else {
                        Toast.makeText(this, "코디 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "코디 정보 로드 실패", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateUI() {
        dateTextView.setText(dateFormat.format(currentOutfit.getDate()));
        descriptionTextView.setText(currentOutfit.getDescription());
        favoriteButton.setImageResource(
            currentOutfit.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
    }

    private void loadClothes() {
        if (currentOutfit.getClothIds() == null || currentOutfit.getClothIds().isEmpty()) {
            return;
        }

        db.collection("clothes")
                .whereIn("id", currentOutfit.getClothIds())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    clothesList.clear();
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        Cloth cloth = doc.toObject(Cloth.class);
                        if (cloth != null) {
                            cloth.setId(doc.getId());
                            clothesList.add(cloth);
                        }
                    }
                    clothesAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "옷 정보 로드 실패", Toast.LENGTH_SHORT).show());
    }

    private void toggleFavorite() {
        if (currentOutfit == null) return;

        boolean newFavoriteState = !currentOutfit.isFavorite();
        db.collection("outfits").document(outfitId)
                .update("favorite", newFavoriteState)
                .addOnSuccessListener(aVoid -> {
                    currentOutfit.setFavorite(newFavoriteState);
                    updateUI();
                    Toast.makeText(this, 
                        newFavoriteState ? "즐겨찾기 추가" : "즐겨찾기 해제", 
                        Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "즐겨찾기 업데이트 실패", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 