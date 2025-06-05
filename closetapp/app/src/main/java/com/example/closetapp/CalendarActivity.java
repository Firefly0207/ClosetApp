package com.example.closetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private TextView selectedDateTextView;
    private Button confirmDateButton;
    private long selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // UI 요소 초기화
        calendarView = findViewById(R.id.calendarView);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        confirmDateButton = findViewById(R.id.confirmDateButton);

        // 현재 날짜로 초기화
        selectedDate = Calendar.getInstance().getTimeInMillis();
        updateSelectedDateText(selectedDate);

        // 캘린더 날짜 선택 리스너
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTimeInMillis();
            updateSelectedDateText(selectedDate);
        });

        // 선택 완료 버튼 리스너
        confirmDateButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedDate", selectedDate);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void updateSelectedDateText(long dateInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
        String dateString = sdf.format(new Date(dateInMillis));
        selectedDateTextView.setText(dateString);
    }
}
