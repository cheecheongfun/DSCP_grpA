package sg.edu.np.mad.greencycle.SolarForecast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    @GET("v1/environment/relative-humidity")
    Call<HumidityResponse> getHumidityByDate(@Query("date") String date);
}

