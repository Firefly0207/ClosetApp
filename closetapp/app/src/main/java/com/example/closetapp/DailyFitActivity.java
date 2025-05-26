package com.example.closetapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.*;

public class DailyFitActivity extends AppCompatActivity {

    private TextView dateTextView;
    private ViewPager2 viewPager;
    private ImageButton favoriteButton, editButton;

    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // 테스트용 임시 이미지 데이터
    private List<Integer> dummyImageList = Arrays.asList(
            R.drawable.sample1,
            R.drawable.sample2,
            R.drawable.sample3
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailyfit);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 뒤로가기 버튼
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 기본 타이틀 제거
        }

        dateTextView = findViewById(R.id.dateTextView);
        viewPager = findViewById(R.id.viewPager);
        favoriteButton = findViewById(R.id.favoriteButton);
        editButton = findViewById(R.id.editButton);

        updateDateText(); // 초기 날짜 표시

        // 날짜 선택
        dateTextView.setOnClickListener(v -> showDatePicker());

        // ViewPager 연결
        OutfitPagerAdapter adapter = new OutfitPagerAdapter(dummyImageList); // 추후 Firestore 연동
        viewPager.setAdapter(adapter);

        // ViewPager 변경 → 날짜도 바꾸고 싶은 경우 여기에 로직 추가 가능

        // 즐겨찾기
        favoriteButton.setOnClickListener(v -> {
            Toast.makeText(this, "즐겨찾기에 저장됨", Toast.LENGTH_SHORT).show();
            // Firestore에 저장 로직 추가 예정
        });

        // 수정하기
        editButton.setOnClickListener(v -> {
            Toast.makeText(this, "수정화면으로 이동", Toast.LENGTH_SHORT).show();
            // 이미지 업로드 or 옷장에서 선택하는 화면 이동
        });

        // 하단 네비게이션
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_daily); // 현재 페이지 표시
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
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

    private void showDatePicker() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog picker = new DatePickerDialog(this, (view, y, m, d) -> {
            selectedDate.set(y, m, d);
            updateDateText();
            // 선택된 날짜에 해당하는 옷 정보 불러오기
        }, year, month, day);
        picker.show();
    }

    private void updateDateText() {
        String dateStr = dateFormat.format(selectedDate.getTime());
        dateTextView.setText(dateStr);
    }
}

