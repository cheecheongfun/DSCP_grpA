package sg.edu.np.mad.greencycle.SolarForecast;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForecastViewModel extends ViewModel {
    private MutableLiveData<OpenMeteoResponse> forecastLiveData = new MutableLiveData<>();
    private MutableLiveData<AggregatedData> aggregatedDataLiveData = new MutableLiveData<>();
    private Map<String, double[]> temperatureData = new HashMap<>();
    private Map<String, double[]> humidityData = new HashMap<>();
    private Map<String, double[]> precipProbData = new HashMap<>();
    private Map<String, double[]> precipitationData = new HashMap<>();
    private Map<String, double[]> cloudCoverData = new HashMap<>();

    // Arrays to store the averages for the three days
    private double[] avgHumidity = new double[3];
    private double[] avgTemperature = new double[3];
    private double[] avgPrecipitation = new double[3];

    // Arrays to store the model output for each model
    private Map<String, double[]> modelOutputs = new HashMap<>();
    private Map<String, double[]> model2Outputs = new HashMap<>();
    private Map<String, double[]> model3Outputs = new HashMap<>();
    private Map<String, double[]> model1Outputs = new HashMap<>();

    public LiveData<OpenMeteoResponse> getForecastLiveData() {
        return forecastLiveData;
    }

    public LiveData<AggregatedData> getAggregatedDataLiveData() {
        return aggregatedDataLiveData;
    }

    public double[] getAvgHumidity() {
        return avgHumidity;
    }

    public double[] getAvgTemperature() {
        return avgTemperature;
    }

    public double[] getAvgPrecipitation() {
        return avgPrecipitation;
    }

    public double[] getModel2Outputs(String selectedModel) {
        return model2Outputs.get(selectedModel);
    }

    public double[] getModel3Outputs(String selectedModel) {
        return model3Outputs.get(selectedModel);
    }

    public double[] getModel1Outputs(String selectedModel) {
        return model1Outputs.get(selectedModel);
    }

    public void fetchForecastData(double latitude, double longitude, String current, String hourly, String timezone, int days) {
        ApiService service = RetrofitClient.getClient("https://api.open-meteo.com/").create(ApiService.class);
        service.getForecast(latitude, longitude, current, hourly, timezone, days).enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoResponse> call, Response<OpenMeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ForecastViewModel", "Fetched forecast data successfully: " + response.body().toString());
                    saveDataByDate(response.body().hourly);
                    forecastLiveData.postValue(response.body());
                } else {
                    try {
                        Log.e("ForecastViewModel", "Failed to fetch forecast data: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e("ForecastViewModel", "Failed to fetch forecast data and error parsing response: ", e);
                    }
                    forecastLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<OpenMeteoResponse> call, Throwable t) {
                Log.e("ForecastViewModel", "Error fetching forecast data: " + t.getMessage());
                forecastLiveData.postValue(null);
            }
        });
    }

    private void saveDataByDate(OpenMeteoResponse.Hourly hourly) {
        Log.d("ForecastViewModel", "Saving data by date");

        for (int i = 0; i < hourly.time.size(); i++) {
            String datetime = hourly.time.get(i);
            String date = datetime.substring(0, datetime.indexOf('T'));
            String time = datetime.substring(datetime.indexOf('T') + 1);

            int hour = Integer.parseInt(time.substring(0, 2));

            // Filter data points to be between 9 AM and 6 PM (inclusive)
            if (hour >= 9 && hour <= 18) {
                temperatureData.computeIfAbsent(date, k -> new double[10])[i % 10] = hourly.temperature_2m.get(i);
                humidityData.computeIfAbsent(date, k -> new double[10])[i % 10] = hourly.relative_humidity_2m.get(i);
                precipProbData.computeIfAbsent(date, k -> new double[10])[i % 10] = hourly.precipitation_probability.get(i);
                precipitationData.computeIfAbsent(date, k -> new double[10])[i % 10] = hourly.precipitation.get(i);
                cloudCoverData.computeIfAbsent(date, k -> new double[10])[i % 10] = hourly.cloud_cover.get(i);
            }
        }

        // Log the data arrays for the three days
        temperatureData.forEach((date, data) -> Log.d("TemperatureData", date + ": " + arrayToString(data)));
        humidityData.forEach((date, data) -> Log.d("HumidityData", date + ": " + arrayToString(data)));
        precipProbData.forEach((date, data) -> Log.d("PrecipProbData", date + ": " + arrayToString(data)));
        precipitationData.forEach((date, data) -> Log.d("PrecipitationData", date + ": " + arrayToString(data)));
        cloudCoverData.forEach((date, data) -> Log.d("CloudCoverData", date + ": " + arrayToString(data)));
    }

    public void updateAggregatedData(List<String> dates, int selectedDatePosition, String selectedModel) {
        Log.d("ForecastViewModel", "Updating aggregated data for selected model: " + selectedModel);

        // Reset the average arrays to ensure no leftover data
        for (int i = 0; i < 3; i++) {
            avgTemperature[i] = 0.0;
            avgHumidity[i] = 0.0;
            avgPrecipitation[i] = 0.0;
        }

        // Calculate aggregated data for the three selected dates
        for (int i = 0; i < 3; i++) {
            String selectedDate = dates.get((selectedDatePosition + i) % dates.size());
            calculateAggregatedData(selectedDate, i);
        }

        // Log the arrays of the averages after aggregation
        Log.d("AverageHumidityArray", arrayToString(getAvgHumidity()));
        Log.d("AverageTemperatureArray", arrayToString(getAvgTemperature()));
        Log.d("AveragePrecipitationArray", arrayToString(getAvgPrecipitation()));

        // Check if the model output for the selected model is already cached
        if (modelOutputs.containsKey(selectedModel) || model2Outputs.containsKey(selectedModel) || model1Outputs.containsKey(selectedModel)) {
            double[] cachedOutputs = modelOutputs.getOrDefault(selectedModel, new double[]{0, 0, 0});
            double[] cachedOutputs2 = model2Outputs.getOrDefault(selectedModel, new double[]{0, 0, 0});
            double[] cachedOutputs1 = model1Outputs.getOrDefault(selectedModel, new double[]{0, 0, 0});
            processAggregatedDataForModel(
                    avgTemperature[0], avgHumidity[0], avgPrecipitation[0],
                    avgTemperature[1], avgHumidity[1], avgPrecipitation[1],
                    avgTemperature[2], avgHumidity[2], avgPrecipitation[2],
                    cachedOutputs[0] + cachedOutputs2[0] + cachedOutputs1[0],
                    cachedOutputs[1] + cachedOutputs2[1] + cachedOutputs1[1],
                    cachedOutputs[2] + cachedOutputs2[2] + cachedOutputs1[2]
            );
        } else {
            fetchModelOutputs(selectedModel);
        }
    }

    private void fetchModelOutputs(String selectedModel) {
        Log.d("ForecastViewModel", "Fetching model outputs for: " + selectedModel);

        if ("SOE".equals(selectedModel)) {
            fetchModelData("model_2", model2Outputs, selectedModel);
            fetchModelData("model_3", model3Outputs, selectedModel);
        } else {
            fetchModelData("model_1", model1Outputs, selectedModel);
        }
    }

    private void fetchModelData(String modelName, Map<String, double[]> outputCache, String selectedModel) {
        Log.d("ForecastViewModel", "Fetching data for model: " + modelName);

        PostForecastData apiClient = new PostForecastData();
        apiClient.postForecastData(modelName, avgHumidity, avgTemperature, avgPrecipitation, new PostForecastData.ModelCallback() {
            @Override
            public void onSuccess(List<Double> modelOutput) {
                Log.d("ForecastViewModel", "Model data fetch successful for " + modelName);

                if (modelOutput.size() == 3) {
                    double day1Output = modelOutput.get(0);
                    double day2Output = modelOutput.get(1);
                    double day3Output = modelOutput.get(2);

                    outputCache.put(selectedModel, new double[]{day1Output, day2Output, day3Output});

                    Log.d("ForecastViewModel", modelName + " outputs: " + Arrays.toString(outputCache.get(selectedModel)));

                    double[] cachedOutputs = modelOutputs.getOrDefault(selectedModel, new double[]{0, 0, 0});
                    double[] cachedOutputs2 = model2Outputs.getOrDefault(selectedModel, new double[]{0, 0, 0});
                    double[] cachedOutputs1 = model1Outputs.getOrDefault(selectedModel, new double[]{0, 0, 0});
                    processAggregatedDataForModel(
                            avgTemperature[0], avgHumidity[0], avgPrecipitation[0],
                            avgTemperature[1], avgHumidity[1], avgPrecipitation[1],
                            avgTemperature[2], avgHumidity[2], avgPrecipitation[2],
                            cachedOutputs[0] + cachedOutputs2[0] + cachedOutputs1[0],
                            cachedOutputs[1] + cachedOutputs2[1] + cachedOutputs1[1],
                            cachedOutputs[2] + cachedOutputs2[2] + cachedOutputs1[2]
                    );
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ForecastViewModel", "Model prediction failed for " + modelName, e);
            }
        });
    }

    public void calculateAggregatedData(String date, int dayIndex) {
        if (!temperatureData.containsKey(date) || temperatureData.get(date).length == 0) {
            Log.e("ForecastViewModel", "No data available for date: " + date);
            aggregatedDataLiveData.postValue(null);
            return;
        }

        // Calculate averages for the selected date
        avgTemperature[dayIndex] = average(temperatureData.get(date));
        avgHumidity[dayIndex] = average(humidityData.get(date));
        avgPrecipitation[dayIndex] = average(precipitationData.get(date));

        Log.d("ForecastViewModel", "Aggregated data for date " + date + ": " +
                "avgTemperature = " + avgTemperature[dayIndex] +
                ", avgHumidity = " + avgHumidity[dayIndex] +
                ", avgPrecipitation = " + avgPrecipitation[dayIndex]);
    }

    private void processAggregatedDataForModel(double avgTemperatureDay1, double avgHumidityDay1, double avgPrecipitationDay1,
                                               double avgTemperatureDay2, double avgHumidityDay2, double avgPrecipitationDay2,
                                               double avgTemperatureDay3, double avgHumidityDay3, double avgPrecipitationDay3,
                                               double day1Output, double day2Output, double day3Output) {
        Log.d("ForecastViewModel", "Processing aggregated data for model");

        AggregatedData aggregatedData = new AggregatedData(
                avgTemperatureDay1, avgHumidityDay1, avgPrecipitationDay1,
                avgTemperatureDay2, avgHumidityDay2, avgPrecipitationDay2,
                avgTemperatureDay3, avgHumidityDay3, avgPrecipitationDay3,
                day1Output, day2Output, day3Output
        );
        aggregatedDataLiveData.postValue(aggregatedData);
    }

    private double average(double[] values) {
        double sum = 0;
        int count = 0;
        for (double value : values) {
            sum += value;
            count++;
        }
        return count > 0 ? sum / count : 0;
    }

    private String arrayToString(double[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static class AggregatedData {
        public double avgTemperatureDay1;
        public double avgHumidityDay1;
        public double avgPrecipitationDay1;
        public double avgTemperatureDay2;
        public double avgHumidityDay2;
        public double avgPrecipitationDay2;
        public double avgTemperatureDay3;
        public double avgHumidityDay3;
        public double avgPrecipitationDay3;
        public double day1Output;
        public double day2Output;
        public double day3Output;

        public AggregatedData(
                double avgTemperatureDay1,
                double avgHumidityDay1,
                double avgPrecipitationDay1,
                double avgTemperatureDay2,
                double avgHumidityDay2,
                double avgPrecipitationDay2,
                double avgTemperatureDay3,
                double avgHumidityDay3,
                double avgPrecipitationDay3,
                double day1Output,
                double day2Output,
                double day3Output) {
            this.avgTemperatureDay1 = avgTemperatureDay1;
            this.avgHumidityDay1 = avgHumidityDay1;
            this.avgPrecipitationDay1 = avgPrecipitationDay1;
            this.avgTemperatureDay2 = avgTemperatureDay2;
            this.avgHumidityDay2 = avgHumidityDay2;
            this.avgPrecipitationDay2 = avgPrecipitationDay2;
            this.avgTemperatureDay3 = avgTemperatureDay3;
            this.avgHumidityDay3 = avgHumidityDay3;
            this.avgPrecipitationDay3 = avgPrecipitationDay3;
            this.day1Output = day1Output;
            this.day2Output = day2Output;
            this.day3Output = day3Output;
        }
    }
}
