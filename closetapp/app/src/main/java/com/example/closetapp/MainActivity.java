package com.example.closetapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView weatherTextView;
    private FirebaseAuth auth;
    private static final String API_KEY = "YOUR_API_KEY"; // 여기에 OpenWeatherMap API 키 입력
    private static final String CITY_NAME = "Seoul";

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

        // 날씨 출력 텍스트뷰
        weatherTextView = findViewById(R.id.weatherTextView);
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
        Call<WeatherResponse> call = service.getCurrentWeather(CITY_NAME, API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String temp = String.valueOf(response.body().getMain().getTemp());
                    String desc = response.body().getWeather().get(0).getDescription();
                    weatherTextView.setText("서울 날씨: " + temp + "°C, " + desc);
                } else {
                    weatherTextView.setText("날씨 정보를 불러올 수 없음");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("WeatherAPI", "날씨 API 호출 실패", t);
                weatherTextView.setText("날씨 오류");
            }
        });
    }
}
