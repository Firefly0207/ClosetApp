package com.example.closetapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyFitActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    TextView dateText;
    ImageView fitImage;
    Uri imageUri;  // 사용자가 선택한 이미지의 Uri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_fit);

        dateText = findViewById(R.id.dateText);
        fitImage = findViewById(R.id.fitImage);

        // 전달받은 날짜 확인
        Intent intent = getIntent();
        String selectedDate = intent.getStringExtra("selectedDate");

        if (selectedDate != null) {
            dateText.setText(selectedDate);
        } else {
            // 오늘 날짜 자동 표시
            String today = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(new Date());
            dateText.setText(today);
        }

        // 날짜 텍스트 클릭 시 CalendarActivity로 이동
        dateText.setOnClickListener(v -> {
            Intent calendarIntent = new Intent(DailyFitActivity.this, CalendarActivity.class);
            startActivity(calendarIntent);
        });

        // 이미지 클릭 시 → 이미지 선택기 열기
        fitImage.setOnClickListener(v -> openImagePicker());
    }

    // 갤러리 열기
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // 이미지 선택 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            fitImage.setImageURI(imageUri);  // 선택한 이미지 표시
        }
    }
}
