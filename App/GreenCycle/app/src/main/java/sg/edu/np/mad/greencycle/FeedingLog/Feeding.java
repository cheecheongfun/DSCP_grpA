package sg.edu.np.mad.greencycle.FeedingLog;


import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
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
import sg.edu.np.mad.greencycle.Classes.NotificationReceiver;
import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.TankSelection.TankSelection;

public class Feeding extends AppCompatActivity {

    CalendarView calendarView;
    WeekCalendarView weekCalendarView;
    RelativeLayout logs, fixedTitleContainer, titlesContainer, scheduleDetails;
    TextView toggleViewButton, backBtn, notesFixed, noLogs, monthHead;
    TextView noDetails, greenText, brownText, scheduleTitle;
    TextView gHead, bHead;
    boolean isCalendarView = true;
    boolean isSelected = true;
    boolean isCancelled;
    LocalDate selectedDate;
    LogAdapter gAdapter, bAdapter;
    ArrayList<DayOfWeek> daysOfWeek;
    ArrayList<Food> greens, browns;
    ArrayList<FeedSchedule> scheduleList;
    RecyclerView gRecycler, bRecycler, greenRecycler, brownRecycler;
    ImageButton gAdd, bAdd, camera, upload;
    FirebaseDatabase database;
    DatabaseReference reference;
    User user;
    Tank tank;
    EditText editNotes;
    Button confirm, schedule, delete, add, logDelete, logSave;
    String today, viewType;
    LinearLayout dayHead, bottomBtnSchedule, bottomBtnLog, details, notesSection, imageButtons;
    DayViewContainer selectedDateContainer = null;
    int selectedIndex, targetTankID;
    ArrayList<Log> feedingLog;
    ArrayList<String> greenFood, brownFood;
    FrameLayout imageFragment;
    galleryAdapter adapter;
    Log selectedLog;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_CODE_CREATE_SCHEDULE = 3;
    private StorageReference mStorageRef;
    private Uri imageUri;
    FoodAdapter greenAdapter;
    FoodAdapter brownAdapter;
    int selectedLogID = -1;
    int nightModeFlags, textColor;
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
        mStorageRef = FirebaseStorage.getInstance().getReference();
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
        logs = findViewById(R.id.logs);
        toggleViewButton = findViewById(R.id.toggle);
        backBtn = findViewById(R.id.backButton);
        confirm = findViewById(R.id.bottomBtn);
        fixedTitleContainer = findViewById(R.id.fixedTitleContainer);
        titlesContainer = fixedTitleContainer.findViewById(R.id.titlesContainer);
        dayHead = titlesContainer.findViewById(R.id.dayTitles);
        monthHead = titlesContainer.findViewById(R.id.monthTitles);
        bottomBtnLog = findViewById(R.id.bottomBtnLog);
        logDelete = bottomBtnLog.findViewById(R.id.logDelete);
        logSave = bottomBtnLog.findViewById(R.id.logSave);


        // log + feeding event
        gHead = logs.findViewById(R.id.greenText);
        bHead = logs.findViewById(R.id.brownText);
        gRecycler = logs.findViewById(R.id.greenRecycler);
        bRecycler = logs.findViewById(R.id.brownRecycler);
        gAdd = logs.findViewById(R.id.addGreen);
        bAdd = logs.findViewById(R.id.addBrown);

        details = logs.findViewById(R.id.details);
        notesSection = logs.findViewById(R.id.notesSection);
        notesFixed = logs.findViewById(R.id.notesFixed);
        editNotes = logs.findViewById(R.id.notesDescription);
        noLogs = logs.findViewById(R.id.noLogsText);
        imageButtons = logs.findViewById(R.id.imageButtons);
        camera = imageButtons.findViewById(R.id.camera);
        upload = imageButtons.findViewById(R.id.upload);
        imageFragment = logs.findViewById(R.id.imageFragment);

        // schedule details
        scheduleDetails =  findViewById(R.id.scheduleDetail);
        greenRecycler = scheduleDetails.findViewById(R.id.greenRecycler);
        brownRecycler = scheduleDetails.findViewById(R.id.brownRecycler);
        noDetails = scheduleDetails.findViewById(R.id.noDetailsText);
        greenText = scheduleDetails.findViewById(R.id.greens);
        brownText= scheduleDetails.findViewById(R.id.browns);
        scheduleTitle= scheduleDetails.findViewById(R.id.scheduleName);
        bottomBtnSchedule = findViewById(R.id.bottomBtnSchedule);
        delete = bottomBtnSchedule.findViewById(R.id.scheduleDelete);
        add = bottomBtnSchedule.findViewById(R.id.scheduleAdd);
        schedule = fixedTitleContainer.findViewById(R.id.schedule);

        selectedIndex =0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        today = formatter.format(new Date());
        selectedDate = LocalDate.now();
        viewType = "month";

        greens = new ArrayList<>();
        browns = new ArrayList<>();
        nightModeFlags = Feeding.this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags){
            case Configuration.UI_MODE_NIGHT_YES:
                textColor = ContextCompat.getColor(Feeding.this, R.color.white);
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                textColor = ContextCompat.getColor(Feeding.this, R.color.textColour);
                break;
        }
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
            if (schedule.getRefDate() != null){
                regenerateDates(scheduleList.indexOf(schedule));
            }
        }
        refreshData(() -> loadForToday(viewType));
        setupCalendarView();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.util.Log.i("add log", "in add");
                if (isCalendarView){
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
                    isCalendarView = !isCalendarView;
                }
                gHead.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_add_24,0);
                bHead.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_add_24,0);
                noLogs.setVisibility(View.GONE);
                details.setVisibility(View.VISIBLE);
                confirm.setVisibility(View.GONE);
                bottomBtnLog.setVisibility(View.VISIBLE);
                toggleViewButton.setVisibility(View.GONE);
                logDelete.setText("Cancel");
                logSave.setText("Save");

                // Green Food section
                ArrayList<Food> greenFoodList = new ArrayList<>();
                gHead.setVisibility(View.VISIBLE);
                gRecycler.setVisibility(View.VISIBLE);
                greenAdapter = new FoodAdapter(greenFood, gRecycler, "green", greenFoodList);
                gRecycler.setLayoutManager(new LinearLayoutManager(Feeding.this));
                gRecycler.setAdapter(greenAdapter);
                gHead.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        greenAdapter.addItem();
                    }
                });


                // Brown Food section
                ArrayList<Food> brownFoodList = new ArrayList<>();
                bHead.setVisibility(View.VISIBLE);
                bRecycler.setVisibility(View.VISIBLE);
                brownAdapter = new FoodAdapter(brownFood, bRecycler, "brown", brownFoodList);
                bRecycler.setLayoutManager(new LinearLayoutManager(Feeding.this));
                bRecycler.setAdapter(brownAdapter);

                bHead.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        brownAdapter.addItem();
                    }
                });

                // notes section
                notesSection.setVisibility(View.VISIBLE);
                notesFixed.setVisibility(View.GONE);
                editNotes.setVisibility(View.VISIBLE);

                // image section
                imageButtons.setVisibility(View.VISIBLE);
                upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chooseImage(selectedLog);
                    }
                });

                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestCameraPermission();
                        loadImage();
                    }
                });
            }

        });
        logDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // deleting log
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                if (logDelete.getText().equals("Delete")){
                    confirm.setText("Add log");
                    confirm.setVisibility(View.VISIBLE);
                    bottomBtnLog.setVisibility(View.GONE);
                    toggleViewButton.setVisibility(View.VISIBLE);
                    ArrayList<Log> logList = tank.getFeedingLog();
                    for (Log log : logList){
                        if (log.getLogDate().equals(formatter.format(selectedDate))){
                            selectedLog = log;
                        }
                    }
                    logList.remove(selectedLog);
                    database = FirebaseDatabase.getInstance();
                    reference = database.getReference("users");

                    for (int i = 0; i < user.getTanks().size(); i++) {
                        Tank tank = user.getTanks().get(i);
                        if (tank.getTankID() == targetTankID) {
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
                                    android.util.Log.i("FirebaseUpdate", "Success");
                                    // Dismiss the dialog or close the activity
                                    refreshData(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isCalendarView){
                                                setupCalendarView();
                                            } else setupWeekView();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    android.util.Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                                }
                            });
                }
                else {
                    if (isCalendarView){
                        setupCalendarView();
                    } else setupWeekView();
                }
            }
        });
        logSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                if (logSave.getText().toString().equals("Save")){
                    android.util.Log.i("logSave", "in save");
                    logSave.setText("Edit");
                    logDelete.setText("Delete");
                    ArrayList<Log> logList;
                    if (!tank.getFeedingLog().isEmpty()){
                        android.util.Log.i("logSave", "logList not empty");
                        logList = tank.getFeedingLog();
                        if (selectedLogID == -1){
                            android.util.Log.i("logSave", "no log");
                            // no log for current selected date
                            selectedLog = new Log(logList.size(), formatter.format(selectedDate), null, null, null);
                            selectedLogID = selectedLog.getLogId();
                            android.util.Log.i("logSave", "id: " + selectedLogID);
                            logList.add(selectedLog);
                        }
                        else{
                            android.util.Log.i("logSave", "log present, index: " + selectedLogID);
                            for (Log log: logList) {
                                if (log.getLogId() == selectedLogID) {
                                    // there is a log for the current selected date
                                    selectedLog = log;
                                    break;
                                }
                            }
                        }
                    }
                    else{
                        // LogList is empty (user had no logs for this tank)
                        logList = new ArrayList<>();
                        selectedLog = new Log(0, formatter.format(selectedDate), null, null, null);
                        logList.add(selectedLog);
                        selectedLogID = 0;
                    }
                    if (!editNotes.getText().toString().isEmpty()){
                        selectedLog.setNotes(editNotes.getText().toString());
                    }
                    if (greenAdapter.getSelectedFoods()!=null && !greenAdapter.getSelectedFoods().isEmpty()){
                        selectedLog.setGreens(greenAdapter.getSelectedFoods());
                    } else selectedLog.setGreens(null);
                    if (brownAdapter.getSelectedFoods()!=null && !brownAdapter.getSelectedFoods().isEmpty()){
                        selectedLog.setBrowns(brownAdapter.getSelectedFoods());
                    } else selectedLog.setBrowns(null);

                    for (Log log: logList) {
                        if (log.getLogId() == selectedLogID) {
                            // there is a log for the current selected date
                            logList.set(logList.indexOf(log), selectedLog);
                            break;
                        }
                    }
                    database = FirebaseDatabase.getInstance();
                    reference = database.getReference("users");

                    for (int i = 0; i < user.getTanks().size(); i++) {
                        Tank tank = user.getTanks().get(i);
                        if (tank.getTankID() == targetTankID) {
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
                                    android.util.Log.i("FirebaseUpdate", "Success");
                                    // Dismiss the dialog or close the activity
                                    refreshData(new Runnable() {
                                        @Override
                                        public void run() {
                                            confirm.setText("Edit log");
                                            confirm.setVisibility(View.VISIBLE);
                                            bottomBtnLog.setVisibility(View.GONE);
                                            toggleViewButton.setVisibility(View.VISIBLE);
                                            if (isCalendarView){
                                                setupCalendarView();
                                            } else setupWeekView();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    android.util.Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                                }
                            });
                }
                else if (logSave.getText().toString().equals("Edit")) {
                    android.util.Log.i("edit log", "in edit");
                    if (isCalendarView){
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
                        isCalendarView = !isCalendarView;
                    }
                    logSave.setText("Save");
                    logDelete.setText("Cancel");
                    gHead.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_add_24,0);
                    bHead.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_add_24,0);
                    confirm.setVisibility(View.GONE);
                    bottomBtnLog.setVisibility(View.VISIBLE);
                    toggleViewButton.setVisibility(View.GONE);

                    for (Log log : tank.getFeedingLog()){
                        if (selectedLogID == log.getLogId()){
                            selectedLog = log;
                        }
                    }
                    // Green Food section
                    ArrayList<Food> greenFoodList;
                    if ( selectedLog.getGreens() == null ||  selectedLog.getGreens().isEmpty()){
                        greenFoodList = new ArrayList<>();
                    } else greenFoodList = selectedLog.getGreens();
                    gHead.setVisibility(View.VISIBLE);
                    gRecycler.setVisibility(View.VISIBLE);
                    greenAdapter = new FoodAdapter(greenFood, gRecycler, "green", greenFoodList);
                    gRecycler.setLayoutManager(new LinearLayoutManager(Feeding.this));
                    gRecycler.setAdapter(greenAdapter);
                    gHead.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            greenAdapter.addItem();
                        }
                    });


                    // Brown Food section
                    ArrayList<Food> brownFoodList;
                    if ( selectedLog.getBrowns() == null ||  selectedLog.getBrowns().isEmpty()){
                        brownFoodList = new ArrayList<>();
                    } else brownFoodList = selectedLog.getBrowns();
                    bHead.setVisibility(View.VISIBLE);
                    bRecycler.setVisibility(View.VISIBLE);
                    brownAdapter = new FoodAdapter(brownFood, bRecycler, "brown", brownFoodList);
                    bRecycler.setLayoutManager(new LinearLayoutManager(Feeding.this));
                    bRecycler.setAdapter(brownAdapter);

                    bHead.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            brownAdapter.addItem();
                        }
                    });

                    // notes section
                    notesSection.setVisibility(View.VISIBLE);
                    notesFixed.setVisibility(View.GONE);
                    editNotes.setVisibility(View.VISIBLE);
                    editNotes.setText(selectedLog.getNotes());

                    // image section
                    imageButtons.setVisibility(View.VISIBLE);
                }
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
                refreshData(new Runnable() {
                    @Override
                    public void run() {
                        if (isCalendarView){
                            setupCalendarView();
                        } else setupWeekView();
                    }
                });
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
                    newLog = new Log(0, formatter.format(selectedDate), feedSched.getGreenFood(), feedSched.getBrownFood(), null );
                }
                newLog = new Log(tank.getFeedingLog().size(), formatter.format(selectedDate), feedSched.getGreenFood(), feedSched.getBrownFood(), null);

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
                refreshData(new Runnable() {
                    @Override
                    public void run() {
                        if (isCalendarView){
                            setupCalendarView();
                        } else setupWeekView();
                    }
                });
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
                refreshData(new Runnable() {
                    @Override
                    public void run() {
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
                                                Toast.makeText(Feeding.this, "No schedule selected", Toast.LENGTH_SHORT).show();
                                                refreshData(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (isCalendarView){
                                                            setupCalendarView();
                                                        } else setupWeekView();
                                                    }
                                                });
                                            }
                                            else{
                                                replaceSchedule(adapter.getNewFeedSchedule());
                                            }

                                        })
                                        .setNegativeButton("Cancel", (dialog, which) -> {
                                            refreshData(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isCalendarView){
                                                        setupCalendarView();
                                                    } else setupWeekView();
                                                }
                                            });
                                        })
                                        .setNeutralButton("New Schedule", (dialog, which) -> {
                                            refreshData(()->createSchedule());
                                        });

                            }
                            else{
                                android.util.Log.i("schedule", "no schedule for today");
                                FeedScheduleAdapter adapter = new FeedScheduleAdapter(Feeding.this, scheduleList, user, tank, scheduleList);
                                scheduleListView.setAdapter(adapter);

                                builder.setView(dialogView)
                                        .setPositiveButton("Set", (dialog, which) -> {

                                            if (scheduleList.isEmpty()){
                                                Toast.makeText(Feeding.this, "No schedule selected", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                addSchedule(adapter.getSelectedIndex(), adapter.getSelectedNotificationType());
                                            }
                                            refreshData(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isCalendarView){
                                                        setupCalendarView();
                                                    } else setupWeekView();
                                                }
                                            });
                                        })
                                        .setNegativeButton("Cancel", (dialog, which) -> {
                                            refreshData(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isCalendarView){
                                                        setupCalendarView();
                                                    } else setupWeekView();
                                                }
                                            });
                                        })
                                        .setNeutralButton("New Schedule", (dialog, which) -> {
                                            if (getAllScheduledDates(scheduleList).contains(selectedDate)){
                                                Toast.makeText(Feeding.this, "One schedule a day", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                refreshData(()->createSchedule());
                                            }
                                        });
                            }
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        else{
                            Intent intent = new Intent(Feeding.this, CreateSchedule.class);

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String date = selectedDate.format(formatter);

                            Bundle info = new Bundle();
                            info.putParcelable("tank", tank);
                            info.putParcelable("user", user);
                            info.putStringArrayList("greenFood", greenFood);
                            info.putStringArrayList("brownFood", brownFood);
                            info.putString("date", date);
                            intent.putExtras(info);
                            ((Activity) Feeding.this).startActivityForResult(intent, REQUEST_CODE_CREATE_SCHEDULE);
                        }
                    }
                });

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
        if (selectedDate.isAfter(LocalDate.now())){
            loadScheduleForDate(selectedDate,viewType);
        }
        else if (selectedDate.isBefore(LocalDate.now())){
            loadEventsForDate(selectedDate, viewType);
        }
        else loadForToday(viewType);
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
                    container.dayText.setTextColor(textColor);
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
                    container.dayText.setTextColor(ContextCompat.getColor(Feeding.this, R.color.android_hint));
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
                if (selectedDate!=null && day.getDate().isEqual(selectedDate)){
                    int backgroundColor = ContextCompat.getColor(Feeding.this, R.color.mid_green);
                    container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.transparent)));
                    container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.white)));
                    container.select.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
                    selectedDate = day.getDate();
                    selectedDateContainer = container;
                }
                else if (selectedDate == null && day.getDate().equals(LocalDate.now())){
                    int backgroundColor = ContextCompat.getColor(Feeding.this, R.color.mid_green);
                    container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.transparent)));
                    container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.white)));
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
        if (selectedDate.isAfter(LocalDate.now())){
            loadScheduleForDate(selectedDate,viewType);
        }
        else if (selectedDate.isBefore(LocalDate.now())){
            android.util.Log.e("CHECK", "here2");
            loadEventsForDate(selectedDate, viewType);
        }
        else loadForToday(viewType);
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
                container.dayText.setTextColor(textColor);
                container.dayText.setOnClickListener(v -> {
                    toggleSelect(container, weekDay.getDate().getYear(), weekDay.getDate().getMonthValue());
                    if (selectedDate.isAfter(LocalDate.now())){
                        loadScheduleForDate(weekDay.getDate(),viewType);
                    }
                    else if (selectedDate.isBefore(LocalDate.now())){
                        android.util.Log.e("CHECK", "here1");
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
                }
                else if (selectedDate == null && weekDay.getDate().equals(LocalDate.now())){
                    int backgroundColor = ContextCompat.getColor(Feeding.this, R.color.mid_green);
                    container.dayText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.transparent)));
                    container.dayText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.white)));
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
        scheduleDetails.setVisibility(View.GONE);
        bottomBtnSchedule.setVisibility(View.GONE);
        bottomBtnLog.setVisibility(View.GONE);
        toggleViewButton.setVisibility(View.VISIBLE);
        confirm.setVisibility(View.VISIBLE);
        schedule.setVisibility(View.GONE);
        schedule.setText("Schedule");
        logs.setVisibility(View.GONE);
        noLogs.setVisibility(View.GONE);
        imageButtons.setVisibility(View.GONE);
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
                    if (log.getGreens() != null){
                        greens.addAll(log.getGreens());
                    }
                    if (log.getBrowns() != null){
                        browns.addAll(log.getBrowns());
                    }
                    notes = log.getNotes();
                    selectedLogID = log.getLogId();
                    android.util.Log.i("loadEvents", "selectedLogID: " + selectedLogID);
                    break;
                }
            }
            loadImage();
            bottomBtnLog.setVisibility(View.VISIBLE);
            confirm.setVisibility(View.GONE);
            logDelete.setText("Delete");
            logSave.setText("Edit");
            logs.setVisibility(View.VISIBLE);
            details.setVisibility(View.VISIBLE);
            noLogs.setVisibility(View.GONE);
            gHead.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            bHead.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            if (notes == null || notes.isEmpty()){
                notesSection.setVisibility(View.GONE);
            }
            else{
                notesFixed.setVisibility(View.VISIBLE);
                notesSection.setVisibility(View.VISIBLE);
                notesFixed.setText(notes);
            }
            if (greens.isEmpty() && browns.isEmpty()){
                android.util.Log.i(null, "In Load If");
                gHead.setVisibility(View.GONE);
                bHead.setVisibility(View.GONE);
                gRecycler.setVisibility(View.GONE);
                bRecycler.setVisibility(View.GONE);
            }
            else if (greens.isEmpty()){
                android.util.Log.i(null, "In Load Else If");
                gRecycler.setVisibility(View.GONE);
                bAdapter = new LogAdapter(Feeding.this, browns, "brown", null);
                bRecycler.setLayoutManager(new LinearLayoutManager(this));
                bRecycler.setAdapter(bAdapter);
                gHead.setVisibility(View.GONE);
                bHead.setVisibility(View.VISIBLE);
                bRecycler.setVisibility(View.VISIBLE);
            }
            else if (browns.isEmpty()){
                bRecycler.setVisibility(View.GONE);
                gAdapter = new LogAdapter(Feeding.this, greens, "green", null);
                gRecycler.setLayoutManager(new LinearLayoutManager(this));
                gRecycler.setAdapter(gAdapter);
                bHead.setVisibility(View.GONE);
                gHead.setVisibility(View.VISIBLE);
                gRecycler.setVisibility(View.VISIBLE);
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

                gHead.setVisibility(View.VISIBLE);
                bHead.setVisibility(View.VISIBLE);
                gRecycler.setVisibility(View.VISIBLE);
                bRecycler.setVisibility(View.VISIBLE);
            }
        } else {
            confirm.setText("Add log");
            android.util.Log.i("loadEvents", "no log");
            logs.setVisibility(View.VISIBLE);
            details.setVisibility(View.GONE);
            noLogs.setVisibility(View.VISIBLE);
            if (date.equals(LocalDate.now())){
                schedule.setVisibility(View.VISIBLE);
            }
            selectedLogID = -1;
            android.util.Log.i("loadEvents", "selectedLogID: " + selectedLogID);
        }
    }

    private void loadScheduleForDate(LocalDate date, String view) {
        confirm.setVisibility(View.GONE);
        logs.setVisibility(View.GONE);
        scheduleDetails.setVisibility(View.GONE);
        bottomBtnLog.setVisibility(View.GONE);
        schedule.setVisibility(View.VISIBLE);
        if (getAllScheduledDates(scheduleList).contains(date)){
            FeedSchedule sched = findFeedScheduleByDate(scheduleList, date);
            android.util.Log.i("load Schedule for date", "In if: " );
            if (date.equals(LocalDate.now())){
                bottomBtnSchedule.setVisibility(View.VISIBLE);
                android.util.Log.i("load Schedule for date", "contains today: " + LocalDate.now() );
            }
            else bottomBtnSchedule.setVisibility(View.GONE);
            schedule.setVisibility(View.VISIBLE);
            schedule.setText("Replace schedule");

            scheduleTitle.setText(sched.getScheduleName());

            noDetails.setText("No details");
            noDetails.setVisibility(View.GONE);
            scheduleDetails.setVisibility(View.VISIBLE);

            if (sched.getGreenFood() == null && sched.getBrownFood() == null){
                android.util.Log.i("load Schedule for date", "null" );
                noDetails.setVisibility(View.VISIBLE);
            }

            else if (sched.getGreenFood() == null){
                android.util.Log.i("load Schedule for date", "green null" );
                greenText.setVisibility(View.GONE);
                greenRecycler.setVisibility(View.GONE);
                brownText.setVisibility(View.VISIBLE);
                brownRecycler.setVisibility(View.VISIBLE);
                bAdapter = new LogAdapter(Feeding.this, sched.getBrownFood(), "brown", null);
                brownRecycler.setLayoutManager(new LinearLayoutManager(this));
                brownRecycler.setAdapter(bAdapter);
                noDetails.setVisibility(View.GONE);
            }
            else if (sched.getBrownFood() == null){
                android.util.Log.i("load Schedule for date", "brown null" );
                greenText.setVisibility(View.VISIBLE);
                greenRecycler.setVisibility(View.VISIBLE);
                brownText.setVisibility(View.GONE);
                brownRecycler.setVisibility(View.GONE);
                gAdapter = new LogAdapter(Feeding.this, sched.getGreenFood(), "green", null);
                greenRecycler.setLayoutManager(new LinearLayoutManager(this));
                greenRecycler.setAdapter(gAdapter);
                noDetails.setVisibility(View.GONE);
            }
            else {
                android.util.Log.i("load Schedule for date", "all details");
                noDetails.setVisibility(View.GONE);
                greenText.setVisibility(View.VISIBLE);
                greenRecycler.setVisibility(View.VISIBLE);
                brownText.setVisibility(View.VISIBLE);
                brownRecycler.setVisibility(View.VISIBLE);

                gAdapter = new LogAdapter(Feeding.this, sched.getGreenFood(), "green", null);
                greenRecycler.setLayoutManager(new LinearLayoutManager(this));
                greenRecycler.setAdapter(gAdapter);

                // brown recycler view
                bAdapter = new LogAdapter(Feeding.this, sched.getBrownFood(), "brown", null);
                brownRecycler.setLayoutManager(new LinearLayoutManager(this));
                brownRecycler.setAdapter(bAdapter);

            }
        }
        else {
            android.util.Log.i("load Schedule for date", "In else: " );
            bottomBtnSchedule.setVisibility(View.GONE);
            schedule.setText("Schedule");
            scheduleDetails.setVisibility(View.VISIBLE);
            scheduleTitle.setText("No Schedule");
            noDetails.setVisibility(View.VISIBLE);
            noDetails.setText(" ");
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
                android.util.Log.e("CHECK", "here4");
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
                android.util.Log.e("CHECK", "here5");
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
            selectedDateContainer.dayText.setTextColor(textColor);
            selectedDateContainer.select.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Feeding.this, R.color.transparent)));
            android.util.Log.i("toggle Select", "getAllScheduledDates: " + (getAllScheduledDates(scheduleList).contains(selectedDate)) );
            if (getAllScheduledDates(scheduleList).contains(selectedDate)){
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
    }

    // Add Schedule to date
    public void addSchedule(int index, String notiType){
        android.util.Log.i("addSchedule", "index: " + index);
        FeedSchedule newSched = scheduleList.get(index);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String date = selectedDate.format(formatter);
        if (newSched.getRefDate()==null){
            newSched.setRefDate(date);
        }

        ArrayList<String> dates;
        if (newSched.getDates() != null){
            dates = newSched.getDates();
        }
        else {
            dates = new ArrayList<>();
            ArrayList<LocalDate> localDates = new ArrayList<>();
            localDates = generateScheduledDates(newSched);
            for (LocalDate ld : localDates){
                dates.add(ld.format(formatter));
            }
        }
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
                        refreshData(new Runnable() {
                            @Override
                            public void run() {
                                if (isCalendarView){
                                    setupCalendarView();
                                } else setupWeekView();
                            }
                        });
                        scheduleNotifications(newSched);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        android.util.Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                    }
                });
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
        refreshData(new Runnable() {
            @Override
            public void run() {
                if (isCalendarView){
                    setupCalendarView();
                } else setupWeekView();
            }
        });
    }

    // Create Schedule
    public void createSchedule(){
        scheduleList = tank.getFeedSchedule();
        if (scheduleList.size() >= 3){
            Toast.makeText(Feeding.this, "Limit reached", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Intent intent = new Intent(Feeding.this, CreateSchedule.class);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String date = selectedDate.format(formatter);

            Bundle info = new Bundle();
            info.putParcelable("tank", tank);
            info.putParcelable("user", user);
            info.putStringArrayList("greenFood", greenFood);
            android.util.Log.i("createSchedule" , "greenFood; " + greenFood.get(0));
            info.putStringArrayList("brownFood", brownFood);
            info.putString("date", date);
            intent.putExtras(info);
            ((Activity) Feeding.this).startActivityForResult(intent, REQUEST_CODE_CREATE_SCHEDULE);
        }
    }

    // Refresh Data
    public void refreshData(Runnable onComplete) {
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
                                if (tank.getFeedSchedule() != null) {
                                    scheduleList = tank.getFeedSchedule();
                                } else {
                                    scheduleList = new ArrayList<>();
                                }
                                if (tank.getFeedingLog() != null) {
                                    feedingLog = tank.getFeedingLog();
                                } else {
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
                    if (tank.getFeedSchedule() != null){
                        for (FeedSchedule sched : tank.getFeedSchedule()){
                            if (sched.getGreenFood() != null){
                                for (Food food: sched.getGreenFood()){
                                    greenFood.add(food.getName());
                                }
                            }
                            if (sched.getBrownFood() != null){
                                for (Food food: sched.getBrownFood()){
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
                android.util.Log.i("Refresh Data", "GreenFood List: " + greenFood.size());
                tank = user.getTanks().get(targetTankID);
                android.util.Log.i("Refresh Data", "Schedule List: " + scheduleList.size());

                // Call the onComplete callback
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                android.util.Log.e("Firebase", "Failed to read user data.", error.toException());
                // Call the onComplete callback even if there is an error to avoid blocking
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    // generate scheduled dates
    public void regenerateDates(int index) {
        android.util.Log.i("regenerateDates", "regenerating");
        // individual
        FeedSchedule feedSchedule = scheduleList.get(index);
        ArrayList<LocalDate> scheduledDates = new ArrayList<>();
        LocalDate nextMonth = LocalDate.now().plusMonths(3); // continues generating 3 months worth of dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate refDate = LocalDate.parse(feedSchedule.getRefDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String repeatType = feedSchedule.getRepeatType();
        HashMap<String, ArrayList<String>> repeatDetails = feedSchedule.getRepeatDetails();
        if (LocalDate.now().isEqual(refDate.plusWeeks(1))) {
            feedSchedule.setRefDate(formatter.format(LocalDate.now()));
            refDate = LocalDate.parse(feedSchedule.getRefDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            if (repeatType.equals("Don't repeat")) {
                scheduledDates.add(refDate);
            } else if (repeatType.equals("Everyday")) {
                for (LocalDate date = refDate; !date.isAfter(nextMonth); date = date.plusDays(1)) {
                    scheduledDates.add(date);
                }
            } else if (repeatType.contains("week")) {
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
            } else if (repeatType.contains("month")) {
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
        }
        else {
            for (String s : feedSchedule.getDates()){
                scheduledDates.add(LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }


        ArrayList<String> dateList = new ArrayList<>();
        for (LocalDate dates : scheduledDates){
            if (!dates.isBefore(LocalDate.now())){
                dateList.add(formatter.format(dates));
            }
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        android.util.Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                    }
                });
    }
    public ArrayList<LocalDate> generateScheduledDates(FeedSchedule feedSchedule) {
        // individual
        ArrayList<LocalDate> scheduledDates = new ArrayList<>();

        LocalDate refDate = selectedDate;
        LocalDate nextMonth = LocalDate.now().plusMonths(3); // continues generating 3 months worth of dates
        String repeatType = feedSchedule.getRepeatType();
        HashMap<String, ArrayList<String>> repeatDetails = feedSchedule.getRepeatDetails();


        if (repeatType.equals("Don't repeat")) {
            scheduledDates.add(selectedDate);
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

        return scheduledDates;
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

    public void loadImage() {
        ImageFragment imageFragment = new ImageFragment(user, tank, Feeding.this, selectedDate);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.imageFragment, imageFragment);
        transaction.commit();
    }

    private void deleteImageFromFirestore(Map<String, String> imageData) {
        String docId = imageData.get("docId");  // Retrieve the document ID
        if (docId == null) {
            Toast.makeText(this, "Document ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUsername())
                .collection("Tanks").document(String.valueOf(tank.getTankID()))
                .collection("Images").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    adapter.imageDataList.remove(imageData);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(Feeding.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Feeding.this, "Error deleting image", Toast.LENGTH_SHORT).show();
                });
    }

    private void chooseImage(Log log) {
        selectedLog = log;  // Store the log in the member variable
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Log log : tank.getFeedingLog()){
            if (log.getLogDate().equals(formatter.format(selectedDate))){
                selectedLog = log;
            }
        }
        if (requestCode == REQUEST_CODE_CREATE_SCHEDULE) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getBooleanExtra("schedule_saved", false)) {
                android.util.Log.i("onActivityResult", "Schedule created successfully");
                refreshData(() -> schedule.callOnClick());
            }
        }
        else if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null){
            if (resultCode == Activity.RESULT_OK){
                imageUri = data.getData();
                if (selectedLog == null){
                    selectedLog = new Log();
                }
                uploadImage(selectedLog);  // Use the member variable
            }
        }
        else if (requestCode == CAPTURE_IMAGE_REQUEST){
            if (resultCode == Activity.RESULT_OK){
                if (selectedLog == null){
                    selectedLog = new Log();
                }
                uploadImage(selectedLog);
            }
        }
    }



    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                android.util.Log.e("Camera", "Error creating file", ex);
            }
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use the camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage(Log log) {
        if (imageUri != null) {
            isCancelled = false;
            Glide.with(this)
                    .asBitmap()
                    .load(imageUri)
                    .centerCrop()
                    .override(500, 500)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            if (!isCancelled) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                resource.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                                byte[] data = baos.toByteArray();
                                StorageReference fileRef = mStorageRef.child("images/" + System.currentTimeMillis() + ".jpg");
                                fileRef.putBytes(data).addOnSuccessListener(taskSnapshot ->
                                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                            if (!isCancelled) {
                                                uploadImageDetailsToFirestore(uri.toString(), log);
                                            }
                                        }).addOnFailureListener(e -> {
                                            Toast.makeText(Feeding.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        })
                                ).addOnFailureListener(e -> {
                                    Toast.makeText(Feeding.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageDetailsToFirestore(String imageUrl, Log log) {
        if (!isCancelled) {
            // Create timestamp for Firestore document
            LocalDate logDate = LocalDate.parse(log.getLogDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Calendar calendar = Calendar.getInstance();
            calendar.set(logDate.getYear(), logDate.getMonthValue() - 1, logDate.getDayOfMonth(), 0, 0, 0);
            Date date = calendar.getTime();
            Timestamp timestamp = new Timestamp(date);

            // Prepare image details
            Map<String, Object> imageDetails = new HashMap<>();
            imageDetails.put("image_url", imageUrl);
            imageDetails.put("timestamp", timestamp);

            // Upload to Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(user.getUsername())
                    .collection("Tanks").document(String.valueOf(tank.getTankID()))
                    .collection("Images").document()
                    .set(imageDetails)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Feeding.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        loadImage();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Feeding.this, "Upload Error", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void scheduleNotifications(FeedSchedule schedule) {
        String scheduleName = schedule.getScheduleName(); // Assuming each schedule has a unique ID
        ArrayList<String> feedingDatesString = schedule.getDates();
        ArrayList<LocalDate> feedingDates = new ArrayList<>();
        for (String s : feedingDatesString) {
            feedingDates.add(LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        for (LocalDate feedingDate : feedingDates) {
            long feedingDateInMillis = getMillisFromLocalDate(feedingDate);

            switch (schedule.getNotification()) {
                case "Don't notify":
                    // Do nothing
                    break;
                case "On the day itself":
                    scheduleNotification(scheduleName, 0, feedingDateInMillis, "feedingReminder");
                    break;
                default:
                    if (schedule.getNotification().contains("days before")) {
                        String noti = schedule.getNotification();
                        int daysBefore = extractDaysBefore(noti);
                        scheduleNotification(scheduleName, daysBefore, feedingDateInMillis, "feedingReminder");
                    }
                    break;
            }
        }
    }
    private int extractDaysBefore(String notificationType) {
        // Extract the number of days before from the string
        String[] parts = notificationType.split(" ");
        for (String part : parts) {
            try {
                return Integer.parseInt(part);
            } catch (NumberFormatException e) {
                // Ignore and continue
            }
        }
        return 0; // Default to 0 if no number found
    }

    private void scheduleNotification(String scheduleName, int daysBefore, long feedingDateInMillis, String notificationType) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("scheduleName", scheduleName);
        intent.putExtra("notificationType", notificationType);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, scheduleName.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(feedingDateInMillis);

        if (daysBefore > 0) {
            calendar.add(Calendar.DAY_OF_YEAR, -daysBefore);
        }

        // Set the time to 4 AM
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }


    private long getMillisFromLocalDate(LocalDate localDate) {
        ZoneId zoneId = ZoneId.systemDefault(); // or ZoneId.of("your-time-zone")
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(zoneId).plusHours(4); // Set the time to 4 AM
        Instant instant = zonedDateTime.toInstant();
        return instant.toEpochMilli();
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

