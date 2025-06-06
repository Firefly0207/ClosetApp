package com.example.closetapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CalendarView;
import android.widget.TextView;

import com.example.closetapp.R;

import java.util.Date;

public class CalendarDialog extends Dialog {
    private Date selectedDate;
    private OnDateSelectedListener listener;
    private CalendarView calendarView;
    private TextView titleTextView;

    public interface OnDateSelectedListener {
        void onDateSelected(Date date);
    }

    public CalendarDialog(Context context, Date initialDate) {
        super(context);
        this.selectedDate = initialDate;
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_calendar);

        calendarView = findViewById(R.id.calendarView);
        titleTextView = findViewById(R.id.titleTextView);

        // 초기 날짜 설정
        calendarView.setDate(selectedDate.getTime());

        // 날짜 선택 리스너
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate.setYear(year - 1900); // Date 클래스는 1900년부터 시작
            selectedDate.setMonth(month);
            selectedDate.setDate(dayOfMonth);
            
            if (listener != null) {
                listener.onDateSelected(selectedDate);
            }
            dismiss();
        });

        // 취소 버튼
        findViewById(R.id.cancelButton).setOnClickListener(v -> dismiss());
    }
} 