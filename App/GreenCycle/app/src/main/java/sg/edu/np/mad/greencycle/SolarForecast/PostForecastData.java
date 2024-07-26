package sg.edu.np.mad.greencycle.SolarForecast;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PostForecastData {

    private static final String URL = "https://deploying-model-to-fb.azurewebsites.net/api/v1/predict";
    private OkHttpClient client;

    public PostForecastData() {
        client = new OkHttpClient();
    }

    public void postForecastData() {
        // Example forecast data
        int[] forecastedHumidity = {70, 65, 75};
        int[] forecastedAirTemp = {29, 32, 31};
        double[] forecastedRainFall = {0.5, 0.3, 0};

        JSONObject jsonPayload = new JSONObject();
        try {
            jsonPayload.put("humidity", new JSONArray(forecastedHumidity));
            jsonPayload.put("air_temp", new JSONArray(forecastedAirTemp));
            jsonPayload.put("rain_fall", new JSONArray(forecastedRainFall));
        } catch (JSONException e) {
            e.printStackTrace();
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
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONArray forecastValuesNext3Days = new JSONArray(responseData);
                        // Handle the forecast values
                        for (int i = 0; i < forecastValuesNext3Days.length(); i++) {
                            System.out.println("Forecasted Energy kWh for day " + (i + 1) + ": " + forecastValuesNext3Days.getDouble(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Request failed with code: " + response.code());
                    System.out.println("Response message: " + response.message());
                }
            }
        });
    }
}

