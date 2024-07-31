package sg.edu.np.mad.greencycle.SolarForecast;

import android.util.Log;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PostForecastData {

    private static final String URL = "https://deploymodel-bpccapb3a8h8hkhh.southeastasia-01.azurewebsites.net/predict";
    private OkHttpClient client;

    public PostForecastData() {
        client = new OkHttpClient();
    }

    public interface ModelCallback {
        void onSuccess(List<Double> modelOutput);
        void onFailure(Exception e);
    }

    public void postForecastData(String modelId, double[] forecastedHumidity, double[] forecastedAirTemp, double[] forecastedRainFall, String[] forecastedDates, ModelCallback callback) {
        JSONObject jsonPayload = new JSONObject();

        try {
            jsonPayload.put("model_id", modelId);
            jsonPayload.put("humidity", new JSONArray(forecastedHumidity));
            jsonPayload.put("air_temp", new JSONArray(forecastedAirTemp));
            jsonPayload.put("rain_fall", new JSONArray(forecastedRainFall));
            jsonPayload.put("dates", new JSONArray(forecastedDates));
        } catch (JSONException e) {
            callback.onFailure(e);
            return;
        }

        RequestBody body = RequestBody.create(
                jsonPayload.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
                Log.e("PostForecastData", "Request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONArray forecastValues = new JSONArray(responseData);
                        List<Double> output = new ArrayList<>();
                        for (int i = 0; i < forecastValues.length(); i++) {
                            output.add(forecastValues.getDouble(i));
                        }
                        callback.onSuccess(output);
                        Log.d("PostForecastData", "Model output: " + output.toString());
                    } catch (JSONException e) {
                        callback.onFailure(e);
                        Log.e("PostForecastData", "Failed to parse response", e);
                    }
                } else {
                    callback.onFailure(new IOException("Request failed with code: " + response.code() + " and message: " + response.message()));
                    Log.e("PostForecastData", "Request failed with code: " + response.code() + " and message: " + response.message());
                }
            }
        });
    }
}
