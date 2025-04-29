package com.example.closetapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchActivity extends AppCompatActivity {

    private Spinner tagFilterSpinner;
    private RecyclerView topsRecyclerView, bottomsRecyclerView;
    private ClothAdapter topsAdapter, bottomsAdapter;
    private List<Cloth> topClothes = new ArrayList<>();
    private List<Cloth> bottomClothes = new ArrayList<>();
    private Button favoriteMatchButton;

    private FirebaseFirestore db;

    private Cloth selectedTop;
    private Cloth selectedBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.homeButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        findViewById(R.id.addClothButton).setOnClickListener(v -> {
            startActivity(new Intent(this, ClothRegisterActivity.class));
        });
        findViewById(R.id.gotoMatchButton).setOnClickListener(v -> {
            // 현재 화면
        });

        db = FirebaseFirestore.getInstance();

        tagFilterSpinner = findViewById(R.id.tagFilterSpinner);
        topsRecyclerView = findViewById(R.id.topsRecyclerView);
        bottomsRecyclerView = findViewById(R.id.bottomsRecyclerView);
        favoriteMatchButton = findViewById(R.id.favoriteMatchButton);

        topsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bottomsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        topsAdapter = new ClothAdapter(this, topClothes);
        bottomsAdapter = new ClothAdapter(this, bottomClothes);

        topsRecyclerView.setAdapter(topsAdapter);
        bottomsRecyclerView.setAdapter(bottomsAdapter);

        loadClothes();

        favoriteMatchButton.setOnClickListener(v -> saveFavoriteMatch());
        topsAdapter.setOnItemClickListener(cloth -> {
            selectedTop = cloth;
            Toast.makeText(this, "상의 선택됨", Toast.LENGTH_SHORT).show();
        });

        bottomsAdapter.setOnItemClickListener(cloth -> {
            selectedBottom = cloth;
            Toast.makeText(this, "하의 선택됨", Toast.LENGTH_SHORT).show();
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
