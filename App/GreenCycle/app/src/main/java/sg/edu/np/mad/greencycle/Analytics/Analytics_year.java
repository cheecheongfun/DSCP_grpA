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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.R;

public class Analytics_year extends Fragment {
    private static final String TAG = "Analytics_year";
    private Calendar currentYear = Calendar.getInstance();
    private TextView yearDateTextView;
    private ImageButton btnNextYear, btnPreviousYear;
    private AppDatabase db;
    private HourlyDataDao hourlyDataDao;
    private Executor databaseExecutor;
    private Tank tank;

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
        View view = inflater.inflate(R.layout.fragment_analytics_year, container, false);

        if (getArguments() != null) {
            tank = getArguments().getParcelable("tank");
        }

        yearDateTextView = view.findViewById(R.id.YearDate);
        btnNextYear = view.findViewById(R.id.btnNextYear);
        btnPreviousYear = view.findViewById(R.id.btnPreviousYear);

        yearDateTextView.setOnClickListener(v -> showDatePickerDialog());
        btnPreviousYear.setOnClickListener(v -> adjustYear(-1));
        btnNextYear.setOnClickListener(v -> adjustYear(1));

        updateDateDisplay();
        fetchAndDisplayYearlyData();
        checkButtonState();
        return view;
    }

    private void showDatePickerDialog() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, Calendar.JANUARY, 1);
            if (!selectedDate.after(today)) {
                adjustToSelectedYear(selectedDate);
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.getDatePicker().setMaxDate(today.getTimeInMillis());
        datePickerDialog.show();
    }

    private void adjustToSelectedYear(Calendar selectedYear) {
        currentYear.setTime(selectedYear.getTime());
        updateDateDisplay();
        fetchAndDisplayYearlyData();
        checkButtonState();
    }

    private void adjustYear(int amount) {
        currentYear.add(Calendar.YEAR, amount);
        updateDateDisplay();
        fetchAndDisplayYearlyData();
        checkButtonState();
    }

    private void checkButtonState() {
        Calendar nextYear = (Calendar) currentYear.clone();
        nextYear.add(Calendar.YEAR, 1);
        Calendar today = Calendar.getInstance();

        boolean isNextYearAllowed = (nextYear.get(Calendar.YEAR) <= today.get(Calendar.YEAR));

        btnNextYear.setEnabled(isNextYearAllowed);
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        yearDateTextView.setText(dateFormat.format(currentYear.getTime()));
    }

    private void fetchAndDisplayYearlyData() {
        databaseExecutor.execute(() -> {
            Calendar startCal = (Calendar) currentYear.clone();
            startCal.set(Calendar.DAY_OF_YEAR, 1);
            String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startCal.getTime());

            Calendar endCal = (Calendar) currentYear.clone();
            endCal.set(Calendar.DAY_OF_YEAR, endCal.getActualMaximum(Calendar.DAY_OF_YEAR));
            String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endCal.getTime());

            List<MonthlyAggregate> aggregates = hourlyDataDao.getMonthlyAggregates(startDate, endDate, tank.getDeviceID());

            if (aggregates == null || aggregates.isEmpty()) {
                Log.d(TAG, "No aggregates found for the selected year.");
            } else {
                Log.d(TAG, "Aggregates found: " + aggregates.size());
                for (MonthlyAggregate aggregate : aggregates) {
                    Log.d(TAG, "Month: " + aggregate.month + ", EC: " + aggregate.avg_ec);
                }
            }

            getActivity().runOnUiThread(() -> {
                if (getView() != null) {
                    setupCharts(getView(), aggregates);
                }
            });
        });
    }

    private void setupCharts(View view, List<MonthlyAggregate> aggregates) {
        // Setup Bar Charts with DAO data for each month
        setupBarChart((BarChart) view.findViewById(R.id.barChart_ec), getBarEntries(aggregates, "avg_ec"), "EC Levels", Color.parseColor("#FFA07A"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_moisture), getBarEntries(aggregates, "avg_moisture"), "Moisture Levels", Color.parseColor("#20B2AA"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_nitrogen), getBarEntries(aggregates, "avg_nitrogen"), "Nitrogen Levels", Color.parseColor("#FFC0CB"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_phosphorous), getBarEntries(aggregates, "avg_phosphorous"), "Phosphorous Levels", Color.parseColor("#DB7093"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_potassium), getBarEntries(aggregates, "avg_potassium"), "Potassium Levels", Color.parseColor("#FF69B4"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_temperature), getBarEntries(aggregates, "avg_temperature"), "Temperature Levels", Color.parseColor("#32CD32"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_ph), getBarEntries(aggregates, "avg_ph"), "pH Levels", Color.parseColor("#90EE90"));
    }

    private ArrayList<BarEntry> getBarEntries(List<MonthlyAggregate> aggregates, String field) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        String[] months = getMonthsOfYear();

        // Ensure all months are represented
        for (int i = 0; i < months.length; i++) {
            entries.add(new BarEntry(i, 0));
        }

        for (MonthlyAggregate aggregate : aggregates) {
            float value = 0;
            switch (field) {
                case "avg_ec":
                    value = aggregate.avg_ec;
                    break;
                case "avg_moisture":
                    value = aggregate.avg_moisture;
                    break;
                case "avg_nitrogen":
                    value = aggregate.avg_nitrogen;
                    break;
                case "avg_phosphorous":
                    value = aggregate.avg_phosphorous;
                    break;
                case "avg_potassium":
                    value = aggregate.avg_potassium;
                    break;
                case "avg_temperature":
                    value = aggregate.avg_temperature;
                    break;
                case "avg_ph":
                    value = aggregate.avg_ph;
                    break;
            }

            // Extract month index from aggregate.month
            String[] split = aggregate.month.split("-");
            if (split.length == 2) {
                int monthIndex = Integer.parseInt(split[1]) - 1; // Month index (0-based)
                Log.d(TAG, "Adding entry: monthIndex = " + monthIndex + ", value = " + value);
                entries.set(monthIndex, new BarEntry(monthIndex, value));
            }
        }

        // Log final entries
        for (int i = 0; i < entries.size(); i++) {
            Log.d(TAG, "Entry at index " + i + ": " + entries.get(i).getY());
        }

        return entries;
    }


    private int getMonthIndex(String month) {
        String[] months = getMonthsOfYear();
        for (int i = 0; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(month)) {
                return i;
            }
        }
        return -1;
    }

    private void setupBarChart(BarChart chart, ArrayList<BarEntry> data, String label, int color) {
        BarDataSet dataSet = new BarDataSet(data, label);
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.WHITE);
        BarData barData = new BarData(dataSet);
        chart.setData(barData);

        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(getMonthsOfYear()));
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setGranularityEnabled(true);

        chart.getAxisLeft().setEnabled(true);
        chart.getAxisRight().setEnabled(false);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.setTouchEnabled(true); // Enable touch interactions
        chart.setDragEnabled(true); // Enable dragging
        chart.setScaleEnabled(false); // Disable scaling
        chart.setPinchZoom(false); // Disable pinch zoom
        chart.setDoubleTapToZoomEnabled(false); // Disable double-tap zoom
        chart.setHighlightPerDragEnabled(false); // Disable highlight on drag
        chart.setHighlightPerTapEnabled(false); // Disable highlight on tap

        // Log data to ensure it is being set
        Log.d(TAG, "Data for chart " + label + ": " + barData.getDataSetCount());

        chart.invalidate(); // Refresh the chart
    }



    private String[] getMonthsOfYear() {
        return new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    }
}
