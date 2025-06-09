package com.example.closetapp.network;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.example.closetapp.model.WeatherResponse;

public interface WeatherApiService {
    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("q") String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String lang
    );
}