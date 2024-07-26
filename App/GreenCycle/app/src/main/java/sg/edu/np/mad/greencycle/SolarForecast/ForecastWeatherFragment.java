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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

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
    private Spinner spinnerDate;
    private Spinner spinnerModel;
    private HashMap<String, List<String>> dateToTimesMap = new HashMap<>();
    private OpenMeteoResponse.Hourly currentHourlyData;
    private BarChart barChart;
    private List<String> dates = new ArrayList<>();
    private String selectedModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast_weather, container, false);
        spinnerDate = view.findViewById(R.id.spinnerDate);
        spinnerModel = view.findViewById(R.id.spinnerModel);
        temperatureInfo = view.findViewById(R.id.temperatureInfo);
        humidityInfo = view.findViewById(R.id.humidityInfo);
        precipProbabilityInfo = view.findViewById(R.id.precipProbabilityInfo);
        precipitationInfo = view.findViewById(R.id.precipitationInfo);
        cloudCoverInfo = view.findViewById(R.id.cloudCoverInfo);
        barChart = view.findViewById(R.id.barChart);

        forecastViewModel = new ViewModelProvider(requireActivity()).get(ForecastViewModel.class);
        setupModelSpinner();
        fetchData();

        return view;
    }

    private void setupModelSpinner() {
        List<String> models = new ArrayList<>();
        models.add("SOE");
        models.add("Estate");
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, models);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModel.setAdapter(modelAdapter);

        spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedModel = models.get(position);
                fetchData(); // re-fetch data when model is changed
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    public void fetchData() {
        double latitude = 1.3331;
        double longitude = 103.7759;
        String current = "temperature_2m,relative_humidity_2m,is_day,precipitation,cloud_cover";
        String hourly = "temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,cloud_cover";
        String timezone = "Asia/Singapore";
        int days = 3;
        forecastViewModel.fetchForecastData(latitude, longitude, current, hourly, timezone, days);

        forecastViewModel.getForecastLiveData().observe(getViewLifecycleOwner(), forecastResponse -> {
            if (forecastResponse != null && forecastResponse.hourly != null) {
                currentHourlyData = forecastResponse.hourly;
                List<String> timeList = forecastResponse.hourly.time;
                if (timeList != null && !timeList.isEmpty()) {
                    // Split datetime strings into separate date and time lists
                    for (String datetime : timeList) {
                        String date = datetime.substring(0, datetime.indexOf('T'));
                        if (!dates.contains(date)) {
                            dates.add(date);
                        }
                        List<String> times = dateToTimesMap.getOrDefault(date, new ArrayList<>());
                        times.add(datetime);
                        dateToTimesMap.put(date, times);
                    }

                    ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, dates);
                    dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDate.setAdapter(dateAdapter);

                    spinnerDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedDate = dates.get(position);
                            List<String> selectedDateTimes = dateToTimesMap.get(selectedDate);

                            // Aggregate the data for the selected date
                            forecastViewModel.calculateAggregatedData(currentHourlyData, selectedDateTimes, selectedModel);
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

        forecastViewModel.getAggregatedDataLiveData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                temperatureInfo.setText(getString(R.string.temperature_info, data.avgTemperature));
                humidityInfo.setText(getString(R.string.humidity_info, data.avgHumidity));
                precipProbabilityInfo.setText(getString(R.string.precip_probability_info, data.avgPrecipProb));
                precipitationInfo.setText(getString(R.string.precipitation_info, data.avgPrecipitation));
                cloudCoverInfo.setText(getString(R.string.cloud_cover_info, data.avgCloudCover));
                updateBarChart(data); // update bar chart with actual data
            } else {
                updateNoDataAvailable();
            }
        });
    }

    private void updateNoDataAvailable() {
        temperatureInfo.setText(R.string.no_data_available);
        humidityInfo.setText(R.string.no_data_available);
        precipProbabilityInfo.setText(R.string.no_data_available);
        precipitationInfo.setText(R.string.no_data_available);
        cloudCoverInfo.setText(R.string.no_data_available);
        barChart.clear();
    }

    private void updateBarChart(ForecastViewModel.AggregatedData data) {
        // Placeholder values for the bar chart, to be replaced by actual model output
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) data.day1Output));
        entries.add(new BarEntry(1f, (float) data.day2Output));
        entries.add(new BarEntry(2f, (float) data.day3Output));

        BarDataSet dataSet = new BarDataSet(entries, "Energy Output");
        dataSet.setColors(new int[]{
                android.graphics.Color.parseColor("#66BB6A"), // Green shade 1
                android.graphics.Color.parseColor("#43A047"), // Green shade 2
                android.graphics.Color.parseColor("#2E7D32")  // Green shade 3
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f); // set custom bar width
        barChart.clear(); // clear the old data
        barChart.setData(barData);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.invalidate(); // refresh

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (value >= 0 && value < dates.size()) {
                    return dates.get((int) value);
                } else {
                    return "";
                }
            }
        });

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Disable interaction
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);

        // Remove description label
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
    }
}
