package pl.tajchert.smartmirror.api;


import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface IWeatherApi {
    @GET("/forecast/daily")
    void getForecastDayily(@Query("lat") String lat, @Query("lon") String lon, @Query("units") String units, @Query("cnt") String dayNumber, Callback<WeatherWeather> callback);
}
