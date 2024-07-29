package sg.edu.np.mad.greencycle.Analytics;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class Analytics_day extends Fragment {
    private static final String TAG = "Analytics_day";

    private DatabaseReference databaseReference;
    private List<Entry> ecData = new ArrayList<>();
    private List<Entry> moistureData = new ArrayList<>();
    private List<Entry> nitrogenData = new ArrayList<>();
    private List<Entry> phosphorusData = new ArrayList<>();
    private List<Entry> potassiumData = new ArrayList<>();
    private List<Entry> temperatureData = new ArrayList<>();
    private List<Entry> phData = new ArrayList<>();
    private TextView todaydate;
    private AppDatabase db;
    private HourlyDataDao hourlyDataDao;
    private User user;
    private Tank tank;
    private Executor databaseExecutor;

    public Analytics_day() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = Room.databaseBuilder(getContext(), AppDatabase.class, "database-name")
                .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3) // Add all migrations here
                .build();
        hourlyDataDao = db.hourlyDataDao();
        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics_day, container, false);

        if (getArguments() != null) {
            user = getArguments().getParcelable("user");
            tank = getArguments().getParcelable("tank");
        }

        todaydate = view.findViewById(R.id.Date);
        updateCurrentDate();
        todaydate.setOnClickListener(v -> showDatePickerDialog());

        return view;
    }

    private void updateCurrentDate() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        todaydate.setText(currentDate);
        fetchAndDisplayData(currentDate);
    }

    private void showDatePickerDialog() {
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, monthOfYear, dayOfMonth);
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
            todaydate.setText(formattedDate);
            fetchAndDisplayData(formattedDate);
        }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(today.getTimeInMillis());
        datePickerDialog.show();
    }

    private void fetchAndDisplayData(String date) {
        if (tank == null || tank.getDeviceID() == null) {
            Toast.makeText(getContext(), "Tank information is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to fetch data from local database first
        databaseExecutor.execute(() -> {
            List<HourlyData> localData = hourlyDataDao.getDataForDateAndTank(date, tank.getDeviceID());
            if (!localData.isEmpty()) {
                Log.d(TAG, "Data retrieved from local database: " + localData.size());
                displayLocalData(localData);
            } else {
                Log.d(TAG, "No data in local database, fetching from Firebase");
                fetchDataFromFirebase(date);
            }
        });
    }

    private void fetchDataFromFirebase(String date) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Tanks/" + tank.getDeviceID() + "/HourlyData");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ecData.clear();
                moistureData.clear();
                nitrogenData.clear();
                phosphorusData.clear();
                potassiumData.clear();
                temperatureData.clear();
                phData.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String timestamp = snapshot.getKey();
                    if (timestamp != null && timestamp.contains(date)) {
                        Map<String, Object> dataMap = (Map<String, Object>) snapshot.getValue();
                        if (dataMap != null) {
                            ecData.add(new Entry(parseTimestamp(timestamp), ((Number) dataMap.get("Soil - EC")).floatValue()));
                            moistureData.add(new Entry(parseTimestamp(timestamp), ((Number) dataMap.get("Soil - Moisture")).floatValue()));
                            nitrogenData.add(new Entry(parseTimestamp(timestamp), ((Number) dataMap.get("Soil - Nitrogen")).floatValue()));
                            phosphorusData.add(new Entry(parseTimestamp(timestamp), ((Number) dataMap.get("Soil - Phosphorus")).floatValue()));
                            potassiumData.add(new Entry(parseTimestamp(timestamp), ((Number) dataMap.get("Soil - Potassium")).floatValue()));
                            temperatureData.add(new Entry(parseTimestamp(timestamp), ((Number) dataMap.get("Soil - Temperature")).floatValue()));
                            phData.add(new Entry(parseTimestamp(timestamp), ((Number) dataMap.get("Soil - PH")).floatValue()));
                        }
                    }
                }
                Log.d(TAG, "Data fetched from Firebase. Sizes - EC: " + ecData.size() + ", Moisture: " + moistureData.size() + ", Nitrogen: " + nitrogenData.size() + ", Phosphorus: " + phosphorusData.size() + ", Potassium: " + potassiumData.size() + ", Temperature: " + temperatureData.size() + ", pH: " + phData.size());
                storeDataLocally(date);
                displayDataOnGraph();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching data from Firebase: " + databaseError.getMessage());
            }
        });
    }

    private int parseTimestamp(String timestamp) {
        // Parse the hour part of the timestamp
        String[] parts = timestamp.split("T")[1].split(":");
        return Integer.parseInt(parts[0]);
    }

    private void displayDataOnGraph() {
        getActivity().runOnUiThread(() -> {
            Log.d(TAG, "Displaying data on graph");
            LineChart ecChart = getView().findViewById(R.id.chart_ec);
            LineChart moistureChart = getView().findViewById(R.id.chart_moisture);
            LineChart nitrogenChart = getView().findViewById(R.id.chart_nitrogen);
            LineChart phosphorusChart = getView().findViewById(R.id.chart_phosphorous);
            LineChart potassiumChart = getView().findViewById(R.id.chart_potassium);
            LineChart temperatureChart = getView().findViewById(R.id.chart_temperature);
            LineChart phChart = getView().findViewById(R.id.chart_ph);

            setupChart(ecChart, ecData, "EC Levels", Color.parseColor("#FFA07A"), Color.WHITE);
            setupChart(moistureChart, moistureData, "Moisture Levels", Color.parseColor("#20B2AA"), Color.WHITE);
            setupChart(nitrogenChart, nitrogenData, "Nitrogen Levels", Color.parseColor("#FFC0CB"), Color.WHITE);
            setupChart(phosphorusChart, phosphorusData, "Phosphorus Levels", Color.parseColor("#DB7093"), Color.WHITE);
            setupChart(potassiumChart, potassiumData, "Potassium Levels", Color.parseColor("#FF69B4"), Color.WHITE);
            setupChart(temperatureChart, temperatureData, "Temperature Levels", Color.parseColor("#32CD32"), Color.WHITE);
            setupChart(phChart, phData, "pH Levels", Color.parseColor("#90EE90"), Color.WHITE);

            // Force the charts to re-render
            ecChart.invalidate();
            moistureChart.invalidate();
            nitrogenChart.invalidate();
            phosphorusChart.invalidate();
            potassiumChart.invalidate();
            temperatureChart.invalidate();
            phChart.invalidate();
        });
    }

    private void displayLocalData(List<HourlyData> localData) {
        ecData.clear();
        moistureData.clear();
        nitrogenData.clear();
        phosphorusData.clear();
        potassiumData.clear();
        temperatureData.clear();
        phData.clear();

        for (HourlyData data : localData) {
            String timestamp = data.timestamp;
            ecData.add(new Entry(parseTimestamp(timestamp), data.ec));
            moistureData.add(new Entry(parseTimestamp(timestamp), data.moisture));
            nitrogenData.add(new Entry(parseTimestamp(timestamp), data.nitrogen));
            phosphorusData.add(new Entry(parseTimestamp(timestamp), data.phosphorous));
            potassiumData.add(new Entry(parseTimestamp(timestamp), data.potassium));
            temperatureData.add(new Entry(parseTimestamp(timestamp), data.temperature));
            phData.add(new Entry(parseTimestamp(timestamp), data.ph));
        }
        Log.d(TAG, "Data retrieved from local database displayed on graph");
        displayDataOnGraph();
    }

    private void setupChart(LineChart chart, List<Entry> data, String label, int color, int backgroundColor) {
        if (data.isEmpty()) {
            Log.d(TAG, "No data to display for " + label);
            return;
        }

        LineDataSet dataSet = new LineDataSet(data, label);
        dataSet.setColor(color);
        dataSet.setValueTextColor(color);
        dataSet.setValueTextSize(10f);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(color);
        dataSet.setCircleRadius(4f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        chart.getDescription().setEnabled(false); // Disable description

        chart.getLegend().setEnabled(false);
        chart.setBackgroundColor(backgroundColor);

        customizeAxis(chart, Color.BLACK);

        // Allow only dragging interactions
        chart.setTouchEnabled(true); // Enable touch interactions
        chart.setDragEnabled(true); // Enable dragging
        chart.setScaleEnabled(false); // Disable scaling
        chart.setPinchZoom(false); // Disable pinch zoom
        chart.setDoubleTapToZoomEnabled(false); // Disable double-tap zoom
        chart.setHighlightPerDragEnabled(false); // Disable highlight on drag
        chart.setHighlightPerTapEnabled(false); // Disable highlight on tap

        chart.setVisibleXRangeMaximum(10); // Set the maximum number of visible entries to avoid cramping

        chart.invalidate(); // Refresh the chart
    }

    private void customizeAxis(LineChart chart, int textColor) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new HourAxisValueFormatter());
        xAxis.setGranularity(1f); // Ensure granularity of 1 hour
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(textColor);
        xAxis.setGridColor(Color.LTGRAY);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setLabelRotationAngle(-45);

        chart.getAxisLeft().setTextColor(textColor);
        chart.getAxisLeft().setGridColor(Color.LTGRAY);
        chart.getAxisLeft().setDrawAxisLine(true);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
    }


    private void storeDataLocally(String date) {
        HourlyData[] dataToInsert = new HourlyData[ecData.size()];
        for (int i = 0; i < ecData.size(); i++) {
            HourlyData data = new HourlyData();
            data.timestamp = date + "T" + String.format("%02d:00:00", (int) ecData.get(i).getX());
            data.tankId = tank.getDeviceID();
            data.ec = ecData.get(i).getY();
            data.moisture = moistureData.get(i).getY();
            data.nitrogen = nitrogenData.get(i).getY();
            data.phosphorous = phosphorusData.get(i).getY();
            data.potassium = potassiumData.get(i).getY();
            data.temperature = temperatureData.get(i).getY();
            data.ph = phData.get(i).getY();
            dataToInsert[i] = data;
        }
        databaseExecutor.execute(() -> {
            Log.d(TAG, "Storing data locally");
            hourlyDataDao.insertAll(dataToInsert);
        });
    }

    class HourAxisValueFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            int hour = (int) value;
            if (hour >= 24) {
                return "";
            }
            return String.format("%02d:00", hour % 24);
        }
    }
}
