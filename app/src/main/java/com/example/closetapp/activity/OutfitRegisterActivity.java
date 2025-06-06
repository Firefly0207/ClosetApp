package com.example.closetapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.closetapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.*;
import com.example.closetapp.adapter.SelectedClothAdapter;
import com.example.closetapp.model.Cloth;
import com.example.closetapp.activity.CalendarActivity;
import com.example.closetapp.activity.ClothSelectActivity;

public class OutfitRegisterActivity extends AppCompatActivity {
    private static final int REQUEST_CALENDAR = 1;
    private static final int REQUEST_SELECT_CLOTHES = 2;

    private TextView dateTextView;
    private RecyclerView selectedClothesRecyclerView;
    private Button addClothesButton, registerButton;
    private TextInputEditText descriptionEditText;
    private CheckBox favoriteCheckBox;

    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
    private List<Cloth> selectedClothes = new ArrayList<>();
    private SelectedClothAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_register);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // UI 요소 초기화
        dateTextView = findViewById(R.id.dateTextView);
        selectedClothesRecyclerView = findViewById(R.id.selectedClothesRecyclerView);
        addClothesButton = findViewById(R.id.addClothesButton);
        registerButton = findViewById(R.id.registerButton);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        favoriteCheckBox = findViewById(R.id.favoriteCheckBox);

        // RecyclerView 설정
        selectedClothesRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new SelectedClothAdapter(this, selectedClothes);
        selectedClothesRecyclerView.setAdapter(adapter);

        // edit 모드 처리
        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        if ("edit".equals(mode)) {
            ArrayList<Cloth> editClothes = (ArrayList<Cloth>) intent.getSerializableExtra("selectedClothes");
            if (editClothes != null) {
                selectedClothes.clear();
                selectedClothes.addAll(editClothes);
                adapter.updateClothes(selectedClothes);
            }
            String dateStr = intent.getStringExtra("date");
            if (dateStr != null) {
                try {
                    selectedDate.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr));
                } catch (Exception e) { /* 무시 */ }
                dateTextView.setText(dateFormat.format(selectedDate.getTime()));
            }
            String desc = intent.getStringExtra("description");
            if (desc != null) descriptionEditText.setText(desc);
            boolean fav = intent.getBooleanExtra("favorite", false);
            favoriteCheckBox.setChecked(fav);
            registerButton.setText("수정하기");
        } else {
            // 추가 모드: date 인텐트 값이 있으면 기본값으로 설정
            String dateStr = intent.getStringExtra("date");
            if (dateStr != null) {
                try {
                    selectedDate.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr));
                } catch (Exception e) { /* 무시 */ }
                dateTextView.setText(dateFormat.format(selectedDate.getTime()));
            }
        }

        // 날짜 선택
        dateTextView.setText(dateFormat.format(selectedDate.getTime()));
        dateTextView.setOnClickListener(v -> {
            Intent calIntent = new Intent(this, CalendarActivity.class);
            startActivityForResult(calIntent, REQUEST_CALENDAR);
        });

        // 옷 추가 버튼
        addClothesButton.setOnClickListener(v -> {
            Intent selIntent = new Intent(this, ClothSelectActivity.class);
            selIntent.putExtra("selectedClothes", new ArrayList<>(selectedClothes));
            startActivityForResult(selIntent, REQUEST_SELECT_CLOTHES);
        });

        // 선택된 옷 제거
        adapter.setOnRemoveClickListener(position -> {
            selectedClothes.remove(position);
            adapter.updateClothes(selectedClothes);
        });

        // 등록/수정 버튼
        registerButton.setOnClickListener(v -> {
            if ("edit".equals(mode)) {
                updateOutfit();
            } else {
                registerOutfit();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CALENDAR && resultCode == Activity.RESULT_OK && data != null) {
            long selectedDateMillis = data.getLongExtra("selectedDate", System.currentTimeMillis());
            selectedDate.setTimeInMillis(selectedDateMillis);
            dateTextView.setText(dateFormat.format(selectedDate.getTime()));
        } else if (requestCode == REQUEST_SELECT_CLOTHES && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<Cloth> newSelectedClothes = (ArrayList<Cloth>) data.getSerializableExtra("selectedClothes");
            if (newSelectedClothes != null) {
                selectedClothes.clear();
                selectedClothes.addAll(newSelectedClothes);
                adapter.updateClothes(selectedClothes);
            }
        }
    }

    private void registerOutfit() {
        if (selectedClothes.isEmpty()) {
            Toast.makeText(this, "최소 한 개 이상의 옷을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = descriptionEditText.getText().toString().trim();
        boolean isFavorite = favoriteCheckBox.isChecked();

        // 선택된 옷들의 ID 리스트 생성
        List<String> clothIds = new ArrayList<>();
        for (Cloth cloth : selectedClothes) {
            clothIds.add(cloth.getId());
        }

        // Firebase Auth에서 userId 가져오기
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Firestore에 저장
        Map<String, Object> outfitData = new HashMap<>();
        outfitData.put("date", new com.google.firebase.Timestamp(selectedDate.getTimeInMillis() / 1000, 0));  // Timestamp로 저장
        outfitData.put("clothIds", clothIds);
        outfitData.put("description", description);
        outfitData.put("favorite", isFavorite);
        outfitData.put("timestamp", new com.google.firebase.Timestamp(Calendar.getInstance().getTimeInMillis() / 1000, 0));
        outfitData.put("userId", userId);

        db.collection("outfits")
                .add(outfitData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "코디가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "코디 등록 실패: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateOutfit() {
        Intent intent = getIntent();
        String outfitId = intent.getStringExtra("outfitId");
        if (outfitId == null) return;
        if (selectedClothes.isEmpty()) {
            Toast.makeText(this, "최소 한 개 이상의 옷을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        String description = descriptionEditText.getText().toString().trim();
        boolean isFavorite = favoriteCheckBox.isChecked();

        List<String> clothIds = new ArrayList<>();
        for (Cloth cloth : selectedClothes) {
            clothIds.add(cloth.getId());
        }

        Map<String, Object> outfitData = new HashMap<>();
        outfitData.put("date", new com.google.firebase.Timestamp(selectedDate.getTimeInMillis() / 1000, 0));  // Timestamp로 저장
        outfitData.put("clothIds", clothIds);
        outfitData.put("description", description);
        outfitData.put("favorite", isFavorite);
        outfitData.put("timestamp", new com.google.firebase.Timestamp(Calendar.getInstance().getTimeInMillis() / 1000, 0));

        db.collection("outfits").document(outfitId)
                .update(outfitData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "코디가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "코디 수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
} 