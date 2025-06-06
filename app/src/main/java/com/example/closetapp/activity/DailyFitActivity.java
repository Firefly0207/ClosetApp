package com.example.closetapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.closetapp.R;
import com.example.closetapp.dialog.CalendarDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;
import android.widget.FrameLayout;
import com.example.closetapp.model.Outfit;
import com.example.closetapp.adapter.OutfitPagerAdapter;

public class DailyFitActivity extends AppCompatActivity {
    private static final int REQUEST_CALENDAR = 1;
    private static final int REQUEST_ADD_OUTFIT = 2;

    private TextView dateTextView;
    private ViewPager2 viewPager;
    private ImageButton favoriteButton, editButton;
    private FloatingActionButton addOutfitButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
    private List<Outfit> outfitList = new ArrayList<>();
    private OutfitPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_fit);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // UI 요소 초기화
        dateTextView = findViewById(R.id.dateTextView);
        viewPager = findViewById(R.id.viewPager);
        favoriteButton = findViewById(R.id.favoriteButton);
        editButton = findViewById(R.id.editButton);
        addOutfitButton = findViewById(R.id.addOutfitButton);

        // 날짜 표시 및 캘린더 버튼
        updateDateDisplay();
        dateTextView.setOnClickListener(v -> showCalendar());

        // ViewPager 설정
        adapter = new OutfitPagerAdapter(this, outfitList);
        viewPager.setAdapter(adapter);

        // 버튼 리스너 설정
        favoriteButton.setOnClickListener(v -> toggleFavorite());
        editButton.setOnClickListener(v -> editCurrentOutfit());
        addOutfitButton.setOnClickListener(v -> addNewOutfit());

        // 하단 네비게이션
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_daily);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_closet) {
                startActivity(new Intent(this, ClosetActivity.class));
                return true;
            } else if (id == R.id.nav_daily) {
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

        // 초기 데이터 로드
        loadOutfitsForDate();
    }

    private void updateDateDisplay() {
        dateTextView.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void showCalendar() {
        CalendarDialog calendarDialog = new CalendarDialog(this, selectedDate.getTime());
        calendarDialog.setOnDateSelectedListener(date -> {
            selectedDate.setTime(date);
            updateDateDisplay();
            loadOutfitsForDate();
        });
        calendarDialog.show();
    }

    private void loadOutfitsForDate() {
        // 선택된 날짜의 시작과 끝 Timestamp 생성
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTime(selectedDate.getTime());
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = Calendar.getInstance();
        endOfDay.setTime(selectedDate.getTime());
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);

        com.google.firebase.Timestamp startTimestamp = new com.google.firebase.Timestamp(startOfDay.getTimeInMillis() / 1000, 0);
        com.google.firebase.Timestamp endTimestamp = new com.google.firebase.Timestamp(endOfDay.getTimeInMillis() / 1000, 0);

        android.util.Log.d("DailyFit", "userId: " + currentUserId + ", date: " + dateFormat.format(selectedDate.getTime()));

        db.collection("outfits")
                .whereEqualTo("userId", currentUserId)
                .whereGreaterThanOrEqualTo("date", startTimestamp)
                .whereLessThanOrEqualTo("date", endTimestamp)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("DailyFit", "쿼리 결과 개수: " + queryDocumentSnapshots.size());
                    outfitList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Outfit outfit = doc.toObject(Outfit.class);
                        if (outfit != null) {
                            outfit.setId(doc.getId());
                            outfitList.add(outfit);
                        }
                    }
                    adapter.updateOutfits(outfitList);
                    updateFavoriteButton();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("DailyFit", "쿼리 실패", e);
                    Toast.makeText(this, "데이터 로드 실패", Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleFavorite() {
        if (outfitList.isEmpty() || viewPager.getCurrentItem() >= outfitList.size()) return;
        Outfit currentOutfit = outfitList.get(viewPager.getCurrentItem());
        boolean newFavoriteState = !currentOutfit.isFavorite();
        db.collection("outfits").document(currentOutfit.getId())
                .update("favorite", newFavoriteState)
                .addOnSuccessListener(aVoid -> {
                    currentOutfit.setFavorite(newFavoriteState);
                    updateFavoriteButton();
                    Toast.makeText(this, 
                        newFavoriteState ? "즐겨찾기 추가" : "즐겨찾기 해제", 
                        Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "즐겨찾기 업데이트 실패", Toast.LENGTH_SHORT).show());
    }

    private void updateFavoriteButton() {
        if (outfitList.isEmpty() || viewPager.getCurrentItem() >= outfitList.size()) {
            favoriteButton.setVisibility(View.GONE);
            return;
        }
        favoriteButton.setVisibility(View.VISIBLE);
        Outfit currentOutfit = outfitList.get(viewPager.getCurrentItem());
        favoriteButton.setImageResource(
            currentOutfit.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
    }

    private void editCurrentOutfit() {
        if (outfitList.isEmpty() || viewPager.getCurrentItem() >= outfitList.size()) return;
        Outfit currentOutfit = outfitList.get(viewPager.getCurrentItem());
        Intent intent = new Intent(this, OutfitRegisterActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("outfitId", currentOutfit.getId());
        startActivityForResult(intent, REQUEST_ADD_OUTFIT);
    }

    private void addNewOutfit() {
        Intent intent = new Intent(this, OutfitRegisterActivity.class);
        intent.putExtra("mode", "add");
        intent.putExtra("date", selectedDate.getTimeInMillis());
        startActivityForResult(intent, REQUEST_ADD_OUTFIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ADD_OUTFIT) {
                loadOutfitsForDate();
            }
        }
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

