package com.example.closetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClosetActivity extends AppCompatActivity {
    private boolean isFavoriteSort = false; // 정렬 상태 기억
    private RecyclerView recyclerView;
    private ClothAdapter adapter;
    private List<Cloth> clothList = new ArrayList<>();

    private Button homeButton, addClothButton, gotoMatchButton;

    private FirebaseFirestore db;
    private CollectionReference clothesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closet);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 뒤로가기 버튼 추가
        }

        db = FirebaseFirestore.getInstance();
        clothesRef = db.collection("clothes");

        recyclerView = findViewById(R.id.closetListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        homeButton = findViewById(R.id.homeButton);
        addClothButton = findViewById(R.id.addClothButton);
        gotoMatchButton = findViewById(R.id.gotoMatchButton);

        adapter = new ClothAdapter(this, clothList);
        recyclerView.setAdapter(adapter);

        // 버튼들
        addClothButton.setOnClickListener(v -> {
            Intent intent = new Intent(ClosetActivity.this, ClothRegisterActivity.class);
            startActivity(intent);
        });

        gotoMatchButton.setOnClickListener(v -> {
            Intent intent = new Intent(ClosetActivity.this, MatchActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.homeButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // 옷 데이터 불러오기
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

            adapter.notifyDataSetChanged();
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


    // 필터 아이콘 메뉴 추가
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_closet, menu);
        return true;
    }
    private void toggleFavoriteSort() {
        isFavoriteSort = !isFavoriteSort;

        List<Cloth> sortedList = new ArrayList<>(clothList);

        if (isFavoriteSort) {
            // 즐겨찾기 true를 위로
            Collections.sort(sortedList, (c1, c2) -> Boolean.compare(!c1.isFavorite(), !c2.isFavorite()));
            Toast.makeText(this, "즐겨찾기순 정렬", Toast.LENGTH_SHORT).show();
        } else {
            // 기본 순서 (timestamp순)
            Collections.sort(sortedList, (c1, c2) -> {
                if (c1.getTimestamp() == null || c2.getTimestamp() == null) return 0;
                return c2.getTimestamp().compareTo(c1.getTimestamp()); // 최신순
            });
            Toast.makeText(this, "최신순 정렬", Toast.LENGTH_SHORT).show();
        }

        adapter = new ClothAdapter(this, sortedList);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_filter) {
            FilterDialogFragment filterDialog = new FilterDialogFragment(clothList, selectedTags -> {
                applyTagFilter(selectedTags);
            });
            filterDialog.show(getSupportFragmentManager(), "FilterDialog");
            return true;
        } else if (item.getItemId() == R.id.menu_sort_favorite) {
            toggleFavoriteSort();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
