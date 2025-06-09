package com.example.closetapp.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.tabs.TabLayout;
import com.example.closetapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.closetapp.model.Outfit;
import com.example.closetapp.adapter.OutfitAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDailyFitsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OutfitAdapter adapter;
    private List<Outfit> favoriteDailyFits = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_daily_fits);
        
        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        // 하단 네비게이션 설정
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
                startActivity(new Intent(this, MyPageActivity.class));
                return true;
            }
            return false;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OutfitAdapter(this, favoriteDailyFits);
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadFavoriteDailyFits();
        setupTabs();
    }
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.getTabAt(2).select();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        startActivity(new Intent(FavoriteDailyFitsActivity.this, FavoriteClothesActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(FavoriteDailyFitsActivity.this, FavoriteMatchesActivity.class));
                        break;
                    case 2:
                        // 현재 탭
                        break;
                    case 3:
                        startActivity(new Intent(FavoriteDailyFitsActivity.this, FavoritePostsActivity.class));
                        break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    private void loadFavoriteDailyFits() {
        db.collection("outfits")
          .whereEqualTo("userId", currentUserId)
          .whereEqualTo("favorite", true)
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              favoriteDailyFits.clear();
              for (var doc : queryDocumentSnapshots.getDocuments()) {
                  Outfit outfit = doc.toObject(Outfit.class);
                  if (outfit != null) {
                      outfit.setId(doc.getId());
                      favoriteDailyFits.add(outfit);
                  }
              }
              adapter.notifyDataSetChanged();
          });
    }
} 