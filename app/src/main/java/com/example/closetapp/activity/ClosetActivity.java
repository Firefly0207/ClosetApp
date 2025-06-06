package com.example.closetapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.closetapp.R;
import com.example.closetapp.adapter.ClothAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.closetapp.model.Cloth;

public class ClosetActivity extends AppCompatActivity {
    private boolean isFavoriteSort = false;

    private RecyclerView recyclerView;
    private ClothAdapter adapter;
    private List<Cloth> clothList = new ArrayList<>();

    private FirebaseFirestore db;
    private CollectionReference clothesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closet);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 뒤로가기 버튼
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 기본 타이틀 제거
        }

        FloatingActionButton fabAdd = findViewById(R.id.fabAddCloth);
        FloatingActionButton fabMatch = findViewById(R.id.fabMatch);

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.closetapp.activity.ClothRegisterActivity.class));
        });

        fabMatch.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.closetapp.activity.MatchActivity.class));
        });


        // 하단 네비게이션
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_closet); // 현재 탭 선택 표시
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, com.example.closetapp.activity.MainActivity.class));
            } else if (id == R.id.nav_closet) {
                return true;
            } else if (id == R.id.nav_daily) {
                startActivity(new Intent(this, com.example.closetapp.activity.DailyFitActivity.class));
            } else if (id == R.id.nav_community) {
                startActivity(new Intent(this, com.example.closetapp.activity.CommunityActivity.class));
            } else if (id == R.id.nav_mypage) {
                startActivity(new Intent(this, com.example.closetapp.activity.MyPageActivity.class));
            }
            return true;
        });

        // Firebase 설정
        db = FirebaseFirestore.getInstance();
        clothesRef = db.collection("clothes");

        recyclerView = findViewById(R.id.closetListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClothAdapter(this, clothList);
        recyclerView.setAdapter(adapter);

        // 데이터 로드
        loadClothesFromFirebase();
    }

    private void loadClothesFromFirebase() {
        clothesRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Toast.makeText(this, "불러오기 실패", Toast.LENGTH_SHORT).show();
                return;
            }

            clothList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                Cloth cloth = doc.toObject(Cloth.class);
                cloth.setId(doc.getId());
                clothList.add(cloth);
            }

            adapter = new ClothAdapter(this, clothList);
            recyclerView.setAdapter(adapter);
        });
    }

    private void applyTagFilter(List<String> selectedTags) {
        if (selectedTags.isEmpty()) {
            adapter = new ClothAdapter(this, clothList);
            recyclerView.setAdapter(adapter);
            return;
        }

        List<Cloth> filteredList = new ArrayList<>();
        for (Cloth cloth : clothList) {
            if (cloth.getTagsList() != null && cloth.getTagsList().containsAll(selectedTags)) {
                filteredList.add(cloth);
            }
        }

        adapter = new ClothAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);
    }

    private void toggleFavoriteSort() {
        isFavoriteSort = !isFavoriteSort;

        List<Cloth> sortedList = new ArrayList<>(clothList);

        if (isFavoriteSort) {
            Collections.sort(sortedList, (c1, c2) -> Boolean.compare(!c1.isFavorite(), !c2.isFavorite()));
            Toast.makeText(this, "즐겨찾기순 정렬", Toast.LENGTH_SHORT).show();
        } else {
            Collections.sort(sortedList, (c1, c2) -> {
                if (c1.getTimestamp() == null || c2.getTimestamp() == null) return 0;
                return c2.getTimestamp().compareTo(c1.getTimestamp());
            });
            Toast.makeText(this, "최신순 정렬", Toast.LENGTH_SHORT).show();
        }

        adapter = new ClothAdapter(this, sortedList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_closet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_filter) {
            com.example.closetapp.dialog.FilterDialogFragment filterDialog = new com.example.closetapp.dialog.FilterDialogFragment(clothList, this::applyTagFilter);
            filterDialog.show(getSupportFragmentManager(), "FilterDialog");
            return true;
        } else if (item.getItemId() == R.id.menu_sort_favorite) {
            toggleFavoriteSort();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

