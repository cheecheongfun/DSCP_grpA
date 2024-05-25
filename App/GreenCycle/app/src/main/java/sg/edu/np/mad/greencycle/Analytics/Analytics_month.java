package sg.edu.np.mad.greencycle.Analytics;

import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
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

public class Analytics_month extends Fragment {
    private Calendar currentMonth = Calendar.getInstance();
    private TextView monthDateTextView;
    private ImageButton btnNextMonth;

    public Analytics_month() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics_month, container, false);

        monthDateTextView = view.findViewById(R.id.Monthdate);
        if (monthDateTextView == null) {
            Log.e("AnalyticsMonth", "monthDateTextView is null");
        } else {
            monthDateTextView.setOnClickListener(v -> showDatePickerDialog());
        }

        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        if (btnNextMonth == null) {
            Log.e("AnalyticsMonth", "btnNextMonth is null");
        }

        ImageButton btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        if (btnPreviousMonth == null) {
            Log.e("AnalyticsMonth", "btnPreviousMonth is null");
        } else {
            btnPreviousMonth.setOnClickListener(v -> adjustMonth(-1));
        }

        currentMonth = Calendar.getInstance(); // Initialize currentMonth
        updateDateDisplay(); // Update display initially
        setupCharts(view);
        checkButtonState(); // Check the state of the next month button

        return view;
    }

    private void showDatePickerDialog() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, monthOfYear, 1); // Set to the first day of the month selected
            adjustToSelectedMonth(selectedDate);
            setupCharts(getView());
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), 1);

        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.show();
    }

    private void adjustToSelectedMonth(Calendar selectedMonth) {
        currentMonth.setTime(selectedMonth.getTime());
        updateDateDisplay();
        checkButtonState();
    }

    private void adjustMonth(int amount) {
        currentMonth.add(Calendar.MONTH, amount);
        updateDateDisplay();
        setupCharts(getView());
        checkButtonState();
    }

    private void checkButtonState() {
        // Only enable the next month button if the currentMonth is before the actual current month
        Calendar nextMonth = (Calendar) currentMonth.clone();
        nextMonth.add(Calendar.MONTH, 1);
        btnNextMonth.setEnabled(!nextMonth.after(Calendar.getInstance()));
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        monthDateTextView.setText(dateFormat.format(currentMonth.getTime()));
    }


    private void setupCharts(View view) {
        // Setup Bar and Line Charts with appropriate data generation for each month
        setupBarChart((BarChart) view.findViewById(R.id.barChart_nitrogen),
                generateMonthlyBarData(20, 80),
                "Nitrogen",
                Color.parseColor("#FFC0CB"));

        setupLineChart((LineChart) view.findViewById(R.id.lineChart_potassium),
                generateMonthlyLineData(10, 60),
                "Potassium",
                Color.parseColor("#FF69B4"));

        setupBarChart((BarChart) view.findViewById(R.id.barChart_phosphorous),
                generateMonthlyBarData(15, 55),
                "Phosphorous",
                Color.parseColor("#DB7093"));

        setupLineChart((LineChart) view.findViewById(R.id.lineChart_temperature),
                generateMonthlyLineData(10, 30),
                "Temperature",
                Color.parseColor("#FFC0CB"));

        setupBarChart((BarChart) view.findViewById(R.id.barChart_humidity),
                generateMonthlyBarData(40, 100),
                "Humidity",
                Color.parseColor("#FF69B4"));

        setupLineChart((LineChart) view.findViewById(R.id.lineChart_ph),
                generateMonthlyLineData(4, 9),
                "pH Level",
                Color.parseColor("#DB7093"));
    }

    private ArrayList<BarEntry> generateMonthlyBarData(float min, float max) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 12; i++) { // Assumes data for 12 months
            entries.add(new BarEntry(i, min + random.nextFloat() * (max - min)));
        }
        return entries;
    }


    private ArrayList<Entry> generateMonthlyLineData(float min, float max) {
        ArrayList<Entry> entries = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 12; i++) { // Assumes data for 12 months
            entries.add(new Entry(i, min + random.nextFloat() * (max - min)));
        }
        return entries;
    }


    private void setupBarChart(BarChart chart, ArrayList<BarEntry> data, String label, int color) {
        BarDataSet dataSet = new BarDataSet(data, label);
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.WHITE);
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        customizeChart(chart, label);
    }

    private void setupLineChart(LineChart chart, ArrayList<Entry> data, String label, int color) {
        LineDataSet dataSet = new LineDataSet(data, label);
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(color);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        customizeChart(chart, label);
    }

    private void customizeChart(Chart<?> chart, String title) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"}));
        xAxis.setGranularity(1f);
        chart.getLeft();
        chart.getRight();
        chart.getDescription().setText(title);
        chart.getDescription().setTextSize(16f);
        chart.getDescription().setTextAlign(Paint.Align.CENTER);
        chart.setNoDataText("No data for the current month");
        chart.setBackgroundColor(Color.WHITE);
        chart.invalidate(); // Refresh the chart
    }
}
