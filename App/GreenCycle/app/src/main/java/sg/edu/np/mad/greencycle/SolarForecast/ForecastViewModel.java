package sg.edu.np.mad.greencycle.SolarForecast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForecastViewModel extends ViewModel {
    private MutableLiveData<OpenMeteoResponse> forecastLiveData = new MutableLiveData<>();
    private MutableLiveData<AggregatedData> aggregatedDataLiveData = new MutableLiveData<>();

    public LiveData<OpenMeteoResponse> getForecastLiveData() {
        return forecastLiveData;
    }

    public LiveData<AggregatedData> getAggregatedDataLiveData() {
        return aggregatedDataLiveData;
    }

    public void fetchForecastData(double latitude, double longitude, String current, String hourly, String timezone, int days) {
        ApiService service = RetrofitClient.getClient("https://api.open-meteo.com/").create(ApiService.class);
        service.getForecast(latitude, longitude, current, hourly, timezone, days).enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoResponse> call, Response<OpenMeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    forecastLiveData.postValue(response.body());
                } else {
                    forecastLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<OpenMeteoResponse> call, Throwable t) {
                forecastLiveData.postValue(null);
            }
        });
    }

    public void calculateAggregatedData(OpenMeteoResponse.Hourly hourly, List<String> selectedDateTimes, String model) {
        // Placeholder logic for aggregation and model selection
        double sumTemperature = 0, sumHumidity = 0, sumPrecipProb = 0, sumPrecipitation = 0, sumCloudCover = 0;
        int count = 0;

        for (String datetime : selectedDateTimes) {
            int index = hourly.time.indexOf(datetime);
            if (index != -1) {
                sumTemperature += hourly.temperature_2m.get(index);
                sumHumidity += hourly.relative_humidity_2m.get(index);
                sumPrecipProb += hourly.precipitation_probability.get(index);
                sumPrecipitation += hourly.precipitation.get(index);
                sumCloudCover += hourly.cloud_cover.get(index);
                count++;
            }
        }

        double avgTemperature = sumTemperature / count;
        double avgHumidity = sumHumidity / count;
        double avgPrecipProb = sumPrecipProb / count;
        double avgPrecipitation = sumPrecipitation / count;
        double avgCloudCover = sumCloudCover / count;

        double day1Output, day2Output, day3Output;
        if ("SOE".equals(model)) {
            day1Output = 500; // Placeholder for model 1
            day2Output = 333;    // Placeholder for model 1
            day3Output = 444;  // Placeholder for model 1
        } else {
            day1Output = 100;  // Placeholder for model 2
            day2Output = 60; // Placeholder for model 2
            day3Output = 88;  // Placeholder for model 2
        }

        AggregatedData aggregatedData = new AggregatedData(avgTemperature, avgHumidity, avgPrecipProb, avgPrecipitation, avgCloudCover, day1Output, day2Output, day3Output);
        aggregatedDataLiveData.postValue(aggregatedData);
    }

    public static class AggregatedData {
        public double avgTemperature, avgHumidity, avgPrecipProb, avgPrecipitation, avgCloudCover;
        public double day1Output, day2Output, day3Output;

        public AggregatedData(double avgTemperature, double avgHumidity, double avgPrecipProb, double avgPrecipitation, double avgCloudCover, double day1Output, double day2Output, double day3Output) {
            this.avgTemperature = avgTemperature;
            this.avgHumidity = avgHumidity;
            this.avgPrecipProb = avgPrecipProb;
            this.avgPrecipitation = avgPrecipitation;
            this.avgCloudCover = avgCloudCover;
            this.day1Output = day1Output;
            this.day2Output = day2Output;
            this.day3Output = day3Output;
        }
    }
}
