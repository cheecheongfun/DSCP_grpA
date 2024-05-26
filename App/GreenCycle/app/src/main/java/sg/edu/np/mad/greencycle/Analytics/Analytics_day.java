package sg.edu.np.mad.greencycle.Analytics;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import sg.edu.np.mad.greencycle.R;

public class Analytics_day extends Fragment {
    TextView todaydate;

    public Analytics_day() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics_day, container, false);
        setupCharts(view);

        todaydate = view.findViewById(R.id.Date);
        updateCurrentDate();

        // Add click listener to the date TextView
        todaydate.setOnClickListener(v -> showDatePickerDialog());

        return view;
    }

    private void updateCurrentDate() {
        String currentDate = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(new Date());
        todaydate.setText(currentDate);
    }

    private void showDatePickerDialog() {
        // Create a Calendar object to get the current year, month, and day
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog that sets the date
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, monthOfYear, dayOfMonth);
            updateDate(selectedDate.getTime());
        }, year, month, day);

        datePickerDialog.show();
    }

    private void updateDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        todaydate.setText(dateFormat.format(date));

        // Update your data based on the selected date
        setupCharts(getView());
    }


    private void setupCharts(View view) {
        int hours = 100; // Increased number of hours to demonstrate scrolling

        // Setting up each chart with specific settings
        setupChart((LineChart) view.findViewById(R.id.chart_nitrogen),
                generateFakeData(20, 80, hours), "Nitrogen Levels",
                Color.parseColor("#FFC0CB"), Color.WHITE, "Nitrogen Chart");

        setupChart((LineChart) view.findViewById(R.id.chart_potassium),
                generateFakeData(10, 60, hours), "Potassium Levels",
                Color.parseColor("#FF69B4"), Color.WHITE, "Potassium Chart");

        setupChart((LineChart) view.findViewById(R.id.chart_phosphorous),
                generateFakeData(15, 50, hours), "Phosphorous Levels",
                Color.parseColor("#DB7093"), Color.WHITE, "Phosphorous Chart");

        setupChart((LineChart) view.findViewById(R.id.chart_temperature),
                generateFakeData(10, 30, hours), "Temperature Levels",
                Color.parseColor("#32CD32"), Color.WHITE, "Temperature Chart");

        setupChart((LineChart) view.findViewById(R.id.chart_humidity),
                generateFakeData(40, 100, hours), "Humidity Levels",
                Color.parseColor("#00FF7F"), Color.WHITE, "Humidity Chart");

        setupChart((LineChart) view.findViewById(R.id.chart_ph),
                generateFakeData(4, 9, hours), "pH Levels",
                Color.parseColor("#90EE90"), Color.WHITE, "pH Chart");
    }

    private void setupChart(LineChart chart, ArrayList<Entry> data, String label, int color, int backgroundColor, String title) {
        LineDataSet dataSet = new LineDataSet(data, label);
        dataSet.setColor(color);
        dataSet.setValueTextColor(color);
        dataSet.setValueTextSize(10f); // Increased text size for data values
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(color);
        dataSet.setCircleRadius(4f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        chart.getDescription().setEnabled(true);
        chart.getDescription().setText(title);
        chart.getDescription().setTextColor(color);
        chart.getDescription().setTextSize(16f); // Set the title size larger
        chart.getDescription().setTextAlign(Paint.Align.CENTER); // Align text to center

        chart.getLegend().setEnabled(false);
        chart.setBackgroundColor(backgroundColor);

        customizeAxis(chart, Color.BLACK);

        // Enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(false);
        chart.setVisibleXRangeMaximum(10); // Shows 10 data points at a time for scrolling

        chart.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure the chart has been laid out so getWidth() doesn't return 0
                chart.getDescription().setPosition(chart.getWidth() / 2f, 40f);
                chart.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        chart.moveViewToX(0);
    }

    private void customizeAxis(LineChart chart, int textColor) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new HourAxisValueFormatter());
        xAxis.setGranularity(1f); // Ensure granularity is set to 1 to have a label per hour
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(textColor);
        xAxis.setGridColor(Color.LTGRAY);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setLabelRotationAngle(-45); // Optional: rotate labels for better visibility

        chart.getAxisLeft().setTextColor(textColor);
        chart.getAxisLeft().setGridColor(Color.LTGRAY);
        chart.getAxisLeft().setDrawAxisLine(true);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
    }


    private ArrayList<Entry> generateFakeData(float min, float max, int count) {
        ArrayList<Entry> entries = new ArrayList<>();
        Random random = new Random();
        // Limit count to 24 to stop at 23:00
        int maxCount = Math.min(count, 24);
        for (int i = 0; i < maxCount; i++) {
            float value = min + random.nextFloat() * (max - min);
            entries.add(new Entry(i, value));
        }
        return entries;
    }


    class HourAxisValueFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            int hour = (int) value;
            if (hour >= 24) {
                // Do not display labels beyond 23:00
                return "";
            }
            return String.format("%02d:00", hour % 24);
        }
    }


}

