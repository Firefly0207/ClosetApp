package com.example.closetapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClothRegisterActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;

    private ImageView clothImageView;
    private Button selectImageButton, registerButton;
    private Spinner categorySpinner;
    private EditText tagEditText, fabricEditText, washInfoEditText, careInstructionsEditText, lastWornDateEditText;
    private CheckBox favoriteCheckBox;

    private Uri selectedImageUri;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth_register);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // 뷰 바인딩
        clothImageView = findViewById(R.id.clothImageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        registerButton = findViewById(R.id.registerButton);
        categorySpinner = findViewById(R.id.categorySpinner);
        tagEditText = findViewById(R.id.tagEditText);
        favoriteCheckBox = findViewById(R.id.favoriteCheckBox);

        fabricEditText = findViewById(R.id.fabricEditText);
        washInfoEditText = findViewById(R.id.washInfoEditText);
        careInstructionsEditText = findViewById(R.id.careInstructionsEditText);
        lastWornDateEditText = findViewById(R.id.lastWornDateEditText);

        // 카테고리 스피너
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.cloth_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // 이미지 선택
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        // 등록 버튼
        registerButton.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "사진을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = categorySpinner.getSelectedItem().toString();
            String tagsText = tagEditText.getText().toString();
            boolean isFavorite = favoriteCheckBox.isChecked();
            List<String> tagList = Arrays.asList(tagsText.split("\\s*,\\s*"));

            String fabric = fabricEditText.getText().toString();
            String washInfo = washInfoEditText.getText().toString();
            String careInstructions = careInstructionsEditText.getText().toString();
            String lastWornDate = lastWornDateEditText.getText().toString();

            // Firebase Storage 저장
            String fileName = "cloth_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child("clothes/" + fileName);

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();

                                Map<String, Object> clothData = new HashMap<>();
                                clothData.put("imageUrl", imageUrl);
                                clothData.put("category", category);
                                clothData.put("tags", tagList);
                                clothData.put("favorite", isFavorite);
                                clothData.put("fabric", fabric);
                                clothData.put("washInfo", washInfo);
                                clothData.put("careInstructions", careInstructions);
                                clothData.put("lastWornDate", lastWornDate);
                                clothData.put("timestamp", FieldValue.serverTimestamp());

                                db.collection("clothes")
                                        .add(clothData)
                                        .addOnSuccessListener(documentReference -> {
                                            String docId = documentReference.getId();
                                            db.collection("clothes").document(docId).update("id", docId);
                                            Toast.makeText(this, "등록 완료!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Firestore 저장 실패", Toast.LENGTH_SHORT).show());
                            }))
                    .addOnFailureListener(e -> Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_SHORT).show());
        });

        // 하단 네비게이션
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_closet);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_closet) {
                return true;
            } else if (id == R.id.nav_daily) {
                startActivity(new Intent(this, DailyFitActivity.class));
            } else if (id == R.id.nav_community) {
                startActivity(new Intent(this, CommunityActivity.class));
            } else if (id == R.id.nav_mypage) {
                startActivity(new Intent(this, MyPageActivity.class));
            }
            return true;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                clothImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
