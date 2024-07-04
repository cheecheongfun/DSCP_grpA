package sg.edu.np.mad.greencycle.SolarForecast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("v1/environment/relative-humidity")
    Call<HumidityResponse> getHumidityByDate(@Query("date") String date);

    @GET("v1/environment/air-temperature")
    Call<TemperatureResponse> getTemperatureByDate(@Query("date") String date);

    @GET("v1/environment/24-hour-weather-forecast")
    Call<WeatherForecastResponse> get24HourWeatherForecast(@Query("date") String date);

    @GET("v1/environment/24-hour-weather-forecast")
    Call<WeatherForecastResponse> getWeatherForecastByDateTime(@Query("date_time") String dateTime);

    @GET("v1/environment/relative-humidity")
    Call<HumidityResponse> getHumidityByDateTime(@Query("date_time") String dateTime);

    @GET("v1/environment/air-temperature")
    Call<TemperatureResponse> getTemperatureByDateTime(@Query("date_time") String dateTime);

    @GET("v1/environment/rainfall")
    Call<RainfallResponse> getRainfallByDateTime(@Query("date_time") String dateTime);
    @GET("v1/forecast")
    Call<OpenMeteoResponse> getForecast(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current") String current,
            @Query("hourly") String hourly,
            @Query("timezone") String timezone,
            @Query("forecast_days") int days
    );




}

