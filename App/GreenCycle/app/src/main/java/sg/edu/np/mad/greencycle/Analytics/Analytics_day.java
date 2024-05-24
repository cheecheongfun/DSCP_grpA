package sg.edu.np.mad.greencycle.Analytics;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Random;

import sg.edu.np.mad.greencycle.R;

public class Analytics_day extends Fragment {

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
        return view;
    }

    private void setupCharts(View view) {
        int hours = 100; // Increased number of hours to demonstrate scrolling

        // Setting up each chart with specific settings
        setupChart((LineChart) view.findViewById(R.id.chart_nitrogen),
                generateFakeData(20, 80, hours), "Nitrogen Levels",
                Color.parseColor("#FFC0CB"), Color.WHITE, "Nitrogen");

        setupChart((LineChart) view.findViewById(R.id.chart_potassium),
                generateFakeData(10, 60, hours), "Potassium Levels",
                Color.parseColor("#FF69B4"), Color.WHITE, "Potassium");

        setupChart((LineChart) view.findViewById(R.id.chart_phosphorous),
                generateFakeData(15, 50, hours), "Phosphorous Levels",
                Color.parseColor("#DB7093"), Color.WHITE, "Phosphorous");

        setupChart((LineChart) view.findViewById(R.id.chart_temperature),
                generateFakeData(10, 30, hours), "Temperature Levels",
                Color.parseColor("#32CD32"), Color.WHITE, "Temperature");

        setupChart((LineChart) view.findViewById(R.id.chart_humidity),
                generateFakeData(40, 100, hours), "Humidity Levels",
                Color.parseColor("#00FF7F"), Color.WHITE, "Humidity");

        setupChart((LineChart) view.findViewById(R.id.chart_ph),
                generateFakeData(4, 9, hours), "pH Levels",
                Color.parseColor("#90EE90"), Color.WHITE, "pH");
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

