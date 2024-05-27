package sg.edu.np.mad.greencycle.SolarForecast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherViewModel extends ViewModel {
    private MutableLiveData<ArrayList<HumidityResponse.StationReading>> humidityLiveData = new MutableLiveData<>();

    public LiveData<ArrayList<HumidityResponse.StationReading>> getHumidityLiveData() {
        return humidityLiveData;
    }

    public void fetchHumidityData(String date) {
        WeatherApiService service = RetrofitClient.getClient().create(WeatherApiService.class);
        service.getHumidityByDate(date).enqueue(new Callback<HumidityResponse>() {
            @Override
            public void onResponse(Call<HumidityResponse> call, Response<HumidityResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().items.isEmpty()) {
                    // Assuming we only want the readings from the first item for simplicity
                    ArrayList<HumidityResponse.StationReading> readings = response.body().items.get(0).readings;
                    humidityLiveData.postValue(readings);
                }
            }

            @Override
            public void onFailure(Call<HumidityResponse> call, Throwable t) {
                // Handle failure here
            }
        });
    }
}
