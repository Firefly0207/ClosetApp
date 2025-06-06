package com.example.closetapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.closetapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import com.example.closetapp.adapter.OutfitAdapter;
import com.example.closetapp.model.Outfit;

public class MyPageActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String currentUserId;
    private TextView nameTextView, emailTextView;
    private Button logoutButton;
    private RecyclerView favoriteOutfitsRecyclerView;
    private OutfitAdapter favoriteOutfitsAdapter;
    private List<Outfit> favoriteOutfitsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        // Firebase 초기화
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // UI 요소 초기화
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        logoutButton = findViewById(R.id.logoutButton);
        favoriteOutfitsRecyclerView = findViewById(R.id.favoriteOutfitsRecyclerView);

        // 즐겨찾기 코디 리스트 설정
        favoriteOutfitsList = new ArrayList<>();
        favoriteOutfitsAdapter = new OutfitAdapter(this, favoriteOutfitsList);
        favoriteOutfitsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteOutfitsRecyclerView.setAdapter(favoriteOutfitsAdapter);

        // 사용자 정보 로드
        loadUserInfo();

        // 즐겨찾기 코디 로드
        loadFavoriteOutfits();

        // 로그아웃 버튼
        logoutButton.setOnClickListener(v -> logout());

        // 하단 네비게이션
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_mypage);
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
                return true;
            }
            return false;
        });
    }

    private void loadUserInfo() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        nameTextView.setText(name);
                        emailTextView.setText(email);
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "사용자 정보 로드 실패", Toast.LENGTH_SHORT).show());
    }

    private void loadFavoriteOutfits() {
        db.collection("outfits")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("favorite", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteOutfitsList.clear();
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        Outfit outfit = doc.toObject(Outfit.class);
                        if (outfit != null) {
                            outfit.setId(doc.getId());
                            favoriteOutfitsList.add(outfit);
                        }
                    }
                    favoriteOutfitsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "즐겨찾기 로드 실패", Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        auth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
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
