package sg.edu.np.mad.greencycle.SolarForecast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import sg.edu.np.mad.greencycle.R;

public class CurrentWeatherFragment extends Fragment {

    private WeatherViewModal viewModel;
    private TextView humidity, temperature, forecast, rainfall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_weather, container, false);
        humidity = view.findViewById(R.id.humidity);
        temperature = view.findViewById(R.id.temperature);
        forecast = view.findViewById(R.id.forecast);
        rainfall = view.findViewById(R.id.rainfall);

        viewModel = new ViewModelProvider(this).get(WeatherViewModal.class);
        fetchData();
        return view;
    }

    public void fetchData() {
        // Formatting the current date and time to the required format for API requests
        SimpleDateFormat sdft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String currentDateTime = sdft.format(new Date());

        // Fetch data from the APIs for humidity, temperature, weather forecast, and rainfall
        viewModel.fetchHumidityData(currentDateTime);
        viewModel.fetchTemperatureData(currentDateTime);
        viewModel.fetchWeatherForecast(currentDateTime);
        viewModel.fetchRainfallData(currentDateTime);

        // Observe LiveData from the ViewModel for humidity and update UI
        viewModel.getHumidityLiveData().observe(getViewLifecycleOwner(), stationReadings -> {
            if (stationReadings != null && !stationReadings.isEmpty()) {
                HumidityResponse.HumidityItem.StationReading reading = stationReadings.get(0);
                humidity.setText("Humidity: " + reading.value + "%");
            } else {
                humidity.setText("No data");
            }
        });

        // Observe LiveData from the ViewModel for temperature and update UI
        viewModel.getTemperatureLiveData().observe(getViewLifecycleOwner(), stationReadings -> {
            if (stationReadings != null && !stationReadings.isEmpty()) {
                TemperatureResponse.TemperatureItem.StationReading reading = stationReadings.get(0);
                temperature.setText("Temperature: " + reading.value + "°C");
            } else {
                temperature.setText("No data");
            }
        });

        // Observe LiveData from the ViewModel for weather forecast and update UI
        viewModel.getWeatherForecastLiveData().observe(getViewLifecycleOwner(), forecastResponse -> {
            if (forecastResponse != null && !forecastResponse.items.isEmpty()) {
                WeatherForecastResponse.Forecast forecastData = forecastResponse.items.get(0);
                if (forecastData != null && forecastData.general != null) {
                    forecast.setText("Forecast: " + forecastData.general.forecast);
                } else {
                    forecast.setText("No forecast data");
                }
            } else {
                forecast.setText("No data");
            }
        });

        // Observe LiveData from the ViewModel for rainfall and update UI
        viewModel.getRainfallLiveData().observe(getViewLifecycleOwner(), stationReadings -> {
            if (stationReadings != null && !stationReadings.isEmpty()) {
                RainfallResponse.RainfallItem.StationReading reading = stationReadings.get(0);
                rainfall.setText("Rainfall: " + reading.value + " mm");  // Display rainfall with unit
            } else {
                rainfall.setText("No rainfall data for S50");
            }
        });
    }
}
