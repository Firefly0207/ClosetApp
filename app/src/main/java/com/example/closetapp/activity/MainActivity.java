package com.example.closetapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.closetapp.R;
import com.example.closetapp.adapter.ClothAdapter;
import com.example.closetapp.model.Cloth;
import com.example.closetapp.model.WeatherResponse;
import com.example.closetapp.network.RetrofitClient;
import com.example.closetapp.network.WeatherApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView weatherTempTextView, weatherCityTextView, weatherDescTextView;
    private ImageView weatherIconImageView;
    private FirebaseAuth auth;
    private static final String API_KEY = "c8546efe4025c94f2b714065b5468b3c"; // 여기에 OpenWeatherMap API 키 입력
    private static final String CITY_NAME = "Seoul";
    private RecyclerView closetPreviewRecyclerView;
    private ClothAdapter closetPreviewAdapter;
    private List<Cloth> closetPreviewList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // xml은 앞서 작성된 Toss 스타일 기반

        // 로그인 확인
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // 옷장 미리보기 RecyclerView 초기화
        closetPreviewRecyclerView = findViewById(R.id.closetPreviewRecyclerView);
        closetPreviewRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        closetPreviewAdapter = new ClothAdapter(this, closetPreviewList);
        closetPreviewRecyclerView.setAdapter(closetPreviewAdapter);
        db = FirebaseFirestore.getInstance();
        loadClosetPreview();

        // 날씨 출력 텍스트뷰
        weatherTempTextView = findViewById(R.id.weatherTempTextView);
        weatherCityTextView = findViewById(R.id.weatherCityTextView);
        weatherDescTextView = findViewById(R.id.weatherDescTextView);
        weatherIconImageView = findViewById(R.id.weatherIconImageView);
        weatherDescTextView.setVisibility(View.GONE); // description 텍스트 임시 숨김
        fetchWeather();

        // 하단 네비게이션
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true; // 현재 페이지
                } else if (id == R.id.nav_closet) {
                    startActivity(new Intent(MainActivity.this, ClosetActivity.class));
                } else if (id == R.id.nav_daily) {
                    startActivity(new Intent(MainActivity.this, DailyFitActivity.class));
                } else if (id == R.id.nav_community) {
                    startActivity(new Intent(MainActivity.this, CommunityActivity.class));
                } else if (id == R.id.nav_mypage) {
                    startActivity(new Intent(MainActivity.this, MyPageActivity.class));
                }
                return true;
            }
        });
    }

    private void fetchWeather() {
        WeatherApiService service = RetrofitClient.getInstance();
        Call<WeatherResponse> call = service.getCurrentWeather(CITY_NAME, API_KEY, "metric", "kr");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String temp = String.valueOf(response.body().getMain().getTemp());
                    String desc = response.body().getWeather().get(0).getDescription();
                    String icon = response.body().getWeather().get(0).getIcon();
                    weatherTempTextView.setText(temp + "℃");
                    // 아이콘
                    int resId = getResources().getIdentifier("ic_weather_" + icon, "drawable", getPackageName());
                    if (resId != 0) {
                        weatherIconImageView.setImageResource(resId);
                    } else {
                        weatherIconImageView.setImageDrawable(null);
                    }
                    // 한글 설명
                    weatherDescTextView.setText(desc);
                    weatherDescTextView.setVisibility(View.VISIBLE);
                    weatherCityTextView.setText(CITY_NAME);
                    // 온도 색상 동적 적용
                    try {
                        float tempValue = Float.parseFloat(temp);
                        weatherTempTextView.setTextColor(getTempColor(tempValue));
                    } catch (Exception e) {
                        weatherTempTextView.setTextColor(0xFF000000); // 검정색 fallback
                    }
                } else {
                    weatherTempTextView.setText("-");
                    weatherCityTextView.setText(CITY_NAME);
                    weatherDescTextView.setText("-");
                    weatherTempTextView.setTextColor(0xFF000000);
                    weatherIconImageView.setImageDrawable(null);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("WeatherAPI", "날씨 API 호출 실패", t);
                weatherTempTextView.setText("-");
                weatherCityTextView.setText(CITY_NAME);
                weatherDescTextView.setText("-");
                weatherTempTextView.setTextColor(0xFF000000);
                weatherIconImageView.setImageDrawable(null);
            }
        });
    }

    // 온도에 따라 파랑~빨강 색상 반환 (-10~30℃)
    private int getTempColor(float temp) {
        float t = Math.max(-10, Math.min(30, temp));
        float ratio = (t + 10) / 40f; // -10~30 → 0~1
        int red = (int)(255 * ratio);
        int blue = (int)(255 * (1 - ratio));
        return 0xFF000000 | (red << 16) | (0 << 8) | blue;
    }

    private void loadClosetPreview() {
        db.collection("clothes").get().addOnSuccessListener(queryDocumentSnapshots -> {
            closetPreviewList.clear();
            for (var doc : queryDocumentSnapshots) {
                Cloth cloth = doc.toObject(Cloth.class);
                cloth.setId(doc.getId());
                closetPreviewList.add(cloth);
            }
            closetPreviewAdapter.notifyDataSetChanged();
        });
    }
}
