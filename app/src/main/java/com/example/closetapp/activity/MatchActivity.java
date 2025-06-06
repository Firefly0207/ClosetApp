package com.example.closetapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.closetapp.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.closetapp.adapter.ClothAdapter;
import com.example.closetapp.model.Cloth;

public class MatchActivity extends AppCompatActivity {

    private Spinner tagFilterSpinner;
    private RecyclerView topsRecyclerView, bottomsRecyclerView;
    private ClothAdapter topsAdapter, bottomsAdapter;
    private List<Cloth> topClothes = new ArrayList<>();
    private List<Cloth> bottomClothes = new ArrayList<>();

    private FirebaseFirestore db;

    private Cloth selectedTop;
    private Cloth selectedBottom;

    private List<Cloth> extraClothes = new ArrayList<>();
    private ClothAdapter extraClothesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();

        tagFilterSpinner = findViewById(R.id.tagFilterSpinner);
        topsRecyclerView = findViewById(R.id.topsRecyclerView);
        bottomsRecyclerView = findViewById(R.id.bottomsRecyclerView);

        topsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bottomsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        topsAdapter = new ClothAdapter(this, topClothes);
        bottomsAdapter = new ClothAdapter(this, bottomClothes);

        topsRecyclerView.setAdapter(topsAdapter);
        bottomsRecyclerView.setAdapter(bottomsAdapter);

        RecyclerView extraClothesRecyclerView = findViewById(R.id.extraClothesRecyclerView);
        extraClothesAdapter = new ClothAdapter(this, extraClothes, extraClothes);
        extraClothesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        extraClothesRecyclerView.setAdapter(extraClothesAdapter);

        Button btnAddMoreClothes = findViewById(R.id.btn_add_more_clothes);
        btnAddMoreClothes.setOnClickListener(v -> showAddMoreClothesDialog());

        Button btnRegisterMatch = findViewById(R.id.btn_register_match);
        btnRegisterMatch.setOnClickListener(v -> saveFavoriteMatch());

        loadClothes();

        topsAdapter.setOnItemClickListener(cloth -> {
            selectedTop = cloth;
            topsAdapter = new ClothAdapter(this, topClothes, java.util.Collections.singletonList(cloth));
            topsRecyclerView.setAdapter(topsAdapter);
            topsAdapter.setOnItemClickListener(this::onTopSelected);
        });

        bottomsAdapter.setOnItemClickListener(cloth -> {
            selectedBottom = cloth;
            bottomsAdapter = new ClothAdapter(this, bottomClothes, java.util.Collections.singletonList(cloth));
            bottomsRecyclerView.setAdapter(bottomsAdapter);
            bottomsAdapter.setOnItemClickListener(this::onBottomSelected);
        });

        // 하단 네비게이션 동작 추가
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_closet) {
                startActivity(new Intent(this, ClosetActivity.class));
                return true;
            } else if (id == R.id.nav_daily) {
                startActivity(new Intent(this, DailyFitActivity.class));
                return true;
            } else if (id == R.id.nav_community) {
                startActivity(new Intent(this, CommunityActivity.class));
                return true;
            } else if (id == R.id.nav_mypage) {
                startActivity(new Intent(this, MyPageActivity.class));
                return true;
            }
            return false;
        });

    }

    private void loadClothes() {
        // clothes 콜렉션에서 상의, 하의 나눠서 로드
        db.collection("clothes").get().addOnSuccessListener(queryDocumentSnapshots -> {
            topClothes.clear();
            bottomClothes.clear();
            for (var doc : queryDocumentSnapshots) {
                Cloth cloth = doc.toObject(Cloth.class);
                if (cloth.getCategory() == null) continue;

                if (cloth.getCategory().contains("상의")) {
                    topClothes.add(cloth);
                } else if (cloth.getCategory().contains("하의")) {
                    bottomClothes.add(cloth);
                }
            }
            topsAdapter.notifyDataSetChanged();
            bottomsAdapter.notifyDataSetChanged();
        });
    }

    private void onTopSelected(Cloth cloth) {
        selectedTop = cloth;
        topsAdapter = new ClothAdapter(this, topClothes, java.util.Collections.singletonList(cloth));
        topsRecyclerView.setAdapter(topsAdapter);
        topsAdapter.setOnItemClickListener(this::onTopSelected);
    }
    private void onBottomSelected(Cloth cloth) {
        selectedBottom = cloth;
        bottomsAdapter = new ClothAdapter(this, bottomClothes, java.util.Collections.singletonList(cloth));
        bottomsRecyclerView.setAdapter(bottomsAdapter);
        bottomsAdapter.setOnItemClickListener(this::onBottomSelected);
    }

    private void showAddMoreClothesDialog() {
        db.collection("clothes").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Cloth> allClothes = new ArrayList<>();
            for (var doc : queryDocumentSnapshots) {
                Cloth cloth = doc.toObject(Cloth.class);
                if (cloth.getCategory() == null) continue;
                allClothes.add(cloth);
            }
            boolean[] checked = new boolean[allClothes.size()];
            for (int i = 0; i < allClothes.size(); i++) {
                checked[i] = extraClothes.contains(allClothes.get(i));
            }
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_image, null);
            RecyclerView recyclerView = dialogView.findViewById(R.id.grid_view);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            ClothAdapter dialogAdapter = new ClothAdapter(this, allClothes, extraClothes);
            recyclerView.setAdapter(dialogAdapter);

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("추가로 선택할 옷들")
                    .setView(dialogView)
                    .setPositiveButton("확인", (dialog, which) -> {
                        extraClothes.clear();
                        for (int i = 0; i < allClothes.size(); i++) {
                            if (dialogAdapter.getSelectedClothes().contains(allClothes.get(i))) {
                                extraClothes.add(allClothes.get(i));
                            }
                        }
                        extraClothesAdapter.notifyDataSetChanged();
                        findViewById(R.id.extraClothesRecyclerView).setVisibility(extraClothes.isEmpty() ? View.GONE : View.VISIBLE);
                    })
                    .setNegativeButton("취소", null)
                    .create();

            dialogAdapter.setOnItemClickListener(cloth -> {
                dialogAdapter.toggleClothSelection(cloth);
                dialogAdapter.notifyDataSetChanged();
            });

            alertDialog.show();
        });
    }

    private void saveFavoriteMatch() {
        if (selectedTop == null || selectedBottom == null) {
            Toast.makeText(this, "상의와 하의를 모두 선택하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String topUrl = selectedTop.getImageUrl();
        String bottomUrl = selectedBottom.getImageUrl();

        // 1. 저장 전에 같은 조합이 이미 있는지 검사
        db.collection("matches")
                .whereEqualTo("topImageUrl", topUrl)
                .whereEqualTo("bottomImageUrl", bottomUrl)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // 2. 이미 같은 조합이 있음
                        Toast.makeText(this, "이미 저장된 조합입니다", Toast.LENGTH_SHORT).show();
                    } else {
                        // 3. 새로운 조합 저장
                        Map<String, Object> matchData = new HashMap<>();
                        matchData.put("topImageUrl", topUrl);
                        matchData.put("bottomImageUrl", bottomUrl);
                        matchData.put("topCategory", selectedTop.getCategory());
                        matchData.put("bottomCategory", selectedBottom.getCategory());
                        matchData.put("timestamp", FieldValue.serverTimestamp());
                        // Firebase Auth에서 userId 가져오기
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        matchData.put("userId", userId); // userId 추가

                        // 추가 옷 이미지/카테고리 저장
                        List<String> extraImageUrls = new ArrayList<>();
                        List<String> extraCategories = new ArrayList<>();
                        for (Cloth c : extraClothes) {
                            extraImageUrls.add(c.getImageUrl());
                            extraCategories.add(c.getCategory());
                        }
                        matchData.put("extraImageUrls", extraImageUrls);
                        matchData.put("extraCategories", extraCategories);

                        db.collection("matches")
                                .add(matchData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "조합 즐겨찾기 저장 완료!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "중복 검사 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
