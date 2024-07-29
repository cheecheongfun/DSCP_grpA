package sg.edu.np.mad.greencycle.Analytics;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class Week_charts extends Fragment {
    private static final String TAG = "Week_charts";

    private Calendar currentWeek = Calendar.getInstance();
    private TextView weekDateTextView;
    private ImageButton btnNextWeek;
    private String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private AppDatabase db;
    private HourlyDataDao hourlyDataDao;
    private Executor databaseExecutor;

    private User user;
    private Tank tank;

    public Week_charts() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = Room.databaseBuilder(getContext(), AppDatabase.class, "database-name")
                .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
                .build();
        hourlyDataDao = db.hourlyDataDao();
        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week_charts, container, false);

        if (getArguments() != null) {
            user = getArguments().getParcelable("user");
            tank = getArguments().getParcelable("tank");
        }

        weekDateTextView = view.findViewById(R.id.Weekdate);
        btnNextWeek = view.findViewById(R.id.btnNextWeek);
        ImageButton btnPreviousWeek = view.findViewById(R.id.btnPreviousWeek);

        weekDateTextView.setOnClickListener(v -> showDatePickerDialog());
        btnPreviousWeek.setOnClickListener(v -> adjustWeek(-1));
        btnNextWeek.setOnClickListener(v -> adjustWeek(1));

        updateDateDisplay();
        setupCharts(view);
        return view;
    }

    private void showDatePickerDialog() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            adjustToSelectedWeek(selectedDate);
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMaxDate(today.getTimeInMillis());
        datePickerDialog.show();
    }

    private void adjustToSelectedWeek(Calendar selectedDate) {
        int dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK) - selectedDate.getFirstDayOfWeek();
        selectedDate.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
        currentWeek.setTime(selectedDate.getTime());
        updateDateDisplay();
        checkButtonState();
        setupCharts(getView());
    }

    private void adjustWeek(int amount) {
        currentWeek.add(Calendar.WEEK_OF_YEAR, amount);
        updateDateDisplay();
        checkButtonState();
        setupCharts(getView());
    }

    private void checkButtonState() {
        Calendar nextWeek = (Calendar) currentWeek.clone();
        nextWeek.add(Calendar.WEEK_OF_YEAR, 1);
        btnNextWeek.setEnabled(!nextWeek.after(Calendar.getInstance()));
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        weekDateTextView.setText(dateFormat.format(currentWeek.getTime()));
    }

    private void setupCharts(View view) {
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentWeek.getTime());
        Calendar endCalendar = (Calendar) currentWeek.clone();
        endCalendar.add(Calendar.DAY_OF_YEAR, 6);
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endCalendar.getTime());

        databaseExecutor.execute(() -> {
            List<DailyAggregate> aggregates = hourlyDataDao.getDailyAggregates(startDate, endDate, tank.getDeviceID());
            if (aggregates.isEmpty()) {
                getActivity().runOnUiThread(() -> clearCharts(view));
                return;
            }

            getActivity().runOnUiThread(() -> {
                setupLineChart((LineChart) view.findViewById(R.id.chart_ec), generateLineData(aggregates, "ec"), "EC Levels", Color.parseColor("#FFA07A"));
                setupLineChart((LineChart) view.findViewById(R.id.chart_moisture), generateLineData(aggregates, "moisture"), "Moisture Levels", Color.parseColor("#20B2AA"));
                setupBarChart((BarChart) view.findViewById(R.id.chart_nitrogen), generateBarData(aggregates, "nitrogen"), "Nitrogen Levels", Color.parseColor("#FFC0CB"));
                setupBarChart((BarChart) view.findViewById(R.id.chart_phosphorous), generateBarData(aggregates, "phosphorous"), "Phosphorus Levels", Color.parseColor("#DB7093"));
                setupBarChart((BarChart) view.findViewById(R.id.chart_potassium), generateBarData(aggregates, "potassium"), "Potassium Levels", Color.parseColor("#FF69B4"));
                setupLineChart((LineChart) view.findViewById(R.id.chart_temperature), generateLineData(aggregates, "temperature"), "Temperature Levels", Color.parseColor("#32CD32"));
                setupLineChart((LineChart) view.findViewById(R.id.chart_ph), generateLineData(aggregates, "ph"), "pH Levels", Color.parseColor("#90EE90"));
            });
        });
    }

    private ArrayList<BarEntry> generateBarData(List<DailyAggregate> aggregates, String field) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < aggregates.size(); i++) {
            float value = 0;
            switch (field) {
                case "nitrogen":
                    value = aggregates.get(i).avg_nitrogen;
                    break;
                case "phosphorous":
                    value = aggregates.get(i).avg_phosphorous;
                    break;
                case "potassium":
                    value = aggregates.get(i).avg_potassium;
                    break;
            }
            entries.add(new BarEntry(i, value));
        }
        return entries;
    }

    private ArrayList<Entry> generateLineData(List<DailyAggregate> aggregates, String field) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < aggregates.size(); i++) {
            float value = 0;
            switch (field) {
                case "ec":
                    value = aggregates.get(i).avg_ec;
                    break;
                case "moisture":
                    value = aggregates.get(i).avg_moisture;
                    break;
                case "temperature":
                    value = aggregates.get(i).avg_temperature;
                    break;
                case "ph":
                    value = aggregates.get(i).avg_ph;
                    break;
            }
            entries.add(new Entry(i, value));
        }
        return entries;
    }

    private void setupBarChart(BarChart chart, ArrayList<BarEntry> data, String label, int color) {
        if (chart == null) {
            Log.e(TAG, "BarChart is null for " + label);
            return;
        }
        BarDataSet dataSet = new BarDataSet(data, label);
        dataSet.setColor(color);
        BarData barData = new BarData(dataSet);
        chart.setData(barData);

        customizeChart(chart);
    }

    private void setupLineChart(LineChart chart, ArrayList<Entry> data, String label, int color) {
        if (chart == null) {
            Log.e(TAG, "LineChart is null for " + label);
            return;
        }
        LineDataSet dataSet = new LineDataSet(data, label);
        dataSet.setColor(color);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        customizeChart(chart);
    }

    private void customizeChart(BarChart chart) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(daysOfWeek.length);
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(Color.WHITE);
        chart.invalidate();
    }

    private void customizeChart(LineChart chart) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(daysOfWeek.length);
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(Color.WHITE);
        chart.invalidate();
    }

    private void clearCharts(View view) {
        clearLineChart((LineChart) view.findViewById(R.id.chart_ec));
        clearLineChart((LineChart) view.findViewById(R.id.chart_moisture));
        clearBarChart((BarChart) view.findViewById(R.id.chart_nitrogen));
        clearBarChart((BarChart) view.findViewById(R.id.chart_phosphorous));
        clearBarChart((BarChart) view.findViewById(R.id.chart_potassium));
        clearLineChart((LineChart) view.findViewById(R.id.chart_temperature));
        clearLineChart((LineChart) view.findViewById(R.id.chart_ph));
    }

    private void clearBarChart(BarChart chart) {
        if (chart != null) {
            chart.clear();
            chart.invalidate();
        }
    }

    private void clearLineChart(LineChart chart) {
        if (chart != null) {
            chart.clear();
            chart.invalidate();
        }
    }
}
