package pl.tajchert.smartmirror.api;


import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface IWeatherApi {
    @GET("/forecast/daily")
    void getForecastDayily(@Query("lat") double lat, @Query("lon") double lon, @Query("units") String units, @Query("cnt") int dayNumber, Callback<WeatherCity> callback);
}
