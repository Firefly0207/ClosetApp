package com.example.closetapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.closetapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;
import com.example.closetapp.adapter.ClothAdapter;
import com.example.closetapp.model.Cloth;

public class ClothSelectActivity extends AppCompatActivity {
    private RecyclerView clothRecyclerView;
    private ClothAdapter adapter;
    private List<Cloth> clothList = new ArrayList<>();
    private List<Cloth> selectedClothes = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth_select);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 이전에 선택된 옷들 가져오기
        ArrayList<Cloth> previousSelected = (ArrayList<Cloth>) getIntent().getSerializableExtra("selectedClothes");
        if (previousSelected != null) {
            selectedClothes.addAll(previousSelected);
        }

        // RecyclerView 설정
        clothRecyclerView = findViewById(R.id.clothRecyclerView);
        clothRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new ClothAdapter(this, clothList, selectedClothes);
        clothRecyclerView.setAdapter(adapter);

        // 옷 선택 리스너
        adapter.setOnItemClickListener(cloth -> {
            if (selectedClothes.contains(cloth)) {
                selectedClothes.remove(cloth);
            } else {
                if (!selectedClothes.contains(cloth)) {
                    selectedClothes.add(cloth);
                }
            }
            adapter.notifyDataSetChanged();
        });

        // 데이터 로드
        loadClothes();
    }

    private void loadClothes() {
        db.collection("clothes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    clothList.clear();
                    for (var doc : queryDocumentSnapshots) {
                        Cloth cloth = doc.toObject(Cloth.class);
                        cloth.setId(doc.getId());
                        clothList.add(cloth);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "옷 목록 로드 실패: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cloth_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_done) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedClothes", new ArrayList<>(selectedClothes));
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 