package sg.edu.np.mad.greencycle.Analytics;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

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
import java.util.Locale;
import java.util.Random;

import sg.edu.np.mad.greencycle.R;

// Oh Ern Qi S10243067K
public class Week_charts extends Fragment {
    private Calendar currentWeek = Calendar.getInstance();
    private TextView weekDateTextView;
    private ImageButton btnNextWeek;
    private String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    public Week_charts() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week_charts, container, false);

        weekDateTextView = view.findViewById(R.id.Weekdate);
        btnNextWeek = view.findViewById(R.id.btnNextWeek);
        ImageButton btnPreviousWeek = view.findViewById(R.id.btnPreviousWeek);

        weekDateTextView.setOnClickListener(v -> showDatePickerDialog());
        btnPreviousWeek.setOnClickListener(v -> {
            adjustWeek(-1);
            setupCharts(view);
        });

        btnNextWeek.setOnClickListener(v -> {
            adjustWeek(1);
            setupCharts(view);
        });

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
            setupCharts(getView());
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMaxDate(today.getTimeInMillis());
        datePickerDialog.show();
    }

    private void adjustToSelectedWeek(Calendar selectedDate) {
        int dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK) - selectedDate.getFirstDayOfWeek();
        selectedDate.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
        currentWeek.setTime(selectedDate.getTime());
        updateDateDisplay();
    }

    private void adjustWeek(int amount) {
        currentWeek.add(Calendar.WEEK_OF_YEAR, amount);
        updateDateDisplay();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        weekDateTextView.setText(dateFormat.format(currentWeek.getTime()));
    }

    private void setupCharts(View view) {
        int maxDayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2; // Monday as 0
        if (!btnNextWeek.isEnabled()) {
            // If showing the current week and today is not Sunday, only show data up to today
            maxDayIndex = Math.min(maxDayIndex, Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2);
        } else {
            maxDayIndex = 6; // Full week
        }

        setupBarChart((BarChart) view.findViewById(R.id.barChart_nitrogen), generateWeeklyBarData(20, 80), "Nitrogen", Color.parseColor("#FFC0CB"));
        setupLineChart((LineChart) view.findViewById(R.id.lineChart_potassium), generateWeeklyLineData(10, 60), "Potassium", Color.parseColor("#FF69B4"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_phosphorous), generateWeeklyBarData(15, 55), "Phosphorous", Color.parseColor("#DB7093"));
        setupLineChart((LineChart) view.findViewById(R.id.lineChart_temperature), generateWeeklyLineData(10, 30), "Temperature", Color.parseColor("#FFC0CB"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_humidity), generateWeeklyBarData(40, 100), "Humidity", Color.parseColor("#FF69B4"));
        setupLineChart((LineChart) view.findViewById(R.id.lineChart_ph), generateWeeklyLineData(4, 9), "pH Level", Color.parseColor("#DB7093"));
    }

    private ArrayList<BarEntry> generateWeeklyBarData(float min, float max) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, min + random.nextFloat() * (max - min)));
        }
        return entries;
    }

    private ArrayList<Entry> generateWeeklyLineData(float min, float max) {
        ArrayList<Entry> entries = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 7; i++) {
            entries.add(new Entry(i, min + random.nextFloat() * (max - min)));
        }
        return entries;
    }

    private void setupBarChart(BarChart chart, ArrayList<BarEntry> data, String label, int color) {
        BarDataSet dataSet = new BarDataSet(data, label);
        dataSet.setColor(color);
        BarData barData = new BarData(dataSet);
        chart.setData(barData);

        customizeChart(chart);
    }

    private void setupLineChart(LineChart chart, ArrayList<Entry> data, String label, int color) {
        LineDataSet dataSet = new LineDataSet(data, label);
        dataSet.setColor(color);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        customizeChart(chart);
    }

    private void customizeChart(BarChart chart) {
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(Color.WHITE);
        chart.invalidate();
    }

    private void customizeChart(LineChart chart) {
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(Color.WHITE);
        chart.invalidate();
    }
}
