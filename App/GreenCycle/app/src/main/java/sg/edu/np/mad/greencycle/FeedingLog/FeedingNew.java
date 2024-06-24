package sg.edu.np.mad.greencycle.FeedingLog;


import android.app.AlertDialog;
import android.app.admin.SecurityLog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.core.Week;
import com.kizitonwose.calendar.core.WeekDay;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.WeekCalendarView;
import com.kizitonwose.calendar.view.WeekDayBinder;
import com.kizitonwose.calendar.view.WeekHeaderFooterBinder;

import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import sg.edu.np.mad.greencycle.Classes.FeedSchedule;
import sg.edu.np.mad.greencycle.Classes.Food;
import sg.edu.np.mad.greencycle.Classes.Log;
import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.LiveData;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.StartUp.RegistrationPage;
import sg.edu.np.mad.greencycle.TankSelection.TankSelection;

public class FeedingNew extends AppCompatActivity {

    CalendarView calendarView;
    WeekCalendarView weekCalendarView;
    RelativeLayout logs1, logs2, details, details1, fixedTitleContainer, titlesContainer;
    TextView toggleViewButton, backBtn, waterAmt, notesFixed, noLogs, waterAmt1, notesFixed1, noLogs1, monthHead, noSched1, noSched2;
    boolean isCalendarView = true;
    boolean isSelected = true;
    LocalDate selectedDate;
    LogAdapterNew gAdapter, bAdapter, sAdapter;
    ArrayList<DayOfWeek> daysOfWeek;
    ArrayList<Food> greens, browns;
    ArrayList<FeedSchedule> scheduleList;
    RecyclerView gRecycler, bRecycler, gRecycler1, bRecycler1, sRecycler1, sRecycler2;
    ImageButton gAdd, bAdd,gAdd1, bAdd1;
    FirebaseDatabase database;
    DatabaseReference reference;
    User user;
    Tank tank;
    EditText editWater, editNotes, editWater1, editNotes1;
    Button confirm, schedule, schedule1;
    String today, viewType;
    LinearLayout dayHead;
    DayViewContainer selectedDateContainer = null;
    int selectedIndex, targetTankID;
    ArrayList<Log> feedingLog;
    ArrayList<String> green,brown, greenFood, brownFood;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.feeding_log1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.feedingCalendar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");
        targetTankID = tank.getTankID();
        calendarView = findViewById(R.id.calendarView);
        weekCalendarView = findViewById(R.id.weekCalendarView);
        logs1 = findViewById(R.id.logs1);
        logs2 = findViewById(R.id.logs2);
        schedule = findViewById(R.id.schedule1);
        schedule1 = findViewById(R.id.schedule2);
        toggleViewButton = findViewById(R.id.toggle);
        backBtn = findViewById(R.id.backButton);
        confirm = findViewById(R.id.bottomBtn);
        fixedTitleContainer = findViewById(R.id.fixedTitleContainer);
        titlesContainer = fixedTitleContainer.findViewById(R.id.titlesContainer);
        dayHead = titlesContainer.findViewById(R.id.dayTitles);
        monthHead = titlesContainer.findViewById(R.id.monthTitles);
//        monthHeader = findViewById(R.id.monthHeader);

        // log 1
        gRecycler = logs1.findViewById(R.id.greenRecycler);
        bRecycler = logs1.findViewById(R.id.brownRecycler);
        gAdd = logs1.findViewById(R.id.addGreen);
        bAdd = logs1.findViewById(R.id.addBrown);
        editWater = logs1.findViewById(R.id.editWater);
        editNotes = logs1.findViewById(R.id.notesDescription);
        waterAmt = logs1.findViewById(R.id.waterAmt);
        notesFixed = logs1.findViewById(R.id.notesFixed);
        details = logs1.findViewById(R.id.details);
        noLogs = logs1.findViewById(R.id.noLogsText);

        // log 2
        gRecycler1 = logs2.findViewById(R.id.greenRecycler);
        bRecycler1 = logs2.findViewById(R.id.brownRecycler);
        gAdd1 = logs2.findViewById(R.id.addGreen);
        bAdd1 = logs2.findViewById(R.id.addBrown);
        editWater1 = logs2.findViewById(R.id.editWater);
        editNotes1 = logs2.findViewById(R.id.notesDescription);
        waterAmt1 = logs2.findViewById(R.id.waterAmt);
        notesFixed1 = logs2.findViewById(R.id.notesFixed);
        details1 = logs2.findViewById(R.id.details);
        noLogs1 = logs2.findViewById(R.id.noLogsText);

        sRecycler1 = findViewById(R.id.sRecycler1);
        sRecycler2 = findViewById(R.id.sRecycler2);
        noSched1 = findViewById(R.id.noSched1);
        noSched2 = findViewById(R.id.noSched2);

        selectedIndex =0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        today = formatter.format(new Date());
        selectedDate = LocalDate.now();
        viewType = "month";
        loadEventsForDate(selectedDate, viewType);

        toggleViewButton.setOnClickListener(view -> toggleView());

        // day of week titles
        daysOfWeek = new ArrayList();
        daysOfWeek.add(DayOfWeek.MONDAY);
        daysOfWeek.add(DayOfWeek.TUESDAY);
        daysOfWeek.add(DayOfWeek.WEDNESDAY);
        daysOfWeek.add(DayOfWeek.THURSDAY);
        daysOfWeek.add(DayOfWeek.FRIDAY);
        daysOfWeek.add(DayOfWeek.SATURDAY);
        daysOfWeek.add(DayOfWeek.SUNDAY);

        refreshData();
        List<FeedSchedule> allSchedules = new ArrayList<>(tank.getFeedSchedule());
        HashMap<FeedSchedule, List<LocalDate>> scheduledDatesMap = generateScheduledDates(allSchedules);
        // Extract all scheduled dates for updating the view
        List<LocalDate> allScheduledDates = new ArrayList<>();
        for (List<LocalDate> dates : scheduledDatesMap.values()) {
            allScheduledDates.addAll(dates);
        }
        for (FeedSchedule schedule : scheduledDatesMap.keySet()) {
            List<LocalDate> scheduledDates = scheduledDatesMap.get(schedule);
            android.util.Log.i("FeedSchedule", schedule.getScheduleName());
            for (LocalDate date : scheduledDates) {
                android.util.Log.i("ScheduledDate", date.toString());
            }
        }

        setupCalendarView();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // saving a new or edited log
                if (confirm.getText().toString().equals("Edit Log") || confirm.getText().toString().equals("Add Log")){
                    confirm.setText("Save");
                }
                // After saved is pressed
                else {
                    confirm.setText("Edit Log");
                    loadEventsForDate(selectedDate, viewType);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FeedingNew.this, TankSelection.class);
                intent.putExtra("user", user);
                intent.putExtra("where", "Feeding");
                startActivity(intent);
                finish();
            }
        });

        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshData();
                android.util.Log.i("schedule", "ScheduleList" + scheduleList.size() +" " + String.valueOf(scheduleList==null));
                if (!scheduleList.isEmpty()){
                    android.util.Log.i("schedule", "In schedule if");
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.schedule_dialog, null);

                    TextView dialogTitle = dialogView.findViewById(R.id.title);
                    ListView scheduleListView = dialogView.findViewById(R.id.scheduleList);

                    dialogTitle.setText("Schedules");

                    // Use FeedScheduleAdapter to adapt the ArrayList to the ListView
                    FeedScheduleAdapter adapter = new FeedScheduleAdapter(FeedingNew.this, scheduleList);
                    scheduleListView.setAdapter(adapter);

                    AlertDialog.Builder builder = new AlertDialog.Builder(FeedingNew.this);
                    builder.setView(dialogView)
                            .setPositiveButton("Set", (dialog, which) -> {
                                // Handle Set button click
                                addSchedule(adapter.getSelectedIndex());
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                // Handle Cancel button click
                            })
                            .setNeutralButton("Add Schedule", (dialog, which) -> {
                                // Handle neutral button click
                                if (scheduleList.size() > 3){
                                    Toast.makeText(FeedingNew.this, "Limit reached", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                    String startDate = selectedDate.format(formatter);
                                    CreateSchedule dialogFragment = CreateSchedule.newInstance(user, tank, greenFood, brownFood, startDate);
                                    dialogFragment.show(getSupportFragmentManager(), "CreateSchedule");
                                    refreshData();
                                    // Extract all scheduled dates for updating the view
                                    if (isCalendarView){
                                        setupCalendarView();
                                    }
                                    else setupWeekView();
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else{
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    String startDate = selectedDate.format(formatter);
                    CreateSchedule dialogFragment = CreateSchedule.newInstance(user, tank, greenFood, brownFood, startDate);
                    dialogFragment.show(getSupportFragmentManager(), "CreateSchedule");
                    refreshData();
                    // Extract all scheduled dates for updating the view
                    if (isCalendarView){
                        setupCalendarView();
                    }
                    else setupWeekView();
                }
            }
        });
    }


    public void setupCalendarView() {
        for (int i = 0; i < dayHead.getChildCount(); i++) {
            TextView textView = (TextView) dayHead.getChildAt(i);
            DayOfWeek dayOfWeek = daysOfWeek.get(i);
            String title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault());
            textView.setText(title);
        }
        // Bind each day in the calendar
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @Override
            public DayViewContainer create(View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(DayViewContainer container, CalendarDay day) {
                container.dayText.setText(String.valueOf(day.getDate().getDayOfMonth()));
                // set all indicators for schedule
                for (LocalDate date : getAllScheduledDates(tank.getFeedSchedule())){
                    if (date.equals(LocalDate.of(day.getDate().getYear(), day.getDate().getMonth(), day.getDate().getDayOfMonth()))){
                        container.scheduleIndicator.setVisibility(View.VISIBLE);
                    }
                }
                if (day.getPosition() == DayPosition.MonthDate) {
                    container.dayText.setTextColor(Color.BLACK);
                    container.dayText.setOnClickListener(v -> {
                        toggleSelect(container, day.getDate().getYear(), day.getDate().getMonthValue());
                        if (selectedDate.isAfter(LocalDate.now())){
                            loadScheduleForDate(day.getDate(),viewType);
                        }
                        else {
                            loadEventsForDate(day.getDate(), viewType);
                        }
                    });
                } else {
                    int textColor = ContextCompat.getColor(FeedingNew.this, R.color.light_grey);
                    container.dayText.setTextColor(textColor);
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
                if (selectedDate!=null && day.getDate().isEqual(selectedDate)){
                    int backgroundColor = ContextCompat.getColor(FeedingNew.this, R.color.mid_green);
                    container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.transparent)));
                    container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.white)));
                    container.select.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
                    selectedDate = day.getDate();
                    selectedDateContainer = container;
                }
                else if (selectedDate == null && day.getDate().equals(LocalDate.now())){
                    int backgroundColor = ContextCompat.getColor(FeedingNew.this, R.color.mid_green);
                    container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.transparent)));
                    container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.white)));
                    container.select.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
                    selectedDate = day.getDate();
                    selectedDateContainer = container;
                }
            }
        });

        calendarView.setMonthScrollListener(new Function1<CalendarMonth, Unit>() {
            @Override
            public Unit invoke(CalendarMonth month) {
                YearMonth yearMonth = month.getYearMonth();
                String monthYear = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + yearMonth.getYear();
                monthHead.setText(monthYear);
                return Unit.INSTANCE;
            }
        });
        android.util.Log.i(null, "selectedDate: " + selectedDate);
        YearMonth currentMonth = YearMonth.from(selectedDate);
        YearMonth firstMonth = currentMonth.minusMonths(12);
        YearMonth lastMonth = currentMonth.plusMonths(12);

        calendarView.setup(firstMonth, lastMonth, DayOfWeek.MONDAY);
        calendarView.scrollToMonth(currentMonth);
    }


    public void setupWeekView() {
        for (int i = 0; i < dayHead.getChildCount(); i++) {
            TextView textView = (TextView) dayHead.getChildAt(i);
            DayOfWeek dayOfWeek = daysOfWeek.get(i);
            String title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault());
            textView.setText(title);
        }
        // Bind each day in the calendar
        weekCalendarView.setDayBinder(new WeekDayBinder<DayViewContainer>() {


            @Override
            public DayViewContainer create(View view) { return new DayViewContainer(view); }
            @Override
            public void bind(@NonNull DayViewContainer container, WeekDay weekDay) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
                for (LocalDate date : getAllScheduledDates(tank.getFeedSchedule())){
                    if (date.equals(LocalDate.of(weekDay.getDate().getYear(), weekDay.getDate().getMonth(), weekDay.getDate().getDayOfMonth())) && date.isAfter(LocalDate.now())){
                        container.scheduleIndicator.setVisibility(View.VISIBLE);
                    }
                }
                container.dayText.setText(String.valueOf(weekDay.getDate().getDayOfMonth()));
                container.dayText.setOnClickListener(v -> {
                    toggleSelect(container, weekDay.getDate().getYear(), weekDay.getDate().getMonthValue());
                    if (selectedDate.isAfter(LocalDate.now())){
                        loadScheduleForDate(weekDay.getDate(),viewType);
                    }
                    else {
                        loadEventsForDate(weekDay.getDate(), viewType);
                    }
                });
                if (selectedDate!=null && weekDay.getDate().isEqual(selectedDate)){
                    int backgroundColor = ContextCompat.getColor(FeedingNew.this, R.color.mid_green);
                    container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.transparent)));
                    container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.white)));
                    container.select.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
                    selectedDate = weekDay.getDate();
                    selectedDateContainer = container;
                    container.scheduleIndicator.setVisibility(View.GONE);
                    container.logIndicator.setVisibility(View.GONE);
                }
                else if (selectedDate == null && weekDay.getDate().equals(LocalDate.now())){
                    int backgroundColor = ContextCompat.getColor(FeedingNew.this, R.color.mid_green);
                    container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.transparent)));
                    container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.white)));
                    container.select.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
                    selectedDate = weekDay.getDate();
                    selectedDateContainer = container;
                }
            }

        });
        weekCalendarView.setWeekScrollListener(new Function1<Week, Unit>() {
            @Override
            public Unit invoke(Week week) {
                LocalDate firstDate = week.getDays().get(0).getDate();
                YearMonth yearMonth = YearMonth.from(firstDate);
                String monthYear = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + yearMonth.getYear();
                monthHead.setText(monthYear);
                return Unit.INSTANCE;
            }
        });

        LocalDate firstDate = selectedDate.minusWeeks(12);
        LocalDate lastDate = selectedDate.plusWeeks(12);

        weekCalendarView.setup(firstDate, lastDate, DayOfWeek.MONDAY);
        weekCalendarView.scrollToDate(selectedDate);
    }

    // load log based on Date and view type(month, week)
    private void loadEventsForDate(LocalDate date, String view) {
        android.util.Log.i("loadEvents", "in load events");
        noSched1.setVisibility(View.GONE);
        noSched2.setVisibility(View.GONE);
        sRecycler1.setVisibility(View.GONE);
        sRecycler2.setVisibility(View.GONE);
        confirm.setVisibility(View.VISIBLE);
        ArrayList<String> dateList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
        for (Log log : tank.getFeedingLog() ){
            dateList.add(log.getLogDate());
        }
        if (dateList.contains(formatter.format(date))){
            for (Log log : tank.getFeedingLog()){
                if (log.getLogDate().equals(formatter.format(date))){
                    greens.addAll(log.getGreens());
                    browns.addAll(log.getBrowns());
                    break;
                }
            }
        }
        else {
            android.util.Log.i("loadEvents", "in else");
            if (view.equals("month")){
                android.util.Log.i("loadEvents", "in month: ");
                noLogs.setVisibility(View.VISIBLE);
                noLogs1.setVisibility(View.GONE);
            }
            else {
                noLogs1.setVisibility(View.VISIBLE);
                noLogs.setVisibility(View.GONE);
            }
        }
        // month view
        if (view.equals("month")){
            logs1.setVisibility(View.VISIBLE);
            android.util.Log.i(null, "month");
            if (greens == null && browns == null){
                android.util.Log.i(null, "In Load If");
                noLogs.setVisibility(View.VISIBLE);
                confirm.setText("Add Log");
            }
            else if (greens == null){
                android.util.Log.i(null, "In Load Else If");
                gRecycler.setVisibility(View.GONE);
                bAdapter = new LogAdapterNew(browns, "brown", null);
                bRecycler.setLayoutManager(new LinearLayoutManager(this));
                bRecycler.setAdapter(bAdapter);
            }
            else if (browns == null){
                bRecycler.setVisibility(View.GONE);
                gAdapter = new LogAdapterNew(greens, "green", null);
                gRecycler.setLayoutManager(new LinearLayoutManager(this));
                gRecycler.setAdapter(gAdapter);
            }
            else {
                android.util.Log.i(null, "In Load Else");
                gAdapter = new LogAdapterNew(greens, "green", null);
                gRecycler.setLayoutManager(new LinearLayoutManager(this));
                gRecycler.setAdapter(gAdapter);

                // brown recycler view
                bAdapter = new LogAdapterNew(browns, "brown", null);
                bRecycler.setLayoutManager(new LinearLayoutManager(this));
                bRecycler.setAdapter(bAdapter);
            }
        }
        // week view
        else {
            logs2.setVisibility(View.VISIBLE);
            if (greens == null && browns == null) {
                android.util.Log.i(null, "In Load If");
                noLogs1.setVisibility(View.VISIBLE);
                confirm.setText("Add Log");
            } else if (greens == null) {
                android.util.Log.i(null, "In Load Else If");
                gRecycler1.setVisibility(View.GONE);
                bAdapter = new LogAdapterNew(browns, "brown", null);
                bRecycler1.setLayoutManager(new LinearLayoutManager(this));
                bRecycler1.setAdapter(bAdapter);
            } else if (browns == null) {
                bRecycler1.setVisibility(View.GONE);
                gAdapter = new LogAdapterNew(greens, "green", null);
                gRecycler1.setLayoutManager(new LinearLayoutManager(this));
                gRecycler1.setAdapter(gAdapter);
            } else {
                android.util.Log.i(null, "In Load Else");
                gAdapter = new LogAdapterNew(greens, "green", null);
                gRecycler1.setLayoutManager(new LinearLayoutManager(this));
                gRecycler1.setAdapter(gAdapter);

                // brown recycler view
                bAdapter = new LogAdapterNew(browns, "brown", null);
                bRecycler1.setLayoutManager(new LinearLayoutManager(this));
                bRecycler1.setAdapter(bAdapter);
            }
        }

    }

    private void loadScheduleForDate(LocalDate date, String view) {
        confirm.setVisibility(View.GONE);
        if (getAllScheduledDates(tank.getFeedSchedule()).contains(date)){
            android.util.Log.i("load Schedule for date", "In if: " );
            sAdapter = new LogAdapterNew(null, "schedule", scheduleList);
            // month view
            if (view.equals("month")){
                if (scheduleList == null || scheduleList.isEmpty()){
                    sRecycler1.setVisibility(View.GONE);
                    noSched1.setVisibility(View.VISIBLE);
                    noSched2.setVisibility(View.GONE);
                }
                else {
                    logs1.setVisibility(View.GONE);
                    sRecycler1.setVisibility(View.VISIBLE);
                    sRecycler1.setLayoutManager(new LinearLayoutManager(this));
                    sRecycler1.setAdapter(sAdapter);
                    noSched1.setVisibility(View.GONE);
                }
            }
            // week view
            else {
                if (scheduleList == null || scheduleList.isEmpty()){
                    noSched2.setVisibility(View.VISIBLE);
                    sRecycler2.setVisibility(View.GONE);
                    noSched1.setVisibility(View.GONE);
                }
                else {
                    logs2.setVisibility(View.GONE);
                    sRecycler2.setVisibility(View.VISIBLE);
                    sRecycler2.setLayoutManager(new LinearLayoutManager(this));
                    sRecycler2.setAdapter(sAdapter);
                    noSched2.setVisibility(View.GONE);
                }
            }
        }
        else {
            android.util.Log.i("load Schedule for date", "In else: " );
            if (view.equals("month")){
                noSched1.setVisibility(View.VISIBLE);
                noSched2.setVisibility(View.GONE);
            }
            else{
                noSched2.setVisibility(View.VISIBLE);
                noSched1.setVisibility(View.GONE);
            }
            sRecycler1.setVisibility(View.GONE);
            sRecycler2.setVisibility(View.GONE);
            logs1.setVisibility(View.GONE);
            logs2.setVisibility(View.GONE);
        }

    }

    // toggle between month view and week view
    private void toggleView() {
        android.util.Log.i(null, "Pressed Toggle");
        if (isCalendarView) {
            android.util.Log.i(null, "Changing to WeekView");
            toggleViewButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.outline_calendar_month_24,0,0,0);
            calendarView.setVisibility(View.GONE);
            logs1.setVisibility(View.GONE);
            logs2.setVisibility(View.VISIBLE);
            weekCalendarView.setVisibility(View.VISIBLE);
            schedule.setVisibility(View.GONE);
            schedule1.setVisibility(View.VISIBLE);
            viewType = "week";
            android.util.Log.i(null, "selected date: " + selectedDate);
            loadEventsForDate(selectedDate,viewType);
            setupWeekView();
        } else {
            android.util.Log.i(null, "Changing to CalendarView");
            toggleViewButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.outline_calendar_view_day_24,0,0,0);
            calendarView.setVisibility(View.VISIBLE);
            logs1.setVisibility(View.VISIBLE);
            logs2.setVisibility(View.GONE);
            weekCalendarView.setVisibility(View.GONE);
            schedule.setVisibility(View.VISIBLE);
            schedule1.setVisibility(View.GONE);
            viewType = "month";
            loadEventsForDate(selectedDate,viewType);
            setupCalendarView();
        }
        isCalendarView = !isCalendarView;
    }

    // customization for date selected
    public void toggleSelect(DayViewContainer container, int currentYear, int currentMonth) {
        int day = Integer.parseInt(container.dayText.getText().toString());
        LocalDate clickedDate = LocalDate.of(currentYear, currentMonth, day);
        if (selectedDateContainer != null) {
            android.util.Log.i("toggle Select", "In select if");
            // reset
            selectedDateContainer.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.transparent)));
            selectedDateContainer.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.black)));
            selectedDateContainer.select.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.transparent)));
            if (getAllScheduledDates(tank.getFeedSchedule()).contains(selectedDate) && selectedDate.isAfter(LocalDate.now())){
                android.util.Log.i("toggle Select", "in if date requirement for indicator");
                selectedDateContainer.scheduleIndicator.setVisibility(View.VISIBLE);
            }
        }
        // Select the new date
        int backgroundColor = ContextCompat.getColor(FeedingNew.this, R.color.mid_green);
        container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.transparent)));
        container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(FeedingNew.this, R.color.white)));
        container.select.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        selectedDate = clickedDate;
        selectedDateContainer = container;
        container.scheduleIndicator.setVisibility(View.GONE);
    }

    // Add Schedule
    public void addSchedule(int index){
        scheduleList = tank.getFeedSchedule();

    }

    // Create Schedule
    public void createSchedule(int size){
        scheduleList = tank.getFeedSchedule();
        if (size <=3 ){

        }
        else Toast.makeText(FeedingNew.this, "Too many schedules created: " + size, Toast.LENGTH_SHORT).show();

    }

    // Refresh Data
    public void refreshData(){
        reference.child(user.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // retrieve user data from Firebase
                    User updatedUser = snapshot.getValue(User.class);
                    if (updatedUser != null && updatedUser.getTanks() != null) {
                        user.setTanks(updatedUser.getTanks());
                        for (Tank tank : user.getTanks()) {
                            if (tank.getTankID() == targetTankID) {
                                android.util.Log.i("refresh data", " tank.getTankID(): " + tank.getTankID());
                                if (tank.getFeedSchedule()!=null){
                                    scheduleList = tank.getFeedSchedule();
                                    if (scheduleList != null){
                                        android.util.Log.i("refresh data", " scheduleList size: " + scheduleList.size());
                                    }
                                }
                                else {
                                    scheduleList = new ArrayList<>();
                                }
                                if (tank.getFeedingLog()!=null){
                                    feedingLog = tank.getFeedingLog();
                                }
                                else {
                                    feedingLog = new ArrayList<>();
                                }
                            }
                        }
                    } else {
                        scheduleList = new ArrayList<>();
                        feedingLog = new ArrayList<>();
                    }
                } else {
                    scheduleList = new ArrayList<>();
                    feedingLog = new ArrayList<>();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                android.util.Log.e("Firebase", "Failed to read user data.", error.toException());
            }
        });
        greenFood = new ArrayList<>();
        brownFood = new ArrayList<>();
        for (Tank tank : user.getTanks()) {
            if (tank.getFeedingLog() != null) {
                for (sg.edu.np.mad.greencycle.Classes.Log log : tank.getFeedingLog()) {
                    if (log.getGreens() != null) {
                        for (Food food : log.getGreens()) {
                            greenFood.add(food.getName());
                        }
                    }
                    if (log.getBrowns() != null) {
                        for (Food food : log.getBrowns()) {
                            brownFood.add(food.getName());
                        }
                    }
                }
            }
        }
        Set<String> greenSet = new HashSet<>(greenFood);
        greenFood.clear();
        greenFood.addAll(greenSet);

        Set<String> brownSet = new HashSet<>(brownFood);
        brownFood.clear();
        brownFood.addAll(brownSet);

        tank = user.getTanks().get(targetTankID);
    }

    // generate scheduled dates
    public static List<LocalDate> generateScheduledDates(FeedSchedule feedSchedule) {
        List<LocalDate> scheduledDates = new ArrayList<>();
        LocalDate selectedDate = LocalDate.parse(feedSchedule.getStartDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        LocalDate nextMonth = selectedDate.plusMonths(3);
        String repeatType = feedSchedule.getRepeatType();
        HashMap<String, ArrayList<String>> repeatDetails = feedSchedule.getRepeatDetails();

        if (repeatType.equals("Don't repeat")) {
            scheduledDates.add(selectedDate);
        }
        else if (repeatType.equals("Everyday")) {
            for (LocalDate date = selectedDate; !date.isAfter(nextMonth); date = date.plusDays(1)) {
                scheduledDates.add(date);
            }
        }
        else if (repeatType.contains("week")) {
            String keyWithWeek = null;
            for (String key : repeatDetails.keySet()) {
                if (key.toLowerCase().contains("week")) {
                    keyWithWeek = key;
                    break;
                }
            }
            ArrayList<String> daysOfWeek = repeatDetails.get(keyWithWeek);
            for (LocalDate date = selectedDate; !date.isAfter(nextMonth); date = date.plusWeeks(1)) {
                for (String day : daysOfWeek) {
                    DayOfWeek dayOfWeek = DayOfWeek.valueOf(day.toUpperCase());
                    LocalDate weekDate = date.with(dayOfWeek);
                    if (!weekDate.isBefore(selectedDate) && !weekDate.isAfter(nextMonth)) {
                        scheduledDates.add(weekDate);
                    }
                }
            }
        }
        else if (repeatType.contains("month")) {
            String keyWithMonth = null;
            for (String key : repeatDetails.keySet()) {
                if (key.toLowerCase().contains("month")) {
                    keyWithMonth = key;
                    break;
                }
            }
            ArrayList<String> daysOfMonth = repeatDetails.get(keyWithMonth);
            for (LocalDate date = selectedDate; !date.isAfter(nextMonth); date = date.plusMonths(1)) {
                scheduledDates.add(date);
                for (String day : daysOfMonth) {
                    DayOfWeek dayOfWeek = DayOfWeek.valueOf(day.toUpperCase());
                    LocalDate weekDate = date.with(dayOfWeek);
                    if (!weekDate.isBefore(selectedDate) && !weekDate.isAfter(nextMonth)) {
                        scheduledDates.add(weekDate);
                    }
                }
            }
        }

        return scheduledDates;
    }

    public static HashMap<FeedSchedule, List<LocalDate>> generateScheduledDates(List<FeedSchedule> feedSchedules) {
        HashMap<FeedSchedule, List<LocalDate>> scheduleMap = new HashMap<>();
        for (FeedSchedule feedSchedule : feedSchedules) {
            List<LocalDate> scheduledDates = generateScheduledDates(feedSchedule);
            scheduleMap.put(feedSchedule, scheduledDates);
        }

        android.util.Log.i("generateScheduledDates", "scheduleMap: " + String.valueOf(scheduleMap.isEmpty()));
        return scheduleMap;
    }

    public static List<LocalDate> getAllScheduledDates(List<FeedSchedule> feedSchedules) {
        List<LocalDate> allScheduledDates = new ArrayList<>();
        for (FeedSchedule feedSchedule : feedSchedules) {
            allScheduledDates.addAll(generateScheduledDates(feedSchedule));
        }
        return allScheduledDates;
    }
    // Classes
    class DayViewContainer extends ViewContainer {
        TextView dayText, select, logIndicator, scheduleIndicator;

        DayViewContainer(View view) {
            super(view);
            dayText = view.findViewById(R.id.dayText);
            select = view.findViewById(R.id.selectedDate);
            logIndicator = view.findViewById(R.id.logIndicator);
            scheduleIndicator = view.findViewById(R.id.scheduleIndicator);
        }
    }
}

