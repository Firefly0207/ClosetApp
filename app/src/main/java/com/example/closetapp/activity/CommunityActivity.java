package com.example.closetapp.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.closetapp.R;
import com.example.closetapp.adapter.ClothesImageAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.closetapp.adapter.PostAdapter;
import com.example.closetapp.model.PostItem;

public class CommunityActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    PostAdapter postAdapter;
    List<PostItem> postList;
    FloatingActionButton fabAddPost;
    Uri selectedImageUri;
    ImageView imagePreview;
    EditText editCaption;
    ActivityResultLauncher<Intent> imagePickerLauncher;

    FirebaseStorage storage;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        FirebaseApp.initializeApp(this);
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fabAddPost = findViewById(R.id.fab_add_post);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        recyclerView.setAdapter(postAdapter);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (imagePreview != null) imagePreview.setImageURI(selectedImageUri);
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 100);
        } else {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        fabAddPost.setOnClickListener(v -> showAddPostDialog());

        loadPostsFromFirebase();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_community);
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
                return true;
            } else if (id == R.id.nav_mypage) {
                startActivity(new Intent(this, MyPageActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showAddPostDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_post, null);
        imagePreview = dialogView.findViewById(R.id.image_preview);
        editCaption = dialogView.findViewById(R.id.edit_caption);
        Button btnSelectImage = dialogView.findViewById(R.id.btn_select_image);

        btnSelectImage.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("clothes")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<String> imageUrls = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            String url = doc.getString("imageUrl");
                            if (url != null) imageUrls.add(url);
                        }

                        View selectView = LayoutInflater.from(this).inflate(R.layout.dialog_select_image, null);
                        RecyclerView recyclerView = selectView.findViewById(R.id.grid_view);
                        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                        ClothesImageAdapter adapter = new ClothesImageAdapter(this, imageUrls, true);
                        recyclerView.setAdapter(adapter);

                        AlertDialog alertDialog = new AlertDialog.Builder(this)
                                .setTitle("사진 선택")
                                .setView(selectView)
                                .setNegativeButton("취소", null)
                                .create();

                        adapter.setOnItemClickListener((position, selectedUrl) -> {
                            selectedImageUri = Uri.parse(selectedUrl);
                            Glide.with(this).load(selectedUrl).into(imagePreview);
                            alertDialog.dismiss();
                        });

                        alertDialog.show();
                    });
        });

        new AlertDialog.Builder(this)
                .setTitle("New Post")
                .setView(dialogView)
                .setPositiveButton("Post", (dialog, which) -> {
                    String caption = editCaption.getText().toString().trim();

                    if (selectedImageUri != null && !caption.isEmpty()) {
                        uploadToFirebase(caption, selectedImageUri);
                    } else {
                        Toast.makeText(this, "사진과 내용을 입력하세요", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void uploadToFirebase(String caption, Uri imageUri) {
        if (imageUri.toString().startsWith("http")) {
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("caption", caption);
            postMap.put("imageUrl", imageUri.toString());
            postMap.put("likes", 0);
            postMap.put("timestamp", System.currentTimeMillis());
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            postMap.put("userId", userId);

            firestore.collection("posts").add(postMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "포스트 업로드 완료", Toast.LENGTH_SHORT).show();
                        new Handler(Looper.getMainLooper()).postDelayed(this::loadPostsFromFirebase, 1000);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Firestore 저장 실패", Toast.LENGTH_SHORT).show());

        } else {
            String fileName = "images/" + UUID.randomUUID().toString();
            StorageReference ref = storage.getReference().child(fileName);
            ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    Map<String, Object> postMap = new HashMap<>();
                    postMap.put("caption", caption);
                    postMap.put("imageUrl", uri.toString());
                    postMap.put("likes", 0);
                    postMap.put("timestamp", System.currentTimeMillis());
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    postMap.put("userId", userId);

                    firestore.collection("posts").add(postMap)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "업로드 완료", Toast.LENGTH_SHORT).show();
                                new Handler(Looper.getMainLooper()).postDelayed(this::loadPostsFromFirebase, 1000);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Firestore 저장 실패", Toast.LENGTH_SHORT).show());
                });
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Storage 업로드 실패", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadPostsFromFirebase() {
        firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();


                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String docId = doc.getId();
                        String caption = doc.getString("caption");
                        String imageUrl = doc.getString("imageUrl");
                        int likes = doc.getLong("likes").intValue();


                        postList.add(new PostItem(docId, imageUrl, caption, likes));
                    }
                    postAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("FIREBASE", "불러오기 실패", e)
                );
    }
}
