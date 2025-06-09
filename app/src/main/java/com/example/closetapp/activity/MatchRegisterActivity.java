package com.example.closetapp.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.example.closetapp.adapter.ClothAdapter;
import com.example.closetapp.model.Cloth;

public class MatchRegisterActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_match_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        db = FirebaseFirestore.getInstance();
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
    }

    private void loadClothes() {
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<String> extraImageUrls = new ArrayList<>();
        for (Cloth c : extraClothes) {
            extraImageUrls.add(c.getImageUrl());
        }
        android.widget.EditText editDescription = findViewById(R.id.editDescription);
        String description = editDescription.getText().toString();

        db.collection("matches")
                .whereEqualTo("topImageUrl", topUrl)
                .whereEqualTo("bottomImageUrl", bottomUrl)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isDuplicate = false;
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        List<String> dbExtra = (List<String>) doc.get("extraImageUrls");
                        if (dbExtra == null) dbExtra = new ArrayList<>();
                        if (dbExtra.size() == extraImageUrls.size() && dbExtra.containsAll(extraImageUrls) && extraImageUrls.containsAll(dbExtra)) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (isDuplicate) {
                        Toast.makeText(this, "이미 저장된 조합입니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Map<String, Object> matchData = new HashMap<>();
                        matchData.put("topImageUrl", topUrl);
                        matchData.put("bottomImageUrl", bottomUrl);
                        matchData.put("topCategory", selectedTop.getCategory());
                        matchData.put("bottomCategory", selectedBottom.getCategory());
                        matchData.put("timestamp", FieldValue.serverTimestamp());
                        matchData.put("userId", userId);
                        matchData.put("extraImageUrls", extraImageUrls);
                        List<String> extraCategories = new ArrayList<>();
                        for (Cloth c : extraClothes) {
                            extraCategories.add(c.getCategory());
                        }
                        matchData.put("extraCategories", extraCategories);
                        matchData.put("description", description);
                        db.collection("matches")
                                .add(matchData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "조합 즐겨찾기 저장 완료!", Toast.LENGTH_SHORT).show();
                                    finish();
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
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 