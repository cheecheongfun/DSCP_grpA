package sg.edu.np.mad.greencycle.SolarForecast;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForecastViewModel extends ViewModel {
    private MutableLiveData<OpenMeteoResponse> forecastLiveData = new MutableLiveData<>();

    public LiveData<OpenMeteoResponse> getForecastLiveData() {
        return forecastLiveData;
    }

    public void fetchForecastData(double latitude, double longitude, String current, String hourly, String timezone, int days) {
        ApiService service = RetrofitClient.getClient("https://api.open-meteo.com/").create(ApiService.class);
        Log.d("API Call", "Making API call to fetch forecast data");
        service.getForecast(latitude, longitude, current, hourly, timezone, days).enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoResponse> call, Response<OpenMeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", "Forecast data: " + new Gson().toJson(response.body()));
                    forecastLiveData.postValue(response.body());
                } else {
                    try {
                        Log.d("API Error", "Response not successful. Status code: " + response.code() + ", Message: " + response.message() + ", Error Body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    forecastLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<OpenMeteoResponse> call, Throwable t) {
                Log.d("API Error", "API call failed", t);
                forecastLiveData.postValue(null);
            }
        });
    }
}
