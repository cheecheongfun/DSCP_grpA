package sg.edu.np.mad.greencycle.Classes;

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

public class PostAnomalyData {

    private static final String URL = "https://deploymodel-bpccapb3a8h8hkhh.southeastasia-01.azurewebsites.net/predict";
    private OkHttpClient client;

    public PostAnomalyData() {
        client = new OkHttpClient();
    }

    public interface ModelCallback {
        void onSuccess(List<String> modelOutput);
        void onFailure(Exception e);
    }

    public void postAnomalyData(double[] energy, int[] month, int[] day, double[] humidity, double[] airTemp, double[] rainfall,ModelCallback callback) {
        JSONObject jsonPayload = new JSONObject();

        try {
            jsonPayload.put("model_id", "model_5");
            jsonPayload.put("energy kwh", new JSONArray(energy));
            jsonPayload.put("month", new JSONArray(month));
            jsonPayload.put("day", new JSONArray(day));
            jsonPayload.put("humidity", new JSONArray(humidity));
            jsonPayload.put("air_temp", new JSONArray(airTemp));
            jsonPayload.put("rain_fall", new JSONArray(rainfall));
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
                Log.e("PostAnomalyData", "Request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONArray valuesPast5days = new JSONArray(responseData);
                        List<String> output = new ArrayList<>();
                        for (int i = 0; i < valuesPast5days.length(); i++) {
                            output.add(valuesPast5days.getString(i));
                        }
                        callback.onSuccess(output);
                        Log.d("PostAnomalyData", "Model output: " + output);
                    } catch (JSONException e) {
                        callback.onFailure(e);
                        Log.e("PostAnomalyData", "Failed to parse response", e);
                    }
                } else {
                    callback.onFailure(new IOException("Request failed with code: " + response.code() + " and message: " + response.message()));
                    Log.e("PostAnomalyData", "Request failed with code: " + response.code() + " and message: " + response.message());
                }
            }
        });
    }
}
