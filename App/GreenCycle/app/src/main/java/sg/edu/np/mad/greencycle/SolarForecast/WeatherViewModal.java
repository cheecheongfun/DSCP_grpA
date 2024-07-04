package sg.edu.np.mad.greencycle.SolarForecast;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherViewModal extends ViewModel {
    private MutableLiveData<ArrayList<HumidityResponse.HumidityItem.StationReading>> humidityLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<TemperatureResponse.TemperatureItem.StationReading>> temperatureLiveData = new MutableLiveData<>();
    private MutableLiveData<WeatherForecastResponse> weatherForecastLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<RainfallResponse.RainfallItem.StationReading>> rainfallLiveData = new MutableLiveData<>();

    public LiveData<ArrayList<HumidityResponse.HumidityItem.StationReading>> getHumidityLiveData() {
        return humidityLiveData;
    }

    public LiveData<ArrayList<TemperatureResponse.TemperatureItem.StationReading>> getTemperatureLiveData() {
        return temperatureLiveData;
    }

    public LiveData<WeatherForecastResponse> getWeatherForecastLiveData() {
        return weatherForecastLiveData;
    }

    public LiveData<ArrayList<RainfallResponse.RainfallItem.StationReading>> getRainfallLiveData() {
        return rainfallLiveData;
    }


    public void fetchHumidityData(String dateTime) {
        ApiService service = RetrofitClient.getClient("https://api.data.gov.sg/").create(ApiService.class);
        service.getHumidityByDateTime(dateTime).enqueue(new Callback<HumidityResponse>() {
            @Override
            public void onResponse(Call<HumidityResponse> call, Response<HumidityResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().items != null && !response.body().items.isEmpty()) {
                    humidityLiveData.postValue(response.body().items.get(0).readings);
                } else {
                    humidityLiveData.postValue(null);
                    Log.d("API Error", "Humidity Response not successful or body/items are null: " + response.code() + ", " + response.message());
                }
            }

            @Override
            public void onFailure(Call<HumidityResponse> call, Throwable t) {
                Log.e("API Error", "Could not fetch humidity data", t);
                humidityLiveData.postValue(null);
            }
        });
    }

    public void fetchTemperatureData(String dateTime) {
        ApiService service = RetrofitClient.getClient("https://api.data.gov.sg/").create(ApiService.class);
        service.getTemperatureByDateTime(dateTime).enqueue(new Callback<TemperatureResponse>() {
            @Override
            public void onResponse(Call<TemperatureResponse> call, Response<TemperatureResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().items != null && !response.body().items.isEmpty()) {
                    temperatureLiveData.postValue(response.body().items.get(0).readings);
                } else {
                    temperatureLiveData.postValue(null);
                    Log.d("API Error", "Temperature Response not successful or body/items are null: " + response.code() + ", " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TemperatureResponse> call, Throwable t) {
                Log.e("API Error", "Could not fetch temperature data", t);
                temperatureLiveData.postValue(null);
            }
        });
    }

    public void fetchWeatherForecast(String dateTime) {
        ApiService service = RetrofitClient.getClient("https://api.data.gov.sg/").create(ApiService.class);
        service.getWeatherForecastByDateTime(dateTime).enqueue(new Callback<WeatherForecastResponse>() {
            @Override
            public void onResponse(Call<WeatherForecastResponse> call, Response<WeatherForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weatherForecastLiveData.postValue(response.body());
                } else {
                    weatherForecastLiveData.postValue(null);
                    Log.d("API Error", "WeatherForecast Response not successful or body is null: " + response.code() + ", " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WeatherForecastResponse> call, Throwable t) {
                Log.e("API Error", "Could not fetch weather forecast data", t);
                weatherForecastLiveData.postValue(null);
            }
        });
    }

    public void fetchRainfallData(String dateTime) {
        ApiService service = RetrofitClient.getClient("https://api.data.gov.sg/").create(ApiService.class);
        service.getRainfallByDateTime(dateTime).enqueue(new Callback<RainfallResponse>() {
            @Override
            public void onResponse(Call<RainfallResponse> call, Response<RainfallResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().items != null && !response.body().items.isEmpty()) {
                    RainfallResponse.RainfallItem item = response.body().items.get(0);
                    ArrayList<RainfallResponse.RainfallItem.StationReading> filteredReadings = new ArrayList<>();
                    for (RainfallResponse.RainfallItem.StationReading reading : item.readings) {
                        if ("S50".equals(reading.station_id)) {
                            filteredReadings.add(reading);
                            break;  // Since we only need data from station S50, we can stop the loop once found
                        }
                    }
                    rainfallLiveData.postValue(filteredReadings);
                } else {
                    rainfallLiveData.postValue(null);
                    Log.d("API Error", "Rainfall Response not successful or body/items are null: " + response.code() + ", " + response.message());
                }
            }

            @Override
            public void onFailure(Call<RainfallResponse> call, Throwable t) {
                Log.e("API Error", "Could not fetch rainfall data", t);
                rainfallLiveData.postValue(null);
            }
        });
    }
}
