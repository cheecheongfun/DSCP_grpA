package sg.edu.np.mad.greencycle.Analytics;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
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

public class Analytics_month extends Fragment {
    private Calendar currentMonth = Calendar.getInstance();
    private TextView monthDateTextView;
    private ImageButton btnNextMonth, btnPreviousMonth;
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
        View view = inflater.inflate(R.layout.fragment_analytics_month, container, false);

        if (getArguments() != null) {
            tank = getArguments().getParcelable("tank");
        }

        monthDateTextView = view.findViewById(R.id.Monthdate);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);

        monthDateTextView.setOnClickListener(v -> showDatePickerDialog());
        btnPreviousMonth.setOnClickListener(v -> adjustMonth(-1));
        btnNextMonth.setOnClickListener(v -> adjustMonth(1));

        updateDateDisplay();
        fetchAndDisplayMonthlyData();
        checkButtonState();
        return view;
    }

    private void showDatePickerDialog() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, monthOfYear, 1);
            if (!selectedDate.after(today)) {
                adjustToSelectedMonth(selectedDate);
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.getDatePicker().setMaxDate(today.getTimeInMillis());
        datePickerDialog.show();
    }

    private void adjustToSelectedMonth(Calendar selectedMonth) {
        currentMonth.setTime(selectedMonth.getTime());
        updateDateDisplay();
        fetchAndDisplayMonthlyData();
        checkButtonState();
    }

    private void adjustMonth(int amount) {
        currentMonth.add(Calendar.MONTH, amount);
        updateDateDisplay();
        fetchAndDisplayMonthlyData();
        checkButtonState();
    }

    private void checkButtonState() {
        Calendar nextMonth = (Calendar) currentMonth.clone();
        nextMonth.add(Calendar.MONTH, 1);
        Calendar today = Calendar.getInstance();

        boolean isNextMonthAllowed = (nextMonth.get(Calendar.YEAR) < today.get(Calendar.YEAR)) ||
                (nextMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR) && nextMonth.get(Calendar.MONTH) <= today.get(Calendar.MONTH));

        btnNextMonth.setEnabled(isNextMonthAllowed);
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        monthDateTextView.setText(dateFormat.format(currentMonth.getTime()));
    }

    private void fetchAndDisplayMonthlyData() {
        databaseExecutor.execute(() -> {
            Calendar startCal = (Calendar) currentMonth.clone();
            startCal.set(Calendar.DAY_OF_MONTH, 1);
            String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startCal.getTime());

            Calendar endCal = (Calendar) currentMonth.clone();
            endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
            String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endCal.getTime());

            List<DailyAggregate> aggregates = hourlyDataDao.getDailyAggregates(startDate, endDate, tank.getDeviceID());

            getActivity().runOnUiThread(() -> {
                setupCharts(getView(), aggregates);
            });
        });
    }

    private void setupCharts(View view, List<DailyAggregate> aggregates) {
        // Setup Bar Charts with DAO data for each month
        setupBarChart((BarChart) view.findViewById(R.id.barChart_ec), getBarEntries(aggregates, "avg_ec"), "EC Levels", Color.parseColor("#FFA07A"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_moisture), getBarEntries(aggregates, "avg_moisture"), "Moisture Levels", Color.parseColor("#20B2AA"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_nitrogen), getBarEntries(aggregates, "avg_nitrogen"), "Nitrogen Levels", Color.parseColor("#FFC0CB"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_phosphorous), getBarEntries(aggregates, "avg_phosphorous"), "Phosphorous Levels", Color.parseColor("#DB7093"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_potassium), getBarEntries(aggregates, "avg_potassium"), "Potassium Levels", Color.parseColor("#FF69B4"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_temperature), getBarEntries(aggregates, "avg_temperature"), "Temperature Levels", Color.parseColor("#32CD32"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_ph), getBarEntries(aggregates, "avg_ph"), "pH Levels", Color.parseColor("#90EE90"));
    }

    private ArrayList<BarEntry> getBarEntries(List<DailyAggregate> aggregates, String field) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < aggregates.size(); i++) {
            DailyAggregate aggregate = aggregates.get(i);
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
            entries.add(new BarEntry(i, value));
        }
        return entries;
    }

    private void setupBarChart(BarChart chart, ArrayList<BarEntry> data, String label, int color) {
        BarDataSet dataSet = new BarDataSet(data, label);
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.WHITE);
        BarData barData = new BarData(dataSet);
        chart.setData(barData);

        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(getDaysOfMonth()));
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

        chart.invalidate(); // Refresh the chart
    }

    private String[] getDaysOfMonth() {
        int daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        String[] days = new String[daysInMonth];
        for (int i = 0; i < daysInMonth; i++) {
            days[i] = String.valueOf(i + 1);
        }
        return days;
    }
}
