package com.example.closetapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    private EditText tagEditText;
    private CheckBox favoriteCheckBox;

    private Uri selectedImageUri;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth_register);

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
            // 현재 화면
        });
        findViewById(R.id.gotoMatchButton).setOnClickListener(v -> {
            startActivity(new Intent(this, MatchActivity.class));
        });
        clothImageView = findViewById(R.id.clothImageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        registerButton = findViewById(R.id.registerButton);
        categorySpinner = findViewById(R.id.categorySpinner);
        tagEditText = findViewById(R.id.tagEditText);
        favoriteCheckBox = findViewById(R.id.favoriteCheckBox);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // 옷 분류 스피너 초기화
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.cloth_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // 사진 선택 버튼 클릭
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        // 등록 버튼 클릭
        registerButton.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "사진을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = categorySpinner.getSelectedItem().toString();
            String tagsText = tagEditText.getText().toString();
            boolean isFavorite = favoriteCheckBox.isChecked();
            List<String> tagList = Arrays.asList(tagsText.split("\\s*,\\s*"));

            // 1. 사진 Firebase Storage에 업로드
            String fileName = "cloth_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child("clothes/" + fileName);

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();

                                // 2. Firestore에 데이터 저장
                                Map<String, Object> clothData = new HashMap<>();
                                clothData.put("imageUrl", imageUrl);
                                clothData.put("category", category);
                                clothData.put("tags", tagList);
                                clothData.put("favorite", isFavorite);
                                clothData.put("timestamp", FieldValue.serverTimestamp());

                                db.collection("clothes")
                                        .add(clothData)
                                        .addOnSuccessListener(documentReference -> {
                                            String docId = documentReference.getId();
                                            db.collection("clothes").document(docId).update("id", docId); // ✅ id 필드에 document id 저장

                                            Toast.makeText(this, "등록 완료!", Toast.LENGTH_SHORT).show();
                                            finish(); // 화면 종료
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Firestore 저장 실패", Toast.LENGTH_SHORT).show());
                            }))
                    .addOnFailureListener(e -> Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_SHORT).show());
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
