package sg.edu.np.mad.greencycle.Analytics;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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
import java.util.Random;

import sg.edu.np.mad.greencycle.R;

public class Week_charts extends Fragment {
    private Calendar currentWeek = Calendar.getInstance();
    private TextView weekDateTextView;
    private ImageButton btnNextWeek;
    private String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat","Sun"};

    public Week_charts() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week_charts, container, false);

        weekDateTextView = view.findViewById(R.id.Weekdate);
        btnNextWeek = view.findViewById(R.id.btnNextWeek);
        ImageButton btnPreviousWeek = view.findViewById(R.id.btnPreviousWeek);

        weekDateTextView.setOnClickListener(v -> showDatePickerDialog());

        btnPreviousWeek.setOnClickListener(v -> {
            adjustWeek(-1);
            setupCharts(view); // Refresh charts with new data
        });

        btnNextWeek.setOnClickListener(v -> {
            adjustWeek(1);
            setupCharts(view); // Refresh charts with new data
        });

        updateDateDisplay();
        setupCharts(view);
        return view;
    }

    private void showDatePickerDialog() {
        // Get the current date
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                adjustToSelectedWeek(selectedDate);
                setupCharts(getView());
            }
        }, year, month, day);

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
        Calendar start = (Calendar) currentWeek.clone();
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_WEEK, 6);

        // Disable the "Next" button if the end of the week is in the future
        btnNextWeek.setEnabled(!end.after(Calendar.getInstance()));

        // If the current week is displayed and today is not Sunday, adjust the end date
        if (!btnNextWeek.isEnabled() && Calendar.getInstance().get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            end = Calendar.getInstance();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
        String dateText = dateFormat.format(start.getTime()) + " - " + dateFormat.format(end.getTime());
        weekDateTextView.setText(dateText);
    }

    private void setupCharts(View view) {
        int maxDayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2; // Monday as 0
        if (!btnNextWeek.isEnabled()) {
            // If showing the current week and today is not Sunday, only show data up to today
            maxDayIndex = Math.min(maxDayIndex, Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2);
        } else {
            maxDayIndex = 6; // Full week
        }

        setupBarChart((BarChart) view.findViewById(R.id.barChart_nitrogen), generateWeeklyBarData(20, 80, maxDayIndex), "Nitrogen", Color.parseColor("#FFC0CB"));
        setupLineChart((LineChart) view.findViewById(R.id.lineChart_potassium), generateWeeklyLineData(10, 60, maxDayIndex), "Potassium", Color.parseColor("#FF69B4"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_phosphorous), generateWeeklyBarData(15, 55,maxDayIndex), "Phosphorous", Color.parseColor("#DB7093"));
        setupLineChart((LineChart) view.findViewById(R.id.lineChart_temperature), generateWeeklyLineData(10, 30,maxDayIndex), "Temperature", Color.parseColor("#FFC0CB"));
        setupBarChart((BarChart) view.findViewById(R.id.barChart_humidity), generateWeeklyBarData(40, 100,maxDayIndex), "Humidity", Color.parseColor("#FF69B4"));
        setupLineChart((LineChart) view.findViewById(R.id.lineChart_ph), generateWeeklyLineData(4, 9,maxDayIndex), "pH Level", Color.parseColor("#DB7093"));
    }

    private void setupBarChart(BarChart chart, ArrayList<BarEntry> data, String label, int color) {
        BarDataSet dataSet = new BarDataSet(data, label);
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.WHITE); // Set text color to white
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        customizeChart(chart, label);
    }

    private void setupLineChart(LineChart chart, ArrayList<Entry> data, String label, int color) {
        LineDataSet dataSet = new LineDataSet(data, label);
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.WHITE); // Set text color to white
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(color);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        customizeChart(chart, label);
    }





    private void customizeChart(BarChart chart, String title) {
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setGranularity(1f);
        chart.getDescription().setEnabled(true);

        chart.getDescription().setText(title);
        chart.getDescription().setTextSize(16f); // Set the title size larger
        chart.getDescription().setTextAlign(Paint.Align.CENTER);
        chart.setNoDataText("No data for the current week");

        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(Color.WHITE);
        chart.invalidate(); // Refresh the chart
    }

    private void customizeChart(LineChart chart, String title) {
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setGranularity(1f);
        chart.getDescription().setEnabled(true);
        chart.getDescription().setText(title);
        chart.getDescription().setTextAlign(Paint.Align.CENTER);
        chart.setNoDataText("No data for the current week");
        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(Color.WHITE);
        chart.invalidate(); // Refresh the chart
    }

    private ArrayList<BarEntry> generateWeeklyBarData(float min, float max, int maxDayIndex) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i <= maxDayIndex; i++) {
            float value = min + random.nextFloat() * (max - min);
            entries.add(new BarEntry(i, value));
        }
        return entries;
    }

    private ArrayList<Entry> generateWeeklyLineData(float min, float max, int maxDayIndex) {
        ArrayList<Entry> entries = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i <= maxDayIndex; i++) {
            float value = min + random.nextFloat() * (max - min);
            entries.add(new Entry(i, value));
        }
        return entries;
    }
}
