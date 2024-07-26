package sg.edu.np.mad.greencycle.SolarForecast;

import android.util.Log;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PostForecastData {

    private static final String URL = "https://deploying-model-to-fb.azurewebsites.net/predict";
    private OkHttpClient client;

    public PostForecastData() {
        client = new OkHttpClient();
    }

    public ArrayList<Double> postForecastData(double[] forecastedHumidity, double[] forecastedAirTemp, double[] forecastedRainFall) {


        ArrayList<Double> output = new ArrayList<>();
        JSONObject jsonPayload = new JSONObject();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ArrayList<String> forecastedDates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            forecastedDates.add(today.plusDays(i).format(formatter));
        }

        try {
            jsonPayload.put("humidity", new JSONArray(forecastedHumidity));
            jsonPayload.put("air_temp", new JSONArray(forecastedAirTemp));
            jsonPayload.put("rain_fall", new JSONArray(forecastedRainFall));
            jsonPayload.put("dates", new JSONArray(forecastedDates));
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
                Log.e("postData", "on response");
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONArray forecastValuesNext3Days = new JSONArray(responseData);
                        // Handle the forecast values
                        for (int i = 0; i < forecastValuesNext3Days.length(); i++) {
                            System.out.println("Forecasted Energy kWh for day " + (i + 1) + ": " + forecastValuesNext3Days.getDouble(i));
                            output.add(forecastValuesNext3Days.getDouble(i));
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
        return output;
    }
}

