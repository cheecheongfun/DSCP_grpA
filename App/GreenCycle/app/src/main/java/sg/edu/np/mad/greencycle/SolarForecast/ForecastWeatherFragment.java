package sg.edu.np.mad.greencycle.SolarForecast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sg.edu.np.mad.greencycle.R;

public class ForecastWeatherFragment extends Fragment {

    private ForecastViewModel forecastViewModel;
    private TextView temperatureInfo;
    private TextView humidityInfo;
    private TextView precipProbabilityInfo;
    private TextView precipitationInfo;
    private TextView cloudCoverInfo;
    private Spinner spinnerDate, spinnerTime;
    private HashMap<String, List<String>> dateToTimesMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast_weather, container, false);
        spinnerDate = view.findViewById(R.id.spinnerDate);
        spinnerTime = view.findViewById(R.id.spinnerTime);
        temperatureInfo = view.findViewById(R.id.temperatureInfo);
        humidityInfo = view.findViewById(R.id.humidityInfo);
        precipProbabilityInfo = view.findViewById(R.id.precipProbabilityInfo);
        precipitationInfo = view.findViewById(R.id.precipitationInfo);
        cloudCoverInfo = view.findViewById(R.id.cloudCoverInfo);

        forecastViewModel = new ViewModelProvider(requireActivity()).get(ForecastViewModel.class);
        fetchData2();

        return view;
    }

    public void fetchData2() {
        double latitude = 1.3331;
        double longitude = 103.7759;
        String current = "temperature_2m,relative_humidity_2m,is_day,precipitation,cloud_cover";
        String hourly = "temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,cloud_cover";
        String timezone = "Asia/Singapore";
        int days = 3;
        forecastViewModel.fetchForecastData(latitude, longitude, current, hourly, timezone, days);

        forecastViewModel.getForecastLiveData().observe(getViewLifecycleOwner(), forecastResponse -> {
            if (forecastResponse != null && forecastResponse.hourly != null) {
                List<String> timeList = forecastResponse.hourly.time;
                if (timeList != null && !timeList.isEmpty()) {
                    // Split datetime strings into separate date and time lists
                    for (String datetime : timeList) {
                        String date = datetime.substring(0, datetime.indexOf('T'));
                        String time = datetime.substring(datetime.indexOf('T') + 1);

                        List<String> times = dateToTimesMap.getOrDefault(date, new ArrayList<>());
                        times.add(time);
                        dateToTimesMap.put(date, times);
                    }

                    List<String> dates = new ArrayList<>(dateToTimesMap.keySet());
                    ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, dates);
                    dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDate.setAdapter(dateAdapter);

                    spinnerDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedDate = dates.get(position);
                            List<String> times = dateToTimesMap.get(selectedDate);
                            ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, times);
                            timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerTime.setAdapter(timeAdapter);

                            // Automatically select the first time of the new date
                            spinnerTime.setSelection(0);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Do nothing
                        }
                    });

                    spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedDate = dates.get(spinnerDate.getSelectedItemPosition());
                            String selectedTime = (String) parent.getItemAtPosition(position);
                            String selectedDateTime = selectedDate + "T" + selectedTime;
                            int index = timeList.indexOf(selectedDateTime);

                            if (index != -1) {
                                temperatureInfo.setText("Temperature: " + forecastResponse.hourly.temperature_2m.get(index) + "°C");
                                humidityInfo.setText("Humidity: " + forecastResponse.hourly.relative_humidity_2m.get(index) + "%");
                                precipProbabilityInfo.setText("Precipitation Probability: " + forecastResponse.hourly.precipitation_probability.get(index) + "%");
                                precipitationInfo.setText("Precipitation: " + forecastResponse.hourly.precipitation.get(index) + " mm");
                                cloudCoverInfo.setText("Cloud Cover: " + forecastResponse.hourly.cloud_cover.get(index) + "%");
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Do nothing
                        }
                    });
                } else {
                    updateNoDataAvailable();
                }
            } else {
                updateNoDataAvailable();
            }
        });
    }

    private void updateNoDataAvailable() {
        temperatureInfo.setText("No data available");
        humidityInfo.setText("No data available");
        precipProbabilityInfo.setText("No data available");
        precipitationInfo.setText("No data available");
        cloudCoverInfo.setText("No data available");
    }

}