package sg.edu.np.mad.greencycle.SolarForecast;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sg.edu.np.mad.greencycle.R;

public class ForecastWeatherFragment extends Fragment {

    private ForecastViewModel forecastViewModel;
    private TextView temperatureInfo;
    private TextView humidityInfo;
    private TextView precipitationInfo;
    private Spinner spinnerDate;
    private Spinner spinnerModel;
    private BarChart barChart;
    private LineChart lineChart;
    private List<String> dates = new ArrayList<>();
    private String selectedModel;
    private Button viewOneMonthForecastButton;
    private boolean isOneMonthForecast = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast_weather, container, false);
        spinnerDate = view.findViewById(R.id.spinnerDate);
        spinnerModel = view.findViewById(R.id.spinnerModel);
        temperatureInfo = view.findViewById(R.id.temperatureInfo);
        humidityInfo = view.findViewById(R.id.humidityInfo);
        precipitationInfo = view.findViewById(R.id.precipitationInfo);
        barChart = view.findViewById(R.id.barChart);
        lineChart = view.findViewById(R.id.lineChart);
        viewOneMonthForecastButton = view.findViewById(R.id.viewOneMonthForecastButton);

        forecastViewModel = new ViewModelProvider(requireActivity()).get(ForecastViewModel.class);
        setupModelSpinner();
        fetchData();

        viewOneMonthForecastButton.setOnClickListener(v -> {
            if (isOneMonthForecast) {
                viewThreeDayForecast();
            } else {
                viewOneMonthForecast();
            }
        });

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
                updateAggregatedData();

                if ("Estate".equals(selectedModel)) {
                    viewOneMonthForecastButton.setVisibility(View.VISIBLE);
                } else {
                    viewOneMonthForecastButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewOneMonthForecastButton.setVisibility(View.GONE);
            }
        });
    }

    private void fetchData() {
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
                        if (!dates.contains(date)) {
                            dates.add(date);
                        }
                    }

                    ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, dates);
                    dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDate.setAdapter(dateAdapter);

                    spinnerDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            updateAggregatedData();
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
                Log.d("AggregatedData", "Updating UI with aggregated data");
                temperatureInfo.setText(getString(R.string.temperature_info, data.avgTemperatureDay1));
                humidityInfo.setText(getString(R.string.humidity_info, data.avgHumidityDay1));
                precipitationInfo.setText(getString(R.string.precipitation_info, data.avgPrecipitationDay1));
                updateBarChart(data); // Update bar chart with actual data
            } else {
                updateNoDataAvailable();
            }
        });
    }

    private void updateAggregatedData() {
        int selectedDatePosition = spinnerDate.getSelectedItemPosition();
        if (selectedDatePosition == AdapterView.INVALID_POSITION || selectedDatePosition >= dates.size()) {
            updateNoDataAvailable();
            return;
        }

        forecastViewModel.updateAggregatedData(dates, selectedDatePosition, selectedModel);
    }

    private void updateNoDataAvailable() {
        Log.d("ForecastWeatherFragment", "No data available, updating UI");
        temperatureInfo.setText(R.string.no_data_available);
        humidityInfo.setText(R.string.no_data_available);
        precipitationInfo.setText(R.string.no_data_available);
        barChart.clear();
        lineChart.clear();
    }

    private void updateBarChart(ForecastViewModel.AggregatedData data) {
        barChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.GONE);

        TextView totalEnergyTextView = getView().findViewById(R.id.totalEnergyTextView);

        if ("SOE".equals(selectedModel)) {
            List<BarEntry> entries = new ArrayList<>();
            List<BarEntry> totalEntries = new ArrayList<>();
            float totalMaxValue = 0f;
            float totalEnergyGenerated = 0f;

            // Get the outputs for model_2 and model_3
            double[] model2OutputsArray = forecastViewModel.getModel2Outputs(selectedModel);
            double[] model3OutputsArray = forecastViewModel.getModel3Outputs(selectedModel);

            // Add logging to check if data is available
            Log.d("updateBarChart", "Model 2 Outputs: " + (model2OutputsArray != null ? Arrays.toString(model2OutputsArray) : "null"));
            Log.d("updateBarChart", "Model 3 Outputs: " + (model3OutputsArray != null ? Arrays.toString(model3OutputsArray) : "null"));

            if (model2OutputsArray != null && model3OutputsArray != null) {
                for (int i = 0; i < 3; i++) {
                    float model2Output = (float) model2OutputsArray[i];
                    float model3Output = (float) model3OutputsArray[i];
                    entries.add(new BarEntry(i, new float[]{model2Output, model3Output}));
                    float totalOutput = model2Output + model3Output;
                    totalEntries.add(new BarEntry(i, totalOutput));
                    totalEnergyGenerated += totalOutput;
                    totalMaxValue = Math.max(totalMaxValue, totalOutput);
                }

                BarDataSet dataSet = new BarDataSet(entries, "");
                dataSet.setColors(new int[]{
                        android.graphics.Color.parseColor("#66BB6A"), // Lighter green for model 2
                        android.graphics.Color.parseColor("#388E3C")  // Darker green for model 3
                });
                dataSet.setStackLabels(new String[]{"Sensor 1 Output", "Sensor 2 Output"});
                dataSet.setDrawValues(false); // Disable drawing values for individual segments

                BarDataSet totalDataSet = new BarDataSet(totalEntries, "");
                totalDataSet.setDrawValues(true);
                totalDataSet.setValueTextColor(android.graphics.Color.BLACK);
                totalDataSet.setColors(android.graphics.Color.TRANSPARENT);

                // Custom value formatter to display the total value at the top
                totalDataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getBarLabel(BarEntry barEntry) {
                        return String.valueOf((int) barEntry.getY());
                    }
                });

                BarData barData = new BarData(dataSet, totalDataSet);
                barData.setBarWidth(0.9f); // set custom bar width
                barChart.setData(barData);
                barChart.setFitBars(true); // make the x-axis fit exactly all bars

                // Set the y-axis range based on data
                YAxis leftAxis = barChart.getAxisLeft();
                leftAxis.setGranularity(1f);
                leftAxis.setGranularityEnabled(true);

                leftAxis.setAxisMinimum(0f); // Start y-axis from 0
                leftAxis.setAxisMaximum(totalMaxValue + 10); // Add some padding to the maximum

                YAxis rightAxis = barChart.getAxisRight();
                rightAxis.setEnabled(false);

                barChart.invalidate(); // refresh
            } else {
                Log.e("updateBarChart", "No data available for model 2 or model 3 outputs.");
            }

            // Update total energy text view
            totalEnergyTextView.setText("Total SOE 3-Day Energy Generated: " + Math.round(totalEnergyGenerated) + " kWh");

        } else if ("Estate".equals(selectedModel)) {
            List<BarEntry> entries = new ArrayList<>();
            List<BarEntry> totalEntries = new ArrayList<>();
            float totalMaxValue = 0f;
            float totalEnergyGenerated = 0f;

            // Add entries for model_1
            double[] model1OutputsArray = forecastViewModel.getModel1Outputs(selectedModel);
            Log.d("updateBarChart", "Model 1 Outputs: " + (model1OutputsArray != null ? Arrays.toString(model1OutputsArray) : "null"));

            if (model1OutputsArray != null) {
                for (int i = 0; i < 3; i++) {
                    float model1Output = (float) model1OutputsArray[i];
                    entries.add(new BarEntry(i, model1Output));
                    totalEntries.add(new BarEntry(i, model1Output));
                    totalEnergyGenerated += model1Output;
                    totalMaxValue = Math.max(totalMaxValue, model1Output);
                }

                BarDataSet dataSet = new BarDataSet(entries, "Model 1 Output");
                dataSet.setColors(new int[]{
                        android.graphics.Color.parseColor("#4285F4")  // Blue shade for model 1
                });
                dataSet.setDrawValues(false); // Disable drawing values for individual segments

                BarDataSet totalDataSet = new BarDataSet(totalEntries, "");
                totalDataSet.setDrawValues(true);
                totalDataSet.setValueTextColor(android.graphics.Color.BLACK);
                totalDataSet.setColors(android.graphics.Color.TRANSPARENT);

                // Custom value formatter to display the total value
                totalDataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getBarLabel(BarEntry barEntry) {
                        return String.valueOf((int) barEntry.getY());
                    }
                });

                BarData barData = new BarData(dataSet, totalDataSet);
                barData.setBarWidth(0.9f); // set custom bar width
                barChart.setData(barData);
                barChart.setFitBars(true); // make the x-axis fit exactly all bars

                // Set the y-axis range based on data
                YAxis leftAxis = barChart.getAxisLeft();
                leftAxis.setGranularity(1f);
                leftAxis.setGranularityEnabled(true);

                leftAxis.setAxisMinimum(0f); // Start y-axis from 0
                leftAxis.setAxisMaximum(totalMaxValue + 10); // Add some padding to the maximum

                YAxis rightAxis = barChart.getAxisRight();
                rightAxis.setEnabled(false);

                barChart.invalidate(); // refresh
            } else {
                Log.e("updateBarChart", "No data available for model 1 outputs.");
            }

            // Update total energy text view
            totalEnergyTextView.setText("Total Estate 3-Day Energy Generated: " + Math.round(totalEnergyGenerated) + " kWh");
        }

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

    private void viewOneMonthForecast() {
        String modelId = "model_1";
        forecastViewModel.fetchOneMonthForecastData(modelId, new PostForecastData.ModelCallback() {
            @Override
            public void onSuccess(List<Double> modelOutput) {
                Log.d("ForecastWeatherFragment", "One month forecast data: " + modelOutput.toString());
                requireActivity().runOnUiThread(() -> {
                    updateLineChart(modelOutput);
                    viewOneMonthForecastButton.setText("View 3-Day Energy Forecast");
                    isOneMonthForecast = true;
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ForecastWeatherFragment", "Error fetching one month forecast data", e);
                // Handle the error scenario
            }
        });
    }

    private void viewThreeDayForecast() {
        // Switch back to 3-day forecast view
        isOneMonthForecast = false;
        viewOneMonthForecastButton.setText("View 1 Month Energy Forecast");
        updateAggregatedData();
    }

    private void updateLineChart(List<Double> modelOutput) {
        lineChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);

        List<Entry> entries = new ArrayList<>();
        float totalEnergy = 0;
        for (int i = 0; i < modelOutput.size(); i++) {
            float value = modelOutput.get(i).floatValue();
            entries.add(new Entry(i, value));
            totalEnergy += value;
        }

        LineDataSet dataSet = new LineDataSet(entries, "One Month Forecast");
        dataSet.setColor(android.graphics.Color.BLUE);
        dataSet.setValueTextColor(android.graphics.Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(true);
        dataSet.setDrawCircleHole(false);
        dataSet.setCircleColor(android.graphics.Color.BLUE);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < dates.size()) {
                    return dates.get(index);
                } else {
                    return "";
                }
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.getDescription().setEnabled(false);

        // Enable dragging but disable scaling
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);

        // Set the MarkerView
        DateMarkerView markerView = new DateMarkerView(getContext(), R.layout.marker_view, dates);
        lineChart.setMarker(markerView);

        // Update total energy text view for one month
        TextView totalEnergyTextView = getView().findViewById(R.id.totalEnergyTextView);
        totalEnergyTextView.setText("Total 1-Month Energy Generated: " + Math.round(totalEnergy) + " kWh");

        lineChart.invalidate();
    }
}
