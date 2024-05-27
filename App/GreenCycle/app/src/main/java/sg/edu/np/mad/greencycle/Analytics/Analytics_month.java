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
    private ImageButton btnNextMonth, btnPreviousMonth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics_month, container, false);

        monthDateTextView = view.findViewById(R.id.Monthdate);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);

        monthDateTextView.setOnClickListener(v -> showDatePickerDialog());
        btnPreviousMonth.setOnClickListener(v -> adjustMonth(-1));
        btnNextMonth.setOnClickListener(v -> adjustMonth(1));

        updateDateDisplay();
        setupCharts(view);
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
                setupCharts(getView());
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
        checkButtonState();
    }

    private void adjustMonth(int amount) {
        currentMonth.add(Calendar.MONTH, amount);
        updateDateDisplay();
        setupCharts(getView());
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

    private void setupCharts(View view) {
        setupBarChart((BarChart) view.findViewById(R.id.barChart_nitrogen), generateMonthlyBarData(20, 80), "Nitrogen", Color.parseColor("#FFC0CB"));
        setupLineChart((LineChart) view.findViewById(R.id.lineChart_potassium), generateMonthlyLineData(10, 60), "Potassium", Color.parseColor("#FF69B4"));
    }

    private ArrayList<BarEntry> generateMonthlyBarData(float min, float max) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i, min + random.nextFloat() * (max - min)));
        }
        return entries;
    }

    private ArrayList<Entry> generateMonthlyLineData(float min, float max) {
        ArrayList<Entry> entries = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
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
        customizeChart(chart);
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
        customizeChart(chart);
    }

    private void customizeChart(BarChart chart) {
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"}));
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(Color.WHITE);
        chart.invalidate();
    }

    private void customizeChart(LineChart chart) {
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"}));
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(Color.WHITE);
        chart.invalidate();
    }
}