package sg.edu.np.mad.greencycle.SolarForecast;

import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static ConcurrentHashMap<String, Retrofit> retrofitInstances = new ConcurrentHashMap<>();
    private static OkHttpClient sharedHttpClient = new OkHttpClient.Builder().build();

    public static Retrofit getClient(String baseUrl) {
        // Use the baseUrl as the key to store and retrieve Retrofit instances
        return retrofitInstances.computeIfAbsent(baseUrl, newBaseUrl -> {
            return new Retrofit.Builder()
                    .baseUrl(newBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(sharedHttpClient)
                    .build();
        });
    }
}
