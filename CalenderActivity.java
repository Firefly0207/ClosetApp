package com.example.closetapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {

    Button selectDateBtn;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        selectDateBtn = findViewById(R.id.selectDateBtn);
        calendar = Calendar.getInstance();

        selectDateBtn.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(CalendarActivity.this,
                    (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                        String result = selectedYear + "년 " + (selectedMonth + 1) + "월 " + selectedDay + "일";
                        Intent intent = new Intent(CalendarActivity.this, DailyFitActivity.class);
                        intent.putExtra("selectedDate", result);
                        startActivity(intent);
                        finish();
                    }, year, month, day);

            dialog.show();
        });
    }
}
