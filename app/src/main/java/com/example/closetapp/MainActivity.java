package com.example.closetapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private Button weatherTabButton, closetTabButton, calendarTabButton, communityTabButton;
    private TextView sectionTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 각 탭 버튼
        weatherTabButton = findViewById(R.id.weatherTabButton);
        closetTabButton = findViewById(R.id.closetTabButton);
        calendarTabButton = findViewById(R.id.calendarTabButton);
        communityTabButton = findViewById(R.id.communityTabButton);
        sectionTitle = findViewById(R.id.sectionTitle);

        weatherTabButton.setOnClickListener(v -> sectionTitle.setText("날씨 탭"));
        closetTabButton.setOnClickListener(v -> sectionTitle.setText("옷장 탭"));
        calendarTabButton.setOnClickListener(v -> sectionTitle.setText("달력 탭"));
        communityTabButton.setOnClickListener(v -> sectionTitle.setText("커뮤니티 탭"));

        setupBottomNavigation();
    }

    @SuppressLint("NonConstantResourceId")
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // 현재 메인 화면
                return true;
            }
            // 개발 중
            /*else if (itemId == R.id.nav_closet) {
                startActivity(new Intent(MainActivity.this, ClosetActivity.class));
                return true;
            } else if (itemId == R.id.nav_calendar) {
                startActivity(new Intent(MainActivity.this, CalendarActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }*/

            return false;
        });
    }

}
