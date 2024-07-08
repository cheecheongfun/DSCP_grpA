package sg.edu.np.mad.greencycle.FeedingLog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import sg.edu.np.mad.greencycle.Classes.FeedSchedule;
import sg.edu.np.mad.greencycle.Classes.Food;
import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class CreateSchedule extends AppCompatActivity {

    EditText scheduleName, waterAmt, secondEdit, thirdEdit, fourthEdit;
    RelativeLayout greenSect, brownSect, waterSect, repeatSect, notificationSect, second, third, fourth, monthOptions;
    LinearLayout dayTitles, repeatOptions, notiOptions;
    TextView green, greenCancel, greenLine, brown, brownCancel, brownLine, repeatText, repeatCancel;
    TextView repeatLine, notiText, notiCancel, notiLine, cancel, save, monthOption1, monthOption2, text2, text3, text4;
    RecyclerView greenRecycler, brownRecycler;
    RadioButton repeatNone, repeatDaily, repeatWeekly, repeatMonthly, notiNone, notiDay, notiDayBefore;
    FoodAdapter gAdapter, bAdapter;
    private Tank tank;
    private ArrayList<String> greenFood, brownFood;
    ArrayList<Food> greenList, brownList;
    private User user;
    String repeatType, notiType, date;
    HashMap<String, ArrayList<String>> repeatDetails = new HashMap<>();
    ArrayList<String> weeklyDaysList;
    Set<String> weeklyDays = new HashSet<>();
    String[] dayAbbreviations;
    int water;
    FirebaseDatabase database;
    DatabaseReference reference;
    LocalDate selectedDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.schedule_create);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.create), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");
        greenFood = receivingEnd.getStringArrayListExtra("greenFood");
        brownFood = receivingEnd.getStringArrayListExtra("brownFood");
        date = receivingEnd.getStringExtra("date");

        // General
        scheduleName = findViewById(R.id.scheduleName);
        save = findViewById(R.id.scheduleSave);
        cancel = findViewById(R.id.scheduleCancel);

        scheduleName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(null, "schedule name on click");
            }
        });
        // Green section
        greenSect = findViewById(R.id.greenSect);
        green = findViewById(R.id.green);
        greenLine = findViewById(R.id.greenLine);
        greenCancel = findViewById(R.id.greenCancel);
        greenRecycler = findViewById(R.id.greenRecycler);

        // Brown section
        brownSect = findViewById(R.id.brownSect);
        brown = findViewById(R.id.brown);
        brownLine = findViewById(R.id.brownLine);
        brownCancel = findViewById(R.id.brownCancel);
        brownRecycler = findViewById(R.id.brownRecycler);

        // Water section
        waterSect = findViewById(R.id.waterSect);
        waterAmt = findViewById(R.id.waterAmt);

        // Repeat section
        repeatSect = findViewById(R.id.repeat);
        repeatText = findViewById(R.id.repeatText);
        repeatLine = findViewById(R.id.repeatLine);
        repeatCancel = findViewById(R.id.repeatCancel);
        // Radio group for options
        repeatOptions = findViewById(R.id.radioRepeatGroup);
        repeatNone = findViewById(R.id.radioRepeatNone);
        repeatDaily = findViewById(R.id.radioRepeatDaily);
        second = findViewById(R.id.second);
        repeatWeekly = findViewById(R.id.radioRepeatWeekly);
        secondEdit = findViewById(R.id.secondEdit);
        dayTitles = findViewById(R.id.dayTitles);
        third = findViewById(R.id.third);
        repeatMonthly = findViewById(R.id.radioRepeatMonthly);
        thirdEdit = findViewById(R.id.thirdEdit);
        monthOptions = findViewById(R.id.monthOptions);
        monthOption1 = findViewById(R.id.option1);
        monthOption2 = findViewById(R.id.option2);

        // Notification section
        notificationSect = findViewById(R.id.notificationSect);
        notiText = findViewById(R.id.notiText);
        notiCancel = findViewById(R.id.notiCancel);
        notiLine = findViewById(R.id.notiLine);
        // Radio group for options
        notiOptions = findViewById(R.id.radioNotiGroup);
        notiNone = notiOptions.findViewById(R.id.radioNotiNone);
        notiDay = notiOptions.findViewById(R.id.radioNotiDay);
        fourth = notiOptions.findViewById(R.id.fourth);
        notiDayBefore = fourth.findViewById(R.id.radioNotiDayBefore);
        fourthEdit = fourth.findViewById(R.id.fourthEdit);
        text2 = findViewById(R.id.text2);
        text3 = findViewById(R.id.text3);
        text4 = findViewById(R.id.text4);

        greenList = new ArrayList<>();
        brownList = new ArrayList<>();
        gAdapter = new FoodAdapter(greenFood, greenRecycler, "green", greenList);
        bAdapter = new FoodAdapter(brownFood, brownRecycler, "brown", brownList);

        // day of week titles
        dayAbbreviations = new String[]{"M", "T", "W", "T", "F", "S", "S"};
        scheduleName.requestFocus();
        greenCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setGone("green");
                green.setClickable(false);
            }
        });
        brownCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setGone("brown");
                brown.setClickable(false);
            }
        });
        repeatCancel.setOnClickListener(nev -> setGone("repeat"));
        notiCancel.setOnClickListener(nev -> setGone("noti"));
        // green section
        greenSect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (greenCancel.getVisibility() == View.VISIBLE){
                    greenCancel.callOnClick();
                }
                else {
                    setVisible("green");
                    gAdapter = new FoodAdapter(greenFood, greenRecycler, "green", greenList);
                    greenRecycler.setLayoutManager(new LinearLayoutManager(CreateSchedule.this));
                    greenRecycler.setAdapter(gAdapter);
                    green.setClickable(true);
                    green.setOnClickListener(v -> gAdapter.addItem());
                }
            }
        });

        // brown section
        brownSect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (brownCancel.getVisibility() == View.VISIBLE){
                    brownCancel.callOnClick();
                }
                else {
                    setVisible("brown");
                    bAdapter = new FoodAdapter(brownFood, brownRecycler, "brown", brownList);
                    brownRecycler.setLayoutManager(new LinearLayoutManager(CreateSchedule.this));
                    brownRecycler.setAdapter(bAdapter);
                    brown.setClickable(true);
                    brown.setOnClickListener(v-> bAdapter.addItem());
                }
            }
        });

        // water section
        waterAmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (greenCancel.getVisibility() == View.VISIBLE){
                    greenCancel.callOnClick();
                }
                if (brownCancel.getVisibility() == View.VISIBLE){
                    brownCancel.callOnClick();
                }
                if (repeatCancel.getVisibility() == View.VISIBLE){
                    repeatCancel.callOnClick();
                }
                if (notiCancel.getVisibility() == View.VISIBLE){
                    notiCancel.callOnClick();
                }
            }
        });

        // repeat section
        repeatType = "";
        repeatSect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeatCancel.getVisibility() == View.VISIBLE) {
                    repeatCancel.callOnClick();
                } else {
                    setVisible("repeat");
                    if (repeatType.isEmpty()){
                        repeatNone.setChecked(true);
                        repeatDaily.setChecked(false);
                        repeatWeekly.setChecked(false);
                        repeatMonthly.setChecked(false);
                        repeatType = "Don't repeat";
                    }
                    else
                    {
                        if (repeatType.equals("Everyday")){
                            repeatNone.setChecked(false);
                            repeatDaily.setChecked(true);
                            repeatWeekly.setChecked(false);
                            repeatMonthly.setChecked(false);
                        }
                        else if (repeatType.contains("week")){
                            repeatNone.setChecked(false);
                            repeatDaily.setChecked(false);
                            repeatWeekly.setChecked(true);
                            repeatMonthly.setChecked(false);
                            dayTitles.setVisibility(View.VISIBLE);
                        }
                        else if (repeatType.contains("month")){
                            repeatNone.setChecked(false);
                            repeatDaily.setChecked(false);
                            repeatWeekly.setChecked(false);
                            repeatMonthly.setChecked(true);
                            monthOptions.setVisibility(View.VISIBLE);
                        }
                        else {
                            repeatNone.setChecked(true);
                            repeatDaily.setChecked(false);
                            repeatWeekly.setChecked(false);
                            repeatMonthly.setChecked(false);
                        }

                    }

                    repeatNone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            repeatNone.setChecked(true);
                            repeatDaily.setChecked(false);
                            repeatWeekly.setChecked(false);
                            repeatMonthly.setChecked(false);
                            dayTitles.setVisibility(View.GONE);
                            monthOptions.setVisibility(View.GONE);
                            repeatType = "Don't repeat";
                            repeatText.setText(repeatType);
                        }
                    });

                    repeatDaily.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i(null, "daily on click");
                            repeatNone.setChecked(false);
                            repeatDaily.setChecked(true);
                            repeatWeekly.setChecked(false);
                            repeatMonthly.setChecked(false);
                            dayTitles.setVisibility(View.GONE);
                            monthOptions.setVisibility(View.GONE);
                            repeatType = "Everyday";
                            repeatText.setText(repeatType);
                        }
                    });

                    repeatWeekly.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i(null, "in repeatWeekly on click");
                            repeatNone.setChecked(false);
                            repeatDaily.setChecked(false);
                            repeatWeekly.setChecked(true);
                            repeatMonthly.setChecked(false);
                            dayTitles.setVisibility(View.VISIBLE);
                            monthOptions.setVisibility(View.GONE);
                            weeklyDays.clear();

                            for (int i = 0; i < dayTitles.getChildCount(); i++) {
                                final int index = i;
                                TextView textView = (TextView) dayTitles.getChildAt(i);
                                String abbreviation = dayAbbreviations[index];
                                textView.setText(abbreviation);
                                textView.setBackgroundColor(Color.TRANSPARENT);
                                textView.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));

                                DayOfWeek dayOfWeek = DayOfWeek.of(index + 1);
                                if (weeklyDays.contains(dayOfWeek.toString())) {
                                    textView.setBackgroundColor(ContextCompat.getColor(CreateSchedule.this, R.color.icon_brown_light));
                                    textView.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                                }

                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String dayString = dayOfWeek.toString();

                                        if (weeklyDays.contains(dayString)) {
                                            weeklyDays.remove(dayString);
                                            textView.setBackgroundColor(Color.TRANSPARENT);
                                            textView.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));
                                        } else {
                                            weeklyDays.add(dayString);
                                            textView.setBackgroundColor(ContextCompat.getColor(CreateSchedule.this, R.color.icon_brown_light));
                                            textView.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                                        }

                                        if (weeklyDays.size() == 7) {
                                            Log.i("WeeklyDays", "All days selected: Switching to daily");
                                            repeatDaily.setChecked(true);
                                            repeatWeekly.setChecked(false);
                                            repeatType = "Everyday";
                                            dayTitles.setVisibility(View.GONE);
                                        } else {
                                            repeatDaily.setChecked(false);
                                            repeatWeekly.setChecked(true);
                                            if (!secondEdit.getText().toString().isEmpty()){
                                                repeatType = "Every " + secondEdit.getText().toString() + " week";
                                            }
                                            else{
                                                repeatType = "Every 1 week";
                                            }
                                            dayTitles.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }

                            if (!secondEdit.getText().toString().isEmpty()){
                                repeatType = "Every " + secondEdit.getText().toString() + " week";
                            }
                            else{
                                repeatType = "Every 1 week";
                            }
                            repeatText.setText(repeatType);
                        }
                    });
                    secondEdit.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            repeatNone.setChecked(false);
                            repeatDaily.setChecked(false);
                            repeatWeekly.setChecked(true);
                            repeatMonthly.setChecked(false);
                            dayTitles.setVisibility(View.VISIBLE);
                            monthOptions.setVisibility(View.GONE);
                            weeklyDays.clear();

                            if (!editable.toString().isEmpty()){
                                repeatType = "Every " + editable + " week";
                            }
                            else{
                                repeatType = "Every 1 week";
                            }
                            repeatText.setText(repeatType);
                            repeatText.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                            for (int i = 0; i < dayTitles.getChildCount(); i++) {
                                final int index = i;
                                TextView textView = (TextView) dayTitles.getChildAt(i);
                                String abbreviation = dayAbbreviations[index];
                                textView.setText(abbreviation);
                                textView.setBackgroundColor(Color.TRANSPARENT);
                                textView.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));

                                DayOfWeek dayOfWeek = DayOfWeek.of(index + 1);
                                if (weeklyDays.contains(dayOfWeek.toString())) {
                                    textView.setBackgroundColor(ContextCompat.getColor(CreateSchedule.this, R.color.icon_brown_light));
                                    textView.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                                }

                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String dayString = dayOfWeek.toString();

                                        if (weeklyDays.contains(dayString)) {
                                            weeklyDays.remove(dayString);
                                            textView.setBackgroundColor(Color.TRANSPARENT);
                                            textView.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));
                                        } else {
                                            weeklyDays.add(dayString);
                                            textView.setBackgroundColor(ContextCompat.getColor(CreateSchedule.this, R.color.icon_brown_light));
                                            textView.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                                        }

                                        if (weeklyDays.size() == 7) {
                                            Log.i("WeeklyDays", "All days selected: Switching to daily");
                                            repeatDaily.setChecked(true);
                                            repeatWeekly.setChecked(false);
                                            repeatType = "Everyday";
                                            dayTitles.setVisibility(View.GONE);
                                        } else {
                                            repeatDaily.setChecked(false);
                                            repeatWeekly.setChecked(true);
                                            if (!editable.toString().isEmpty()){
                                                repeatType = "Every " + editable + " week";
                                            }
                                            else{
                                                repeatType = "Every 1 week";
                                            }
                                            dayTitles.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }
                        }
                    });
                    text2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            repeatNone.setChecked(false);
                            repeatDaily.setChecked(false);
                            repeatWeekly.setChecked(true);
                            repeatMonthly.setChecked(false);
                            repeatType = "Every 1 week";
                            repeatText.setText(repeatType);
                            repeatText.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));
                            dayTitles.setVisibility(View.VISIBLE);
                            monthOptions.setVisibility(View.GONE);
                            weeklyDays.clear();
                            for (int i = 0; i < dayTitles.getChildCount(); i++) {
                                final int index = i;
                                TextView textView = (TextView) dayTitles.getChildAt(i);
                                String abbreviation = dayAbbreviations[index];
                                textView.setText(abbreviation);
                                textView.setBackgroundColor(Color.TRANSPARENT);
                                textView.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));

                                DayOfWeek dayOfWeek = DayOfWeek.of(index + 1);
                                if (weeklyDays.contains(dayOfWeek.toString())) {
                                    textView.setBackgroundColor(ContextCompat.getColor(CreateSchedule.this, R.color.icon_brown_light));
                                    textView.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.icon_brown_light));
                                }

                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String dayString = dayOfWeek.toString();

                                        if (weeklyDays.contains(dayString)) {
                                            weeklyDays.remove(dayString);
                                            textView.setBackgroundColor(Color.TRANSPARENT);
                                            textView.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));
                                        } else {
                                            weeklyDays.add(dayString);
                                            textView.setBackgroundColor(ContextCompat.getColor(CreateSchedule.this, R.color.icon_brown_light));
                                            textView.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                                        }

                                        if (weeklyDays.size() == 7) {
                                            Log.i("WeeklyDays", "All days selected: Switching to daily");
                                            repeatDaily.setChecked(true);
                                            repeatWeekly.setChecked(false);
                                            repeatType = "Everyday";
                                            dayTitles.setVisibility(View.GONE);
                                        } else {
                                            repeatDaily.setChecked(false);
                                            repeatWeekly.setChecked(true);
                                            if (!secondEdit.getText().toString().isEmpty()){
                                                repeatType = "Every " + secondEdit.getText().toString() + " week";
                                            }
                                            else{
                                                repeatType = "Every 1 week";
                                            }
                                            dayTitles.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }

                            if (!secondEdit.getText().toString().isEmpty()){
                                repeatType = "Every " + secondEdit.getText().toString() + " week";
                            }
                            else{
                                repeatType = "Every 1 week";
                            }
                            repeatText.setText(repeatType);
                        }
                    });

                    repeatMonthly.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            repeatNone.setChecked(false);
                            repeatDaily.setChecked(false);
                            repeatWeekly.setChecked(false);
                            repeatMonthly.setChecked(true);
                            dayTitles.setVisibility(View.GONE);
                            monthOptions.setVisibility(View.VISIBLE);
                            LocalDate startDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                            monthOption1.setText("Every " + startDate.getDayOfMonth() + " of the month" );
                            monthOption2.setText("Every " + getNthOccurrenceAndDay(startDate) + " of the month");
                            if (!thirdEdit.getText().toString().isEmpty()){
                                repeatType = "Every " + thirdEdit.getText().toString() + " month";
                            }
                            else repeatType = "Every 1 month";
                            repeatText.setText(repeatType);


                            monthOption1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    monthOption1.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));
                                    monthOption2.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                                    weeklyDays.clear();
                                    weeklyDays.add(String.valueOf(startDate.getDayOfMonth()));

                                }
                            });

                            monthOption2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    monthOption2.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));
                                    monthOption1.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                                    weeklyDays.clear();
                                    weeklyDays.add(getNthOccurrenceAndDay(startDate));
                                }
                            });

                        }
                    });
                    thirdEdit.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            repeatNone.setChecked(false);
                            repeatDaily.setChecked(false);
                            repeatWeekly.setChecked(false);
                            repeatMonthly.setChecked(true);
                            dayTitles.setVisibility(View.GONE);
                            monthOptions.setVisibility(View.VISIBLE);
                            LocalDate startDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                            monthOption1.setText("Every " + startDate.getDayOfMonth() + " of the month" );
                            monthOption2.setText("Every " + getNthOccurrenceAndDay(startDate) + " of the month");
                            if (!editable.toString().isEmpty()){
                                repeatType = "Every " + editable + " month";
                            }
                            else{
                                repeatType = "Every 1 month";
                            }
                            repeatText.setText(repeatType);

                        }
                    });
                    text3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            repeatNone.setChecked(false);
                            repeatDaily.setChecked(false);
                            repeatWeekly.setChecked(false);
                            repeatMonthly.setChecked(true);
                            repeatType = "Every 1 month";
                            repeatText.setText(repeatType);
                            dayTitles.setVisibility(View.GONE);
                            monthOptions.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        // notification section
        notiType = "";
        notificationSect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notiCancel.getVisibility() == View.VISIBLE) {
                    notiCancel.callOnClick();
                } else {
                    setVisible("noti");
                    if (notiType.isEmpty()){
                        notiNone.setChecked(true);
                        notiDay.setChecked(false);
                        notiDayBefore.setChecked(false);
                        notiType = "Don't notify";
                    }
                    else {
                        if (notiType.equals("On the day itself")){
                            notiNone.setChecked(false);
                            notiDay.setChecked(true);
                            notiDayBefore.setChecked(false);
                        }
                        else if(notiType.contains("day before")){
                            notiNone.setChecked(false);
                            notiDay.setChecked(false);
                            notiDayBefore.setChecked(true);
                        }
                        else {
                            notiNone.setChecked(true);
                            notiDay.setChecked(false);
                            notiDayBefore.setChecked(false);
                        }
                    }

                    notiNone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            notiNone.setChecked(true);
                            notiDay.setChecked(false);
                            notiDayBefore.setChecked(false);
                            notiType = "Don't notify";
                            notiText.setText(notiType);
                        }
                    });

                    notiDay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            notiDay.setChecked(true);
                            notiNone.setChecked(false);
                            notiDayBefore.setChecked(false);
                            notiType = "On the day itself";
                            notiText.setText(notiType);
                        }
                    });

                    notiDayBefore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            notiDayBefore.setChecked(true);
                            notiDay.setChecked(false);
                            notiNone.setChecked(false);
                            if (!fourthEdit.getText().toString().isEmpty()){
                                notiType = fourthEdit.getText().toString() + " day before";
                            }
                            else {
                                notiType = "1 day before";
                            }
                            notiText.setText(notiType);
                        }
                    });
                    fourthEdit.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            notiDayBefore.setChecked(true);
                            notiDay.setChecked(false);
                            notiNone.setChecked(false);
                            if (!editable.toString().isEmpty()){
                                notiType = editable.toString() + " day before";
                            }
                            else {
                                notiType = "1 day before";
                            }
                            notiText.setText(notiType);
                        }
                    });
                    text4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            notiDayBefore.setChecked(true);
                            notiDay.setChecked(false);
                            notiNone.setChecked(false);
                            notiType = "1 day before";
                            notiText.setText(notiType);
                        }
                    });
                }
            }
        });

        save.setOnClickListener(v -> {
            Log.i("save schedule", "save clicked");
            if (scheduleName.getText().toString().isEmpty()){
                Toast.makeText(CreateSchedule.this, "Fill in schedule name", Toast.LENGTH_SHORT).show();
            }
            else{
                Log.i("save schedule", "schedule name filled");
                if (!tank.getFeedSchedule().isEmpty()){
                    Log.i("save schedule", "Schedule List not empty");
                    ArrayList<String> names = new ArrayList<>();
                    for (FeedSchedule sched : tank.getFeedSchedule()){
                        names.add(sched.getScheduleName());
                    }
                    if (names.contains(scheduleName.getText().toString())){
                        Toast.makeText(CreateSchedule.this, "Schedule name already used", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String name = scheduleName.getText().toString().trim();
                        // Food
                        if (gAdapter.getSelectedFoods() != null && !gAdapter.getSelectedFoods().isEmpty()){
                            greenList = gAdapter.getSelectedFoods();
                        }
                        else greenList = null;
                        if (bAdapter.getSelectedFoods() != null && !bAdapter.getSelectedFoods().isEmpty()){
                            brownList = bAdapter.getSelectedFoods();
                        }
                        else brownList = null;

                        // Repeat
                        if (repeatType.isEmpty()){
                            repeatType = "Don't repeat";
                            weeklyDaysList = new ArrayList<>();
                        }
                        else{
                            weeklyDaysList = new ArrayList<>(weeklyDays);
                        }

                        repeatDetails.put(repeatType, weeklyDaysList);

                        // Water
                        if (waterAmt.getText().toString().isEmpty()){
                            water = 0;
                        }
                        else water = Integer.parseInt(waterAmt.getText().toString());

                        // Notification
                        if (notiType.isEmpty()){
                            notiType = "Don't notify";
                        }
                        ArrayList<String> dates = new ArrayList<>();
                        FeedSchedule schedule = new FeedSchedule(name, greenList, brownList, repeatType,  repeatDetails, notiType, water, null, dates);
                        for (LocalDate d : generateScheduledDates(schedule)){
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            dates.add(formatter.format(d));
                        }
                        schedule.setDates(dates);
                        Log.i("In save schedule", "schedule: " + schedule.getScheduleName() + schedule.getWaterAmt() + schedule.getNotification());
                        Log.i("In save schedule", "schedule: " + schedule.getGreenFood() + schedule.getBrownFood() + schedule.getRepeatType() + schedule.getRepeatDetails());
                        ArrayList<FeedSchedule> feedSchedules = tank.getFeedSchedule();
                        feedSchedules.add(schedule);
                        tank.setFeedSchedule(feedSchedules);
                        saveToFirebase(tank);
                    }
                }
                else {
                    Log.i("save schedule", "schedule List empty");
                    String name = scheduleName.getText().toString().trim();
                    // Food
                    if (gAdapter.getSelectedFoods() != null || !gAdapter.getSelectedFoods().isEmpty()){
                        greenList = gAdapter.getSelectedFoods();
                    }
                    else greenList = null;
                    if (bAdapter.getSelectedFoods() != null || !bAdapter.getSelectedFoods().isEmpty()){
                        brownList = bAdapter.getSelectedFoods();
                    }
                    else brownList = null;

                    // Repeat
                    if (repeatType.isEmpty()){
                        repeatType = "Don't repeat";
                        weeklyDaysList = new ArrayList<>();
                    }
                    else{
                        weeklyDaysList = new ArrayList<>(weeklyDays);
                    }
                    repeatDetails.put(repeatType, weeklyDaysList);

                    // Water
                    if (waterAmt.getText().toString().isEmpty()){
                        water = 0;
                    }
                    else water = Integer.parseInt(waterAmt.getText().toString());

                    // Notification
                    if (notiType.isEmpty()){
                        notiType = "Don't notify";
                    }
                    ArrayList<String> dates = new ArrayList<>();
                    FeedSchedule schedule = new FeedSchedule(name, greenList, brownList, repeatType,  repeatDetails, notiType, water, null, dates);
                    for (LocalDate d : generateScheduledDates(schedule)){
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        dates.add(formatter.format(d));
                    }
                    schedule.setDates(dates);
                    ArrayList<FeedSchedule> feedSchedules = tank.getFeedSchedule();
                    feedSchedules.add(schedule);
                    tank.setFeedSchedule(feedSchedules);
                    saveToFirebase(tank);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    public void setVisible(String section){
        switch (section) {
            case "green":
                green.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                greenCancel.setVisibility(View.VISIBLE);
                greenLine.setVisibility(View.VISIBLE);
                greenRecycler.setVisibility(View.VISIBLE);
                green.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_add_24, 0, 0, 0);
                if (brownCancel.getVisibility() == View.VISIBLE){
                    brownCancel.callOnClick();
                }
                if (repeatCancel.getVisibility() == View.VISIBLE){
                    repeatCancel.callOnClick();
                }
                if (notiCancel.getVisibility() == View.VISIBLE){
                    notiCancel.callOnClick();
                }
                scheduleName.clearFocus();
                waterAmt.clearFocus();
                break;
            case "brown":
                brown.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                brownCancel.setVisibility(View.VISIBLE);
                brownLine.setVisibility(View.VISIBLE);
                brownRecycler.setVisibility(View.VISIBLE);
                brown.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_add_24, 0, 0, 0);
                if (greenCancel.getVisibility() == View.VISIBLE){
                    greenCancel.callOnClick();
                }
                if (repeatCancel.getVisibility() == View.VISIBLE){
                    repeatCancel.callOnClick();
                }
                if (notiCancel.getVisibility() == View.VISIBLE){
                    notiCancel.callOnClick();
                }
                scheduleName.clearFocus();
                waterAmt.clearFocus();
                break;
            case "repeat":
                repeatText.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                repeatCancel.setVisibility(View.VISIBLE);
                repeatLine.setVisibility(View.VISIBLE);
                repeatOptions.setVisibility(View.VISIBLE);
                if (greenCancel.getVisibility() == View.VISIBLE){
                    greenCancel.callOnClick();
                }
                if (brownCancel.getVisibility() == View.VISIBLE){
                    brownCancel.callOnClick();
                }
                if (notiCancel.getVisibility() == View.VISIBLE){
                    notiCancel.callOnClick();
                }
                scheduleName.clearFocus();
                waterAmt.clearFocus();
                break;
            case "noti":
                notiText.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                notiCancel.setVisibility(View.VISIBLE);
                notiLine.setVisibility(View.VISIBLE);
                notiOptions.setVisibility(View.VISIBLE);
                if (greenCancel.getVisibility() == View.VISIBLE){
                    greenCancel.callOnClick();
                }
                if (brownCancel.getVisibility() == View.VISIBLE){
                    brownCancel.callOnClick();
                }
                if (repeatCancel.getVisibility() == View.VISIBLE){
                    repeatCancel.callOnClick();
                }
                scheduleName.clearFocus();
                waterAmt.clearFocus();
                break;
        }
    }

    public void setGone(String section) {
        switch (section) {
            case "green":
                if (gAdapter.getSelectedFoods() != null && !gAdapter.getSelectedFoods().isEmpty()) {
                    for (int i = 0; i < gAdapter.getSelectedFoods().size(); i++) {
                        Log.i("setGone", "greenList: " + greenList.get(0).getName());
                        if (!gAdapter.getSelectedFoods().get(i).getName().isEmpty()) {
                            greenList = gAdapter.getSelectedFoods();
                            Log.i("setGone", "greenList: " + greenList.get(0).getName());
                            green.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                            green.setText("Green: " + greenList.size() + " items selected");
                        }
                    }
                }

                else {
                    green.setText("Green");
                    green.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));
                }
                greenCancel.setVisibility(View.GONE);
                greenLine.setVisibility(View.GONE);
                greenRecycler.setVisibility(View.GONE);
                green.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.outline_nest_eco_leaf_24, 0, 0, 0);
                break;
            case "brown":
                if (bAdapter.getSelectedFoods() != null && !bAdapter.getSelectedFoods().isEmpty()) {
                    for (int i = 0; i < bAdapter.getSelectedFoods().size(); i++) {
                        Log.i("setGone", "brownList: " + brownList.get(0).getName());
                        if (!bAdapter.getSelectedFoods().get(i).getName().isEmpty()) {
                            brownList = bAdapter.getSelectedFoods();
                            Log.i("setGone", "brownList: " + brownList.get(0).getName());
                            brown.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                            brown.setText("Green: " + brownList.size() + " items selected");
                        }
                    }
                }

                else {
                    brown.setText("Green");
                    brown.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));
                }
                brownCancel.setVisibility(View.GONE);
                brownLine.setVisibility(View.GONE);
                brownRecycler.setVisibility(View.GONE);
                brown.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.outline_nest_eco_leaf_24, 0, 0, 0);
                break;
            case "repeat":
                if (!repeatType.isEmpty()){
                    repeatText.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                    repeatText.setText(repeatType);
                }
                else repeatText.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));
                repeatCancel.setVisibility(View.GONE);
                repeatLine.setVisibility(View.GONE);
                repeatOptions.setVisibility(View.GONE);
                break;
            case "noti":
                if (!notiType.isEmpty()){
                    notiText.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.textColour));
                    notiText.setText(notiType);
                }
                else notiText.setTextColor(ContextCompat.getColor(CreateSchedule.this, R.color.android_hint));
                notiCancel.setVisibility(View.GONE);
                notiLine.setVisibility(View.GONE);
                notiOptions.setVisibility(View.GONE);
                break;
        }
    }
    public static String getNthOccurrenceAndDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayOfMonth = date.getDayOfMonth();

        // Find the first occurrence of the specified day in the month
        LocalDate firstOccurrence = date.with(TemporalAdjusters.firstInMonth(dayOfWeek));
        int count = 1;

        // Iterate through the month, counting occurrences of the day of the week
        LocalDate current = firstOccurrence;
        while (current.isBefore(date)) {
            current = current.plusWeeks(1);
            count++;
        }

        String dayOfWeekName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
        String formattedDayOfWeek = dayOfWeekName.substring(0, 1).toUpperCase() + dayOfWeekName.substring(1).toLowerCase();

        return String.format("%d%s %s", count, getOrdinalSuffix(count), formattedDayOfWeek);
    }

    private static String getOrdinalSuffix(int value) {
        if (value % 100 >= 11 && value % 100 <= 13) {
            return "th";
        }
        switch (value % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    public void saveToFirebase(Tank updatedTank){
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        for (int i = 0; i < user.getTanks().size(); i++) {
            Tank tank = user.getTanks().get(i);
            if (tank.getTankID() == updatedTank.getTankID()) {
                user.getTanks().set(i, updatedTank);
                user.setTanks(user.getTanks());
                break;
            }
        }

        Log.i(null, "after set tank");
        reference.child(user.getUsername()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("FirebaseUpdate in Create Schedule", "Feed Schedule: "+ user.getTanks().get(0).getFeedSchedule().size());
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("schedule_saved", true);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                    }
                });

    }
    public ArrayList<LocalDate> generateScheduledDates(FeedSchedule feedSchedule) {
        // individual
        ArrayList<LocalDate> scheduledDates = new ArrayList<>();
        LocalDate refDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        LocalDate nextMonth = LocalDate.now().plusMonths(3); // continues generating 3 months worth of dates
        String repeatType = feedSchedule.getRepeatType();
        HashMap<String, ArrayList<String>> repeatDetails = feedSchedule.getRepeatDetails();


        if (repeatType.equals("Don't repeat")) {

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
}
