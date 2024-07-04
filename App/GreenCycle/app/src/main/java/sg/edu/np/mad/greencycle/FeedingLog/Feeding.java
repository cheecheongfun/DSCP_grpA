package sg.edu.np.mad.greencycle.FeedingLog;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.kizitonwose.calendar.view.ViewContainer;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.WeekCalendarView;
import com.kizitonwose.calendar.view.WeekDayBinder;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
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
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.TankSelection.TankSelection;

public class Feeding extends AppCompatActivity {

    CalendarView calendarView;
    WeekCalendarView weekCalendarView;
    RelativeLayout logs1, logs2, details, details1, fixedTitleContainer, titlesContainer, scheduleDetails1, scheduleDetails2;
    TextView toggleViewButton, backBtn, waterAmt, notesFixed, noLogs, waterAmt1, notesFixed1, noLogs1, monthHead, water1, water2;
    TextView noDetails1, noDetails2, greenText1, greenText2, brownText1, brownText2, scheduleTitle1, scheduleTitle2, noteText1, noteText2;
    boolean isCalendarView = true;
    boolean isSelected = true;
    LocalDate selectedDate;
    LogAdapter gAdapter, bAdapter;
    ArrayList<DayOfWeek> daysOfWeek;
    ArrayList<Food> greens, browns;
    ArrayList<FeedSchedule> scheduleList;
    RecyclerView gRecycler, bRecycler, gRecycler1, bRecycler1, greenRecycler1, greenRecycler2, brownRecycler1, brownRecycler2;
    ImageButton gAdd, bAdd,gAdd1, bAdd1;
    FirebaseDatabase database;
    DatabaseReference reference;
    User user;
    Tank tank;
    EditText editWater, editNotes, editWater1, editNotes1;
    Button confirm, schedule, schedule1, delete, add, logDelete, logSave;
    String today, viewType;
    LinearLayout dayHead, bottomBtnSchedule, bottomBtnLog;
    DayViewContainer selectedDateContainer = null;
    int selectedIndex, targetTankID;
    ArrayList<Log> feedingLog;
    ArrayList<String> greenFood, brownFood;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.feeding_log);
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
        if (tank.getFeedSchedule()!=null){
            scheduleList = tank.getFeedSchedule();
        }
        else scheduleList = new ArrayList<>();
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
        bottomBtnSchedule = findViewById(R.id.bottomBtnSchedule);
        delete = bottomBtnSchedule.findViewById(R.id.scheduleDelete);
        add = bottomBtnSchedule.findViewById(R.id.scheduleAdd);
        bottomBtnLog = findViewById(R.id.bottomBtnLog);
        logDelete=bottomBtnLog.findViewById(R.id.logDelete);
        logSave = bottomBtnLog.findViewById(R.id.logSave);

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
        noteText1 = logs1.findViewById(R.id.notesText);

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
        noteText2 = logs2.findViewById(R.id.notesText);

        scheduleDetails1 =  findViewById(R.id.scheduleDetail1);
        scheduleDetails2 =  findViewById(R.id.scheduleDetail2);

        // schedule details 1 (month)
        greenRecycler1 = scheduleDetails1.findViewById(R.id.greenRecycler);
        brownRecycler1 = scheduleDetails1.findViewById(R.id.brownRecycler);
        water1 = scheduleDetails1.findViewById(R.id.water);
        noDetails1 = scheduleDetails1.findViewById(R.id.noDetailsText);
        greenText1 = scheduleDetails1.findViewById(R.id.greens);
        brownText1 = scheduleDetails1.findViewById(R.id.browns);
        scheduleTitle1 = scheduleDetails1.findViewById(R.id.scheduleName);

        // schedule details 2 (week)
        greenRecycler2 = scheduleDetails2.findViewById(R.id.greenRecycler);
        brownRecycler2 = scheduleDetails2.findViewById(R.id.brownRecycler);
        water2 = scheduleDetails2.findViewById(R.id.water);
        noDetails2 = scheduleDetails2.findViewById(R.id.noDetailsText);
        greenText2 = scheduleDetails2.findViewById(R.id.greens);
        brownText2 = scheduleDetails2.findViewById(R.id.browns);
        scheduleTitle2 = scheduleDetails2.findViewById(R.id.scheduleName);

        selectedIndex =0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        today = formatter.format(new Date());
        selectedDate = LocalDate.now();
        viewType = "month";

        greens = new ArrayList<>();
        browns = new ArrayList<>();

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

        for (FeedSchedule schedule : scheduleList){
            regenerateDates(schedule);
        }
        refreshData();
        loadForToday(viewType);
        setupCalendarView();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Adding log / Editing log
                Log selectedLog = new Log();
                confirm.setVisibility(View.GONE);
                bottomBtnLog.setVisibility(View.VISIBLE);
                if (confirm.getText().equals("Edit log")){
                    if (isCalendarView){
                        toggleView();
                    }
                    for (Log log : tank.getFeedingLog()){
                        if (selectedDate.equals(LocalDate.parse(log.getLogDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy")))){
                            selectedLog = log;
                        }
                    }


                }
            }
        });
        logDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // deleting log
                confirm.setText("Add log");
                confirm.setVisibility(View.VISIBLE);
                bottomBtnLog.setVisibility(View.GONE);
                if (isCalendarView){
                    setupCalendarView();
                } else setupWeekView();
            }
        });
        logSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // saving log
                confirm.setText("Edit log");
                confirm.setVisibility(View.VISIBLE);
                bottomBtnLog.setVisibility(View.GONE);
                if (isCalendarView){
                    setupCalendarView();
                } else setupWeekView();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                FeedSchedule feedSched = findFeedScheduleByDate(scheduleList, selectedDate);
                int index = scheduleList.indexOf(feedSched);
                ArrayList<String> dates = new ArrayList<>(feedSched.getDates());
                for (FeedSchedule sched : scheduleList){
                    if (sched.equals(findFeedScheduleByDate(scheduleList, selectedDate))){
                        dates.remove(formatter.format(selectedDate));
                    }
                }

                feedSched.setDates(dates);
                scheduleList.set(index, feedSched);

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                for (int i = 0; i < user.getTanks().size(); i++) {
                    Tank tank = user.getTanks().get(i);
                    if (tank.getTankID() == targetTankID) {
                        tank.setFeedSchedule(scheduleList);
                        user.getTanks().set(i, tank);
                        user.setTanks(user.getTanks());
                        break;
                    }
                }

                android.util.Log.i(null, "after set tank");
                reference.child(user.getUsername()).setValue(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                android.util.Log.i("FirebaseUpdate", "Feed Schedule: "+ user.getTanks().get(0).getFeedSchedule().size());
                                // Dismiss the dialog or close the activity
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                android.util.Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                            }
                        });
                refreshData();
                if (isCalendarView){
                    setupCalendarView();
                } else setupWeekView();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                // removing schedule date
                FeedSchedule feedSched = findFeedScheduleByDate(scheduleList, selectedDate);
                int index = scheduleList.indexOf(feedSched);
                ArrayList<String> dates = new ArrayList<>(feedSched.getDates());
                for (FeedSchedule sched : scheduleList){
                    if (sched.equals(findFeedScheduleByDate(scheduleList, selectedDate))){
                        dates.remove(formatter.format(selectedDate));
                    }
                }
                feedSched.setDates(dates);
                scheduleList.set(index, feedSched);

                // creating log
                Log newLog = new Log();
                if (tank.getFeedingLog() != null){
                    newLog = new Log(0, formatter.format(selectedDate), feedSched.getGreenFood(), feedSched.getBrownFood(), null, feedSched.getWaterAmt() );
                }
                newLog = new Log(tank.getFeedingLog().size(), formatter.format(selectedDate), feedSched.getGreenFood(), feedSched.getBrownFood(), null, feedSched.getWaterAmt() );

                ArrayList<Log> logList;
                if (tank.getFeedingLog() != null){
                    logList = tank.getFeedingLog();
                }
                else logList = new ArrayList<>();
                logList.add(newLog);
                tank.setFeedingLog(logList);

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                for (int i = 0; i < user.getTanks().size(); i++) {
                    Tank tank = user.getTanks().get(i);
                    if (tank.getTankID() == targetTankID) {
                        tank.setFeedSchedule(scheduleList);
                        tank.setFeedingLog(logList);
                        user.getTanks().set(i, tank);
                        user.setTanks(user.getTanks());
                        break;
                    }
                }

                android.util.Log.i(null, "after set tank");
                reference.child(user.getUsername()).setValue(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                android.util.Log.i("FirebaseUpdate", "Feed Schedule: "+ user.getTanks().get(0).getFeedSchedule().size());
                                // Dismiss the dialog or close the activity
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                android.util.Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                            }
                        });
                refreshData();
                if (isCalendarView){
                    setupCalendarView();
                } else setupWeekView();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Feeding.this, TankSelection.class);
                intent.putExtra("user", user);
                intent.putExtra("where", "Feeding");
                startActivity(intent);
                finish();
            }
        });

        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.util.Log.i("schedule", "schedule click");
                refreshData();
                if (!scheduleList.isEmpty()){
                    android.util.Log.i("schedule", "scheduleList not empty");
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.schedule_dialog, null);

                    TextView dialogTitle = dialogView.findViewById(R.id.title);
                    ListView scheduleListView = dialogView.findViewById(R.id.scheduleList);

                    dialogTitle.setText("Schedules");
                    AlertDialog.Builder builder = new AlertDialog.Builder(Feeding.this);

                    if (getAllScheduledDates(scheduleList).contains(selectedDate)){

                        FeedSchedule existing = findFeedScheduleByDate(scheduleList, selectedDate);
                        ArrayList<FeedSchedule> temporary = new ArrayList<>(scheduleList);
                        temporary.remove(existing);
                        FeedScheduleAdapter adapter = new FeedScheduleAdapter(Feeding.this, temporary, user, tank, scheduleList);
                        scheduleListView.setAdapter(adapter);
                        builder.setView(dialogView)
                                .setPositiveButton("Replace", (dialog, which) -> {
                                    if (temporary.isEmpty()){
                                        refreshData();
                                        Toast.makeText(Feeding.this, "No schedule selected", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                    else{
                                        replaceSchedule(adapter.getNewFeedSchedule());
                                    }
                                    dialog.dismiss();
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .setNeutralButton("New Schedule", (dialog, which) -> {
                                    if (getAllScheduledDates(scheduleList).contains(selectedDate)){
                                        Toast.makeText(Feeding.this, "One schedule a day", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        createSchedule();
                                    }
                                    dialog.dismiss();
                                });
                    }
                    else{
                        android.util.Log.i("schedule", "no schedule for today");
                        FeedScheduleAdapter adapter = new FeedScheduleAdapter(Feeding.this, scheduleList, user, tank, scheduleList);
                        scheduleListView.setAdapter(adapter);

                        builder.setView(dialogView)
                                .setPositiveButton("Set", (dialog, which) -> {
                                    refreshData();
                                    android.util.Log.i("positive schedule set", "scheduleList size: " + scheduleList.size());
                                    if (getAllScheduledDates(scheduleList).contains(selectedDate)){
                                        Toast.makeText(Feeding.this, "One schedule a day", Toast.LENGTH_SHORT).show();
                                    }
                                    else if(scheduleList.isEmpty()){

                                    }
                                    else{
                                        addSchedule(adapter.getSelectedIndex(), adapter.getSelectedNotificationType());
                                    }
                                    dialog.dismiss();
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .setNeutralButton("New Schedule", (dialog, which) -> {
                                    if (getAllScheduledDates(scheduleList).contains(selectedDate)){
                                        Toast.makeText(Feeding.this, "One schedule a day", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        createSchedule();
                                    }
                                    dialog.dismiss();
                                });
                    }

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            refreshData();
                            if (isCalendarView) {
                                setupCalendarView();
                            } else {
                                setupWeekView();
                            }
                        }
                    });
                    android.util.Log.i("schedule", "after dialog show, scheduleList: " + scheduleList.size());

                }
                else{
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    String startDate = selectedDate.format(formatter);
                    CreateSchedule dialogFragment = CreateSchedule.newInstance(user, tank, greenFood, brownFood, startDate);
                    dialogFragment.show(getSupportFragmentManager(), "CreateSchedule");

                    getSupportFragmentManager().setFragmentResultListener("requestKey", Feeding.this, new FragmentResultListener() {
                        @Override
                        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                            // Handle the result here
                            refreshData();
                            android.util.Log.i("schedule", "here");
                            if (isCalendarView) {
                                setupCalendarView();
                            } else {
                                setupWeekView();
                            }
                        }
                    });
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
                for (LocalDate date : getAllScheduledDates(scheduleList)){
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
                        else if (selectedDate.isBefore(LocalDate.now())){
                            loadEventsForDate(day.getDate(), viewType);
                        }
                        else loadForToday(viewType);
                    });
                } else {
                    int textColor = ContextCompat.getColor(Feeding.this, R.color.light_grey);
                    container.dayText.setTextColor(textColor);
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
                if (selectedDate!=null && day.getDate().isEqual(selectedDate)){
                    int backgroundColor = ContextCompat.getColor(Feeding.this, R.color.mid_green);
                    container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.transparent)));
                    container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.white)));
                    container.select.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
                    selectedDate = day.getDate();
                    selectedDateContainer = container;
                    if (selectedDate.isAfter(LocalDate.now())){
                        loadScheduleForDate(day.getDate(),viewType);
                    }
                    else if (selectedDate.isBefore(LocalDate.now())){
                        loadEventsForDate(day.getDate(), viewType);
                    }
                    else loadForToday(viewType);
                }
                else if (selectedDate == null && day.getDate().equals(LocalDate.now())){
                    int backgroundColor = ContextCompat.getColor(Feeding.this, R.color.mid_green);
                    container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.transparent)));
                    container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.white)));
                    container.select.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
                    selectedDate = day.getDate();
                    selectedDateContainer = container;
                    if (selectedDate.isAfter(LocalDate.now())){
                        loadScheduleForDate(day.getDate(),viewType);
                    }
                    else if (selectedDate.isBefore(LocalDate.now())){
                        loadEventsForDate(day.getDate(), viewType);
                    }
                    else loadForToday(viewType);
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
                for (LocalDate date : getAllScheduledDates(scheduleList)){
                    if (date.equals(LocalDate.of(weekDay.getDate().getYear(), weekDay.getDate().getMonth(), weekDay.getDate().getDayOfMonth()))){
                        container.scheduleIndicator.setVisibility(View.VISIBLE);
                    }
                }
                container.dayText.setText(String.valueOf(weekDay.getDate().getDayOfMonth()));
                container.dayText.setOnClickListener(v -> {
                    toggleSelect(container, weekDay.getDate().getYear(), weekDay.getDate().getMonthValue());
                    if (selectedDate.isAfter(LocalDate.now())){
                        loadScheduleForDate(weekDay.getDate(),viewType);
                    }
                    else if (selectedDate.isBefore(LocalDate.now())){
                        loadEventsForDate(weekDay.getDate(), viewType);
                    }
                    else loadForToday(viewType);
                });
                if (selectedDate!=null && weekDay.getDate().isEqual(selectedDate)){
                    int backgroundColor = ContextCompat.getColor(Feeding.this, R.color.mid_green);
                    container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.transparent)));
                    container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.white)));
                    container.select.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
                    selectedDate = weekDay.getDate();
                    selectedDateContainer = container;
                    if (selectedDate.isAfter(LocalDate.now())){
                        loadScheduleForDate(weekDay.getDate(),viewType);
                    }
                    else if (selectedDate.isBefore(LocalDate.now())){
                        loadEventsForDate(weekDay.getDate(), viewType);
                    }
                    else loadForToday(viewType);
                }
                else if (selectedDate == null && weekDay.getDate().equals(LocalDate.now())){
                    int backgroundColor = ContextCompat.getColor(Feeding.this, R.color.mid_green);
                    container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.transparent)));
                    container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.white)));
                    container.select.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
                    selectedDate = weekDay.getDate();
                    selectedDateContainer = container;
                    if (selectedDate.isAfter(LocalDate.now())){
                        loadScheduleForDate(weekDay.getDate(),viewType);
                    }
                    else if (selectedDate.isBefore(LocalDate.now())){
                        loadEventsForDate(weekDay.getDate(), viewType);
                    }
                    else loadForToday(viewType);
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
        scheduleDetails1.setVisibility(View.GONE);
        scheduleDetails2.setVisibility(View.GONE);
        bottomBtnSchedule.setVisibility(View.GONE);
        confirm.setVisibility(View.VISIBLE);
        schedule.setVisibility(View.GONE);
        schedule1.setVisibility(View.GONE);
        schedule.setText("Schedule");
        schedule1.setText("Schedule");
        scheduleDetails1.setVisibility(View.GONE);
        scheduleDetails2.setVisibility(View.GONE);
        logs1.setVisibility(View.GONE);
        logs2.setVisibility(View.GONE);
        noLogs.setVisibility(View.GONE);
        noLogs1.setVisibility(View.GONE);
        ArrayList<LocalDate> dateList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
        for (Log log : tank.getFeedingLog()) {
            dateList.add(LocalDate.parse(log.getLogDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        String notes = " ";
        int water = 0;
        greens.clear();
        browns.clear();
        if (dateList.contains(date)) {
            for (Log log : tank.getFeedingLog()) {
                if (log.getLogDate().equals(formatter.format(date))) {
                    greens.addAll(log.getGreens());
                    browns.addAll(log.getBrowns());
                    notes = log.getNotes();
                    water = log.getWaterAmt();
                    android.util.Log.i("loadEventsForDate", "greens: " + log.getGreens().size());
                    break;
                }
            }

            confirm.setText("Edit log");
            //month view
            if (view.equals("month")){
                logs1.setVisibility(View.VISIBLE);
                logs2.setVisibility(View.GONE);
                android.util.Log.i(null, "month");
                if (notes == null || notes.isEmpty()){
                    notesFixed.setVisibility(View.GONE);
                    noteText1.setVisibility(View.GONE);
                }
                else{
                    notesFixed.setVisibility(View.VISIBLE);
                    noteText1.setVisibility(View.VISIBLE);
                    notesFixed.setText(notes);
                }
                water1.setText("Water: " + water + " ml");
                if (greens == null && browns == null){
                    android.util.Log.i(null, "In Load If");
                    noLogs.setVisibility(View.VISIBLE);
                    confirm.setText("Add Log");
                }
                else if (greens == null){
                    android.util.Log.i(null, "In Load Else If");
                    gRecycler.setVisibility(View.GONE);
                    bAdapter = new LogAdapter(Feeding.this, browns, "brown", null);
                    bRecycler.setLayoutManager(new LinearLayoutManager(this));
                    bRecycler.setAdapter(bAdapter);
                }
                else if (browns == null){
                    bRecycler.setVisibility(View.GONE);
                    gAdapter = new LogAdapter(Feeding.this, greens, "green", null);
                    gRecycler.setLayoutManager(new LinearLayoutManager(this));
                    gRecycler.setAdapter(gAdapter);
                }
                else {
                    android.util.Log.i(null, "In Load Else");
                    gAdapter = new LogAdapter(Feeding.this, greens, "green", null);
                    gRecycler.setLayoutManager(new LinearLayoutManager(this));
                    gRecycler.setAdapter(gAdapter);

                    // brown recycler view
                    bAdapter = new LogAdapter(Feeding.this, browns, "brown", null);
                    bRecycler.setLayoutManager(new LinearLayoutManager(this));
                    bRecycler.setAdapter(bAdapter);
                }
            }
            // week view
            else {
                logs1.setVisibility(View.GONE);
                logs2.setVisibility(View.VISIBLE);
                if (notes == null || notes.isEmpty()){
                    notesFixed1.setVisibility(View.GONE);
                    noteText2.setVisibility(View.GONE);
                }
                else{
                    notesFixed1.setVisibility(View.VISIBLE);
                    noteText2.setVisibility(View.VISIBLE);
                    notesFixed1.setText(notes);
                }
                water2.setText("Water: " + water + " ml");
                if (greens == null && browns == null) {
                    android.util.Log.i(null, "In Load If");
                    noLogs1.setVisibility(View.VISIBLE);
                    confirm.setText("Add Log");
                } else if (greens == null) {
                    android.util.Log.i(null, "In Load Else If");
                    gRecycler1.setVisibility(View.GONE);
                    bAdapter = new LogAdapter(Feeding.this, browns, "brown", null);
                    bRecycler1.setLayoutManager(new LinearLayoutManager(this));
                    bRecycler1.setAdapter(bAdapter);
                } else if (browns == null) {
                    bRecycler1.setVisibility(View.GONE);
                    gAdapter = new LogAdapter(Feeding.this, greens, "green", null);
                    gRecycler1.setLayoutManager(new LinearLayoutManager(this));
                    gRecycler1.setAdapter(gAdapter);
                } else {
                    android.util.Log.i(null, "In Load Else");
                    gAdapter = new LogAdapter(Feeding.this, greens, "green", null);
                    gRecycler1.setLayoutManager(new LinearLayoutManager(this));
                    gRecycler1.setAdapter(gAdapter);

                    // brown recycler view
                    bAdapter = new LogAdapter(Feeding.this, browns, "brown", null);
                    bRecycler1.setLayoutManager(new LinearLayoutManager(this));
                    bRecycler1.setAdapter(bAdapter);
                }
            }
        } else {
            confirm.setText("Add log");
            android.util.Log.i("loadEvents", "no log");
            if (view.equals("month")) {
                android.util.Log.i("loadEvents", "in month: ");
                logs1.setVisibility(View.VISIBLE);
                noLogs.setVisibility(View.VISIBLE);
                noLogs1.setVisibility(View.GONE);
                if (date.equals(LocalDate.now())){
                    schedule.setVisibility(View.VISIBLE);
                }
            } else {
                android.util.Log.i("loadEvents", "in week: ");
                logs2.setVisibility(View.VISIBLE);
                noLogs1.setVisibility(View.VISIBLE);
                noLogs.setVisibility(View.GONE);
                if (date.equals(LocalDate.now())){
                    schedule1.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void loadScheduleForDate(LocalDate date, String view) {
        confirm.setVisibility(View.GONE);
        logs1.setVisibility(View.GONE);
        logs2.setVisibility(View.GONE);
        scheduleDetails1.setVisibility(View.GONE);
        scheduleDetails2.setVisibility(View.GONE);
        bottomBtnLog.setVisibility(View.GONE);
        if (getAllScheduledDates(scheduleList).contains(date)){
            FeedSchedule sched = findFeedScheduleByDate(scheduleList, date);
            android.util.Log.i("load Schedule for date", "In if: " );
            if (date.equals(LocalDate.now())){
                bottomBtnSchedule.setVisibility(View.VISIBLE);
                android.util.Log.i("load Schedule for date", "contains today: " + LocalDate.now() );
            }
            else bottomBtnSchedule.setVisibility(View.GONE);
            // month view
            if (view.equals("month")){
                android.util.Log.i("load Schedule for date", "month view" );
                schedule.setVisibility(View.VISIBLE);
                schedule.setText("Replace schedule");
                schedule1.setVisibility(View.GONE);
                scheduleDetails2.setVisibility(View.GONE);

                scheduleTitle1.setText(sched.getScheduleName());
                scheduleTitle2.setText(sched.getScheduleName());

                noDetails1.setText("No details");
                water1.setText("Water: " + sched.getWaterAmt() + " " + "ml");
                noDetails1.setVisibility(View.GONE);
                scheduleDetails1.setVisibility(View.VISIBLE);

                if (sched.getGreenFood() == null && sched.getBrownFood() == null && sched.getWaterAmt() == 0){
                    android.util.Log.i("load Schedule for date", "null" );
                    noDetails1.setVisibility(View.VISIBLE);
                }
                else if (sched.getGreenFood() == null && sched.getBrownFood() == null){
                    android.util.Log.i("load Schedule for date", "food null" );
                    greenText1.setVisibility(View.GONE);
                    greenRecycler1.setVisibility(View.GONE);
                    brownText1.setVisibility(View.GONE);
                    brownRecycler1.setVisibility(View.GONE);
                    noDetails1.setVisibility(View.GONE);
                    water1.setVisibility(View.VISIBLE);
                }
                else if (sched.getGreenFood() == null){
                    android.util.Log.i("load Schedule for date", "green null" );
                    greenText1.setVisibility(View.GONE);
                    greenRecycler1.setVisibility(View.GONE);
                    brownText1.setVisibility(View.VISIBLE);
                    brownRecycler1.setVisibility(View.VISIBLE);
                    bAdapter = new LogAdapter(Feeding.this, sched.getBrownFood(), "brown", null);
                    brownRecycler1.setLayoutManager(new LinearLayoutManager(this));
                    brownRecycler1.setAdapter(bAdapter);
                    water1.setVisibility(View.VISIBLE);
                    noDetails1.setVisibility(View.GONE);
                }
                else if (sched.getBrownFood() == null){
                    android.util.Log.i("load Schedule for date", "brown null" );
                    greenText1.setVisibility(View.VISIBLE);
                    greenRecycler1.setVisibility(View.VISIBLE);
                    brownText1.setVisibility(View.GONE);
                    brownRecycler1.setVisibility(View.GONE);
                    gAdapter = new LogAdapter(Feeding.this, sched.getGreenFood(), "green", null);
                    greenRecycler1.setLayoutManager(new LinearLayoutManager(this));
                    greenRecycler1.setAdapter(gAdapter);
                    noDetails1.setVisibility(View.GONE);
                    water1.setVisibility(View.VISIBLE);
                }
                else {
                    android.util.Log.i("load Schedule for date", "all details");
                    noDetails1.setVisibility(View.GONE);
                    greenText1.setVisibility(View.VISIBLE);
                    greenRecycler1.setVisibility(View.VISIBLE);
                    brownText1.setVisibility(View.VISIBLE);
                    brownRecycler1.setVisibility(View.VISIBLE);

                    gAdapter = new LogAdapter(Feeding.this, sched.getGreenFood(), "green", null);
                    greenRecycler1.setLayoutManager(new LinearLayoutManager(this));
                    greenRecycler1.setAdapter(gAdapter);

                    // brown recycler view
                    bAdapter = new LogAdapter(Feeding.this, sched.getBrownFood(), "brown", null);
                    brownRecycler1.setLayoutManager(new LinearLayoutManager(this));
                    brownRecycler1.setAdapter(bAdapter);

                    water1.setVisibility(View.VISIBLE);
                }
            }
            // week view
            else {
                android.util.Log.i("load Schedule for date", "week view" );
                schedule1.setVisibility(View.VISIBLE);
                schedule1.setText("Replace schedule");
                schedule.setVisibility(View.GONE);
                scheduleDetails1.setVisibility(View.GONE);

                scheduleTitle1.setText(sched.getScheduleName());
                scheduleTitle2.setText(sched.getScheduleName());
                noDetails2.setText("No details");
                water2.setText("Water: " + sched.getWaterAmt() + " " + "ml");
                noDetails2.setVisibility(View.GONE);
                scheduleDetails2.setVisibility(View.VISIBLE);

                if (sched.getGreenFood() == null && sched.getBrownFood() == null && sched.getWaterAmt() == 0){
                    android.util.Log.i("load Schedule for date", "null" );
                    noDetails2.setVisibility(View.VISIBLE);
                }
                else if (sched.getGreenFood() == null && sched.getBrownFood() == null){
                    android.util.Log.i("load Schedule for date", "food null" );
                    greenText2.setVisibility(View.GONE);
                    greenRecycler2.setVisibility(View.GONE);
                    brownText2.setVisibility(View.GONE);
                    brownRecycler2.setVisibility(View.GONE);
                    noDetails2.setVisibility(View.GONE);
                    water2.setVisibility(View.VISIBLE);
                    noDetails2.setVisibility(View.GONE);
                }
                else if (sched.getGreenFood() == null){
                    android.util.Log.i("load Schedule for date", "green null" );
                    greenText2.setVisibility(View.GONE);
                    greenRecycler2.setVisibility(View.GONE);
                    brownText2.setVisibility(View.VISIBLE);
                    brownRecycler2.setVisibility(View.VISIBLE);
                    bAdapter = new LogAdapter(Feeding.this, sched.getBrownFood(), "brown", null);
                    brownRecycler2.setLayoutManager(new LinearLayoutManager(this));
                    brownRecycler2.setAdapter(bAdapter);
                    water2.setVisibility(View.VISIBLE);
                    noDetails2.setVisibility(View.GONE);
                }
                else if (sched.getBrownFood() == null){
                    android.util.Log.i("load Schedule for date", "brown null" );
                    greenText2.setVisibility(View.VISIBLE);
                    greenRecycler2.setVisibility(View.VISIBLE);
                    brownText2.setVisibility(View.GONE);
                    brownRecycler2.setVisibility(View.GONE);
                    gAdapter = new LogAdapter(Feeding.this, sched.getGreenFood(), "green", null);
                    greenRecycler2.setLayoutManager(new LinearLayoutManager(this));
                    greenRecycler2.setAdapter(gAdapter);
                    water2.setVisibility(View.VISIBLE);
                    noDetails2.setVisibility(View.GONE);
                }
                else {
                    android.util.Log.i("load Schedule for date", "all details");
                    greenText2.setVisibility(View.VISIBLE);
                    greenRecycler2.setVisibility(View.VISIBLE);
                    brownText2.setVisibility(View.VISIBLE);
                    brownRecycler2.setVisibility(View.VISIBLE);

                    gAdapter = new LogAdapter(Feeding.this, sched.getGreenFood(), "green", null);
                    greenRecycler2.setLayoutManager(new LinearLayoutManager(this));
                    greenRecycler2.setAdapter(gAdapter);

                    // brown recycler view
                    bAdapter = new LogAdapter(Feeding.this, sched.getBrownFood(), "brown", null);
                    brownRecycler2.setLayoutManager(new LinearLayoutManager(this));
                    brownRecycler2.setAdapter(bAdapter);

                    water2.setVisibility(View.VISIBLE);
                    noDetails2.setVisibility(View.GONE);
                }
            }
        }
        else {
            android.util.Log.i("load Schedule for date", "In else: " );
            bottomBtnSchedule.setVisibility(View.GONE);
            if (view.equals("month")){
                schedule1.setVisibility(View.GONE);
                schedule.setVisibility(View.VISIBLE);
                schedule.setText("Schedule");
                scheduleDetails1.setVisibility(View.VISIBLE);
                scheduleTitle1.setText("No Schedule");
                noDetails1.setVisibility(View.VISIBLE);
                noDetails1.setText(" ");
            }
            else{
                schedule.setVisibility(View.GONE);
                scheduleDetails2.setVisibility(View.VISIBLE);
                scheduleTitle2.setText("No Schedule");
                noDetails2.setVisibility(View.VISIBLE);
                noDetails2.setText(" ");
                schedule1.setVisibility(View.VISIBLE);
                schedule1.setText("Schedule");
            }
        }
    }

    private void loadForToday(String view) {
        LocalDate today = LocalDate.now();
        android.util.Log.i("LoadForToday", "loading logs");
        if (!getAllScheduledDates(scheduleList).contains(today)){
            loadEventsForDate(today, view);
        }
        else{
            android.util.Log.i("LoadForToday", "loading schedule");
            loadScheduleForDate(today, view);
            // add to log
            // load events
            // change button to edit log
            // remove schedule date
        }
    }

    // toggle between month view and week view
    private void toggleView() {
        android.util.Log.i(null, "Pressed Toggle");
        if (isCalendarView) {
            android.util.Log.i(null, "Changing to WeekView");
            toggleViewButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.outline_calendar_month_24,0,0,0);
            calendarView.setVisibility(View.GONE);
            weekCalendarView.setVisibility(View.VISIBLE);
            viewType = "week";
            if (selectedDate.isAfter(LocalDate.now())){
                loadScheduleForDate(selectedDate,viewType);
            }
            else if (selectedDate.isBefore(LocalDate.now())){
                loadEventsForDate(selectedDate, viewType);
            }
            else loadForToday(viewType);
            setupWeekView();
        } else {
            android.util.Log.i(null, "Changing to CalendarView");
            toggleViewButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.outline_calendar_view_day_24,0,0,0);
            calendarView.setVisibility(View.VISIBLE);
            weekCalendarView.setVisibility(View.GONE);
            viewType = "month";
            if (selectedDate.isAfter(LocalDate.now())){
                loadScheduleForDate(selectedDate,viewType);
            }
            else if (selectedDate.isBefore(LocalDate.now())){
                loadEventsForDate(selectedDate, viewType);
            }
            else loadForToday(viewType);
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
            selectedDateContainer.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.transparent)));
            selectedDateContainer.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.black)));
            selectedDateContainer.select.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.transparent)));
            android.util.Log.i("toggle Select", "getAllScheduledDates: " + (getAllScheduledDates(scheduleList).contains(selectedDate)) );
            if (getAllScheduledDates(scheduleList).contains(selectedDate) && selectedDate.isAfter(LocalDate.now())){
                android.util.Log.i("toggle Select", "indicator on");
                selectedDateContainer.scheduleIndicator.setVisibility(View.VISIBLE);
            }
            else {
                android.util.Log.i("toggle Select", "indicator off");
                selectedDateContainer.scheduleIndicator.setVisibility(View.GONE);
            }
        }
        // Select the new date
        int backgroundColor = ContextCompat.getColor(Feeding.this, R.color.mid_green);
        container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.transparent)));
        container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.white)));
        container.select.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        selectedDate = clickedDate;
        selectedDateContainer = container;
        container.scheduleIndicator.setVisibility(View.GONE);
    }

    // Add Schedule to date
    public void addSchedule(int index, String notiType){
        FeedSchedule newSched = scheduleList.get(index);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String date = selectedDate.format(formatter);
        ArrayList<String> dates;
        if (newSched.getDates() != null){
            dates = newSched.getDates();
        }
        else dates = new ArrayList<>();
        dates.add(date);
        newSched.setDates(dates);
        scheduleList.set(index, newSched);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        for (int i = 0; i < user.getTanks().size(); i++) {
            Tank tank = user.getTanks().get(i);
            if (tank.getTankID() == targetTankID) {
                tank.setFeedSchedule(scheduleList);
                user.getTanks().set(i, tank);
                user.setTanks(user.getTanks());
                break;
            }
        }

        android.util.Log.i(null, "after set tank");
        reference.child(user.getUsername()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        android.util.Log.i("FirebaseUpdate", "Feed Schedule: "+ user.getTanks().get(0).getFeedSchedule().size());
                        // Dismiss the dialog or close the activity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        android.util.Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                    }
                });
        refreshData();
    }
    // Replace schedule
    public void replaceSchedule(FeedSchedule newSchedule){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        FeedSchedule old = findFeedScheduleByDate(scheduleList, selectedDate);
        int indexOld = scheduleList.indexOf(old);
        ArrayList<String> dates;
        if (old.getDates() != null){
            dates = new ArrayList<>(old.getDates());
        }
        else dates = new ArrayList<>();
        dates.remove(formatter.format(selectedDate));
        old.setDates(dates);

        int indexNew = scheduleList.indexOf(newSchedule);
        String name = newSchedule.getScheduleName();
        for (FeedSchedule schedule: scheduleList){
            if (schedule.getScheduleName().equals(name)){
                newSchedule = schedule;
                indexNew = scheduleList.indexOf(schedule);
            }
        }

        ArrayList<String> datesNew;
        if (newSchedule.getDates() != null){
            datesNew = new ArrayList<>(newSchedule.getDates());
        }
        else datesNew = new ArrayList<>();
        datesNew.add(formatter.format(selectedDate));
        newSchedule.setDates(datesNew);

        scheduleList.set(indexOld, old);
        scheduleList.set(indexNew, newSchedule);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        for (int i = 0; i < user.getTanks().size(); i++) {
            Tank tank = user.getTanks().get(i);
            if (tank.getTankID() == targetTankID) {
                tank.setFeedSchedule(scheduleList);
                user.getTanks().set(i, tank);
                user.setTanks(user.getTanks());
                break;
            }
        }

        android.util.Log.i(null, "after set tank");
        reference.child(user.getUsername()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        android.util.Log.i("FirebaseUpdate", "Feed Schedule: "+ user.getTanks().get(0).getFeedSchedule().size());
                        // Dismiss the dialog or close the activity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        android.util.Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                    }
                });
        refreshData();
    }

    // Create Schedule
    public void createSchedule(){
        scheduleList = tank.getFeedSchedule();
        if (scheduleList.size() >= 3){
            Toast.makeText(Feeding.this, "Limit reached", Toast.LENGTH_SHORT).show();
        }
        else
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String startDate = selectedDate.format(formatter);
            CreateSchedule dialogFragment = CreateSchedule.newInstance(user, tank, greenFood, brownFood, startDate);
            dialogFragment.show(getSupportFragmentManager(), "CreateSchedule");
            android.util.Log.i("createSchedule", "before refresh");
            getSupportFragmentManager().setFragmentResultListener("requestKey", Feeding.this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                    // Handle the result here
                    refreshData();
                    if (isCalendarView) {
                        setupCalendarView();
                    } else {
                        setupWeekView();
                    }
                }
            });
        }
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
                                if (tank.getFeedSchedule()!=null){
                                    scheduleList = tank.getFeedSchedule();
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
                for (Log log : tank.getFeedingLog()) {
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
        scheduleList = tank.getFeedSchedule();
        android.util.Log.i("Refresh Data", "Schedule List: " + scheduleList.size());
    }

    // generate scheduled dates
    public void regenerateDates(FeedSchedule feedSchedule) {
        // individual
        ArrayList<LocalDate> scheduledDates = new ArrayList<>();
        LocalDate refDate = LocalDate.parse(feedSchedule.getRefDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        LocalDate nextMonth = LocalDate.now().plusMonths(3); // continues generating 3 months worth of dates

        String repeatType = feedSchedule.getRepeatType();
        HashMap<String, ArrayList<String>> repeatDetails = feedSchedule.getRepeatDetails();
        int index = scheduleList.indexOf(feedSchedule);
        if (LocalDate.now().isEqual(refDate.plusWeeks(1))){
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            feedSchedule.setRefDate(formatter.format(LocalDate.now()));
            refDate = LocalDate.parse(feedSchedule.getRefDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            if (repeatType.equals("Don't repeat")) {
                scheduledDates.add(refDate);
            }
            else if (repeatType.equals("Everyday")) {
                for (LocalDate date = refDate; !date.isAfter(nextMonth); date = date.plusDays(1)) {
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
                int repeatOften = Integer.parseInt(feedSchedule.getRepeatType().replaceAll("[^0-9]", ""));
                for (LocalDate date = refDate; !date.isAfter(nextMonth); date = date.plusWeeks(repeatOften)) {
                    for (String day : daysOfWeek) {
                        DayOfWeek dayOfWeek = DayOfWeek.valueOf(day.toUpperCase());
                        LocalDate weekDate = date.with(dayOfWeek);
                        if (!weekDate.isBefore(refDate) && !weekDate.isAfter(nextMonth)) {
                            scheduledDates.add(weekDate);
                        }
                    }
                }
            }
            else if (repeatType.contains("month")) {
                android.util.Log.i("generateScheduledDates", "month");
                String keyWithMonth = null;
                for (String key : repeatDetails.keySet()) {
                    if (key.toLowerCase().contains("month")) {
                        keyWithMonth = key;
                        break;
                    }
                }
                String daysOfMonth = repeatDetails.get(keyWithMonth).get(0);
                int repeatOften = Integer.parseInt(feedSchedule.getRepeatType().replaceAll("[^0-9]", ""));
                addDatesForNextMonths(daysOfMonth, repeatOften, scheduledDates);
            }
            ArrayList<String> dateList = new ArrayList<>();
            for (LocalDate dates : scheduledDates){
                dateList.add(formatter.format(dates));
            }
            feedSchedule.setDates(dateList);
            scheduleList.set(index, feedSchedule);
            database = FirebaseDatabase.getInstance();
            reference = database.getReference("users");

            for (int i = 0; i < user.getTanks().size(); i++) {
                Tank tank = user.getTanks().get(i);
                if (tank.getTankID() == targetTankID) {
                    tank.setFeedSchedule(scheduleList);
                    user.getTanks().set(i, tank);
                    user.setTanks(user.getTanks());
                    break;
                }
            }

            android.util.Log.i(null, "after set tank");
            reference.child(user.getUsername()).setValue(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            android.util.Log.i("FirebaseUpdate", "Update");
                            // Dismiss the dialog or close the activity
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            android.util.Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                        }
                    });
        }
    }
    public static void addDatesForNextMonths(String input, int everyNMonths, List<LocalDate> scheduledDates) {
        try {
            String[] parts = input.split(" ");
            int nth = Integer.parseInt(parts[0].substring(0, parts[0].length() - 2)); // Get the number part (1st, 2nd, etc.)
            String dayOfWeekStr = parts[1];

            // Get the day of week enum from string
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayOfWeekStr.toUpperCase());

            // Get the current month
            LocalDate today = LocalDate.now();

            // Loop through the specified number of occurrences, incrementing by everyNMonths
            for (int i = 0; i < 12; i++) {
                LocalDate firstDayOfMonth = today.plusMonths(i * everyNMonths).withDayOfMonth(1);
                LocalDate firstOccurrence = firstDayOfMonth.with(TemporalAdjusters.firstInMonth(dayOfWeek));

                // Calculate the nth occurrence in the month
                LocalDate date = firstOccurrence.plusWeeks(nth - 1);

                // Check if the calculated date is within the same month
                if (date.getMonth() == firstDayOfMonth.getMonth()) {
                    scheduledDates.add(date);
                } else {
                    System.out.println("The " + nth + " " + dayOfWeekStr + " does not exist in " + firstDayOfMonth.getMonth());
                }
            }
        } catch (Exception e) {
            // Handle parsing errors or invalid input format
            android.util.Log.i("addDatesforMonth", "Invalid input format: " + input);
        }
    }
    public static FeedSchedule findFeedScheduleByDate(ArrayList<FeedSchedule> scheduleList, LocalDate targetDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (FeedSchedule schedule : scheduleList){
            if (schedule.getDates()!= null){
                if (schedule.getDates().contains(formatter.format(targetDate))) {
                    return schedule;
                }
            }
        }
        return null;
    }

    public static List<LocalDate> getAllScheduledDates(ArrayList<FeedSchedule> scheduleList) {
        // all schedule dates
        List<LocalDate> allScheduledDates = new ArrayList<>();
        for (FeedSchedule schedule : scheduleList){
            if (schedule.getDates() != null){
                for (String s : schedule.getDates()){
                    allScheduledDates.add(LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
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

