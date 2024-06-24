package sg.edu.np.mad.greencycle.FeedingLog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TimingLogger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
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

public class CreateSchedule extends DialogFragment {

    EditText scheduleName, waterAmt, secondEdit, thirdEdit, fourthEdit;
    RelativeLayout greenSect, brownSect, waterSect, repeatSect, notificationSect, second, third, fourth, monthOptions;
    LinearLayout dayTitles, repeatOptions, notiOptions;
    TextView green, greenCancel, greenLine, brown, brownCancel, brownLine, repeatText, repeatCancel;
    TextView repeatLine, notiText, notiCancel, notiLine, cancel, save, monthOption1, monthOption2;
    RecyclerView greenRecycler, brownRecycler;
    RadioButton repeatNone, repeatDaily, repeatWeekly, repeatMonthly, notiNone, notiDay, notiDayBefore;
    FoodAdapter gAdapter, bAdapter;
    private static final String ARG_GREEN = "arg_green_food_list";
    private static final String ARG_BROWN = "arg_brown_food_list";
    private static final String ARG_TANK = "arg_tank";
    private static final String ARG_USER = "user";
    private static final String ARG_DATE = "date";
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


    public static CreateSchedule newInstance(User user, Tank tank, ArrayList<String> greenFood, ArrayList<String> brownFood, String date) {
        CreateSchedule fragment = new CreateSchedule();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
        args.putParcelable(ARG_TANK, tank);
        args.putStringArrayList(ARG_GREEN, greenFood);
        args.putStringArrayList(ARG_BROWN, brownFood);
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.schedule_create, container, false);

        // General
        scheduleName = view.findViewById(R.id.scheduleName);
        save = view.findViewById(R.id.scheduleSave);
        cancel = view.findViewById(R.id.scheduleCancel);

        scheduleName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(null, "schedule name on click");
            }
        });
        // Green section
        greenSect = view.findViewById(R.id.greenSect);
        green = view.findViewById(R.id.green);
        greenLine = view.findViewById(R.id.greenLine);
        greenCancel = view.findViewById(R.id.greenCancel);
        greenRecycler = view.findViewById(R.id.greenRecycler);

        // Brown section
        brownSect = view.findViewById(R.id.brownSect);
        brown = view.findViewById(R.id.brown);
        brownLine = view.findViewById(R.id.brownLine);
        brownCancel = view.findViewById(R.id.brownCancel);
        brownRecycler = view.findViewById(R.id.brownRecycler);

        // Water section
        waterSect = view.findViewById(R.id.waterSect);
        waterAmt = view.findViewById(R.id.waterAmt);

        // Repeat section
        repeatSect = view.findViewById(R.id.repeat);
        repeatText = view.findViewById(R.id.repeatText);
        repeatLine = view.findViewById(R.id.repeatLine);
        repeatCancel = view.findViewById(R.id.repeatCancel);
        // Radio group for options
        repeatOptions = view.findViewById(R.id.radioRepeatGroup);
        repeatNone = view.findViewById(R.id.radioRepeatNone);
        repeatDaily = view.findViewById(R.id.radioRepeatDaily);
        second = view.findViewById(R.id.second);
        repeatWeekly = view.findViewById(R.id.radioRepeatWeekly);
        secondEdit = view.findViewById(R.id.secondEdit);
        dayTitles = view.findViewById(R.id.dayTitles);
        third = view.findViewById(R.id.third);
        repeatMonthly = view.findViewById(R.id.radioRepeatMonthly);
        thirdEdit = view.findViewById(R.id.thirdEdit);
        monthOptions = view.findViewById(R.id.monthOptions);
        monthOption1 = view.findViewById(R.id.option1);
        monthOption2 = view.findViewById(R.id.option2);

        // Notification section
        notificationSect = view.findViewById(R.id.notificationSect);
        notiText = view.findViewById(R.id.notiText);
        notiCancel = view.findViewById(R.id.notiCancel);
        notiLine = view.findViewById(R.id.notiLine);
        // Radio group for options
        notiOptions = view.findViewById(R.id.radioNotiGroup);
        notiNone = notiOptions.findViewById(R.id.radioNotiNone);
        notiDay = notiOptions.findViewById(R.id.radioNotiDay);
        fourth = notiOptions.findViewById(R.id.fourth);
        notiDayBefore = fourth.findViewById(R.id.radioNotiDayBefore);
        fourthEdit = fourth.findViewById(R.id.fourthEdit);

        greenList = new ArrayList<>();
        brownList = new ArrayList<>();

        // day of week titles
        dayAbbreviations = new String[]{"M", "T", "W", "T", "F", "S", "S"};
        // green section
        greenSect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (greenCancel.getVisibility() == View.VISIBLE){
                    greenCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setGone("green");
                            green.setClickable(false);
                        }
                    });
                }
                else {
                    setVisible("green");
                    brownCancel.callOnClick();
                    notiCancel.callOnClick();
                    repeatCancel.callOnClick();
                    Log.i("greenVisible", "greenFood List: " + greenFood.size());
                    gAdapter = new FoodAdapter(greenFood, greenRecycler, "green", greenList);
                    greenRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
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
                    brownCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setGone("brown");
                            brown.setClickable(false);
                        }
                    });
                }
                else {
                    setVisible("brown");
                    greenCancel.callOnClick();
                    notiCancel.callOnClick();
                    repeatCancel.callOnClick();
                    bAdapter = new FoodAdapter(brownFood, brownRecycler, "brown", brownList);
                    brownRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
                    brownRecycler.setAdapter(bAdapter);
                    brown.setClickable(true);
                    brown.setOnClickListener(v-> bAdapter.addItem());
                }
            }
        });
        // repeat section

        repeatType = "";
        repeatSect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeatCancel.getVisibility() == View.VISIBLE) {
                    repeatCancel.setOnClickListener(nev -> setGone("repeat"));
                } else {
                    setVisible("repeat");
                    greenCancel.callOnClick();
                    notiCancel.callOnClick();
                    brownCancel.callOnClick();
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
                            repeatText.setTextColor(Color.parseColor("#FFFFFFFF"));
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
                            repeatText.setTextColor(Color.parseColor("#FFFFFFFF"));
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
                                textView.setTextColor(Color.BLACK);

                                DayOfWeek dayOfWeek = DayOfWeek.of(index + 1);
                                if (weeklyDays.contains(dayOfWeek.toString())) {
                                    textView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.icon_brown_light));
                                    textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.icon_brown_light));
                                }

                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String dayString = dayOfWeek.toString();

                                        if (weeklyDays.contains(dayString)) {
                                            weeklyDays.remove(dayString);
                                            textView.setBackgroundColor(Color.TRANSPARENT);
                                            textView.setTextColor(Color.BLACK);
                                        } else {
                                            weeklyDays.add(dayString);
                                            textView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.icon_brown_light));
                                            textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.icon_brown_light));
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
                            repeatText.setTextColor(Color.parseColor("#FFFFFFFF"));
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
                            repeatText.setTextColor(Color.parseColor("#FFFFFFFF"));
                            for (int i = 0; i < dayTitles.getChildCount(); i++) {
                                final int index = i;
                                TextView textView = (TextView) dayTitles.getChildAt(i);
                                String abbreviation = dayAbbreviations[index];
                                textView.setText(abbreviation);
                                textView.setBackgroundColor(Color.TRANSPARENT);
                                textView.setTextColor(Color.BLACK);

                                DayOfWeek dayOfWeek = DayOfWeek.of(index + 1);
                                if (weeklyDays.contains(dayOfWeek.toString())) {
                                    textView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.icon_brown_light));
                                    textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.icon_brown_light));
                                }

                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String dayString = dayOfWeek.toString();

                                        if (weeklyDays.contains(dayString)) {
                                            weeklyDays.remove(dayString);
                                            textView.setBackgroundColor(Color.TRANSPARENT);
                                            textView.setTextColor(Color.BLACK);
                                        } else {
                                            weeklyDays.add(dayString);
                                            textView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.icon_brown_light));
                                            textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.icon_brown_light));
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
                            repeatText.setTextColor(Color.parseColor("#FFFFFFFF"));


                            monthOption1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    monthOption1.setTextColor(Color.parseColor("#FFFFFFFF"));
                                    monthOption2.setTextColor(ContextCompat.getColor(getActivity(), R.color.android_hint));
                                    weeklyDays.clear();
                                    weeklyDays.add(String.valueOf(startDate.getDayOfMonth()));

                                }
                            });

                            monthOption2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    monthOption2.setTextColor(Color.parseColor("#FFFFFFFF"));
                                    monthOption1.setTextColor(ContextCompat.getColor(getActivity(), R.color.android_hint));
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
                            repeatText.setTextColor(Color.parseColor("#FFFFFFFF"));

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
                    notiCancel.setOnClickListener(nev -> setGone("noti"));
                } else {
                    setVisible("noti");
                    greenCancel.callOnClick();
                    brownCancel.callOnClick();
                    repeatCancel.callOnClick();
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
                            notiText.setTextColor(Color.parseColor("#FFFFFFFF"));
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
                            notiText.setTextColor(Color.parseColor("#FFFFFFFF"));
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
                            notiText.setTextColor(Color.parseColor("#FFFFFFFF"));
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
                            notiText.setTextColor(Color.parseColor("#FFFFFFFF"));
                        }
                    });
                }
            }
        });

        save.setOnClickListener(v -> {
            Log.i("save schedule", "save clicked");
            if (scheduleName.getText().toString().isEmpty()){
                Toast.makeText(getActivity(), "Fill in schedule name", Toast.LENGTH_SHORT).show();
            }
            else{
                if (tank.getFeedSchedule() != null){
                    for (FeedSchedule sched : tank.getFeedSchedule()){
                        if (sched.getScheduleName().equals(scheduleName.getText().toString())){
                            Toast.makeText(getActivity(), "Schedule name already exists", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String name = scheduleName.getText().toString().trim();
                            // Food
                            if (gAdapter.getSelectedFoods() != null || !gAdapter.getSelectedFoods().isEmpty()){
                                greenList = gAdapter.getSelectedFoods();
                            }
                            else greenList = new ArrayList<>();
                            if (bAdapter.getSelectedFoods() != null || !bAdapter.getSelectedFoods().isEmpty()){
                                brownList = bAdapter.getSelectedFoods();
                            }
                            else brownList = new ArrayList<>();

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
                            FeedSchedule schedule = new FeedSchedule(name, greenList, brownList, repeatType,  repeatDetails, notiType, water, date);
                            Log.i("In save schedule", "schedule: " + schedule.getScheduleName() + schedule.getWaterAmt() + schedule.getNotification());
                            Log.i("In save schedule", "schedule: " + schedule.getGreenFood() + schedule.getBrownFood() + schedule.getRepeatType() + schedule.getRepeatDetails());
                            ArrayList<FeedSchedule> feedSchedules = new ArrayList<>();
                            feedSchedules.add(schedule);
                            tank.setFeedSchedule(feedSchedules);
                            saveToFirebase(tank);
                            Bundle result = new Bundle();
                            result.putString("scheduleName", name);
                            getParentFragmentManager().setFragmentResult("requestKey", result);
                            dismiss();
                            Toast.makeText(getActivity(), schedule.getScheduleName() + " saved", Toast.LENGTH_SHORT).show();
                            break;
                        }

                    }
                }
                else {
                    String name = scheduleName.getText().toString().trim();
                    // Food
                    if (gAdapter.getSelectedFoods() != null || !gAdapter.getSelectedFoods().isEmpty()){
                        greenList = gAdapter.getSelectedFoods();
                    }
                    else greenList = new ArrayList<>();
                    if (bAdapter.getSelectedFoods() != null || !bAdapter.getSelectedFoods().isEmpty()){
                        brownList = bAdapter.getSelectedFoods();
                    }
                    else brownList = new ArrayList<>();

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
                    FeedSchedule schedule = new FeedSchedule(name, greenList, brownList, repeatType,  repeatDetails, notiType, water, date);
                    Log.i("In save schedule", "schedule: " + schedule.getScheduleName() + schedule.getWaterAmt() + schedule.getNotification());
                    Log.i("In save schedule", "schedule: " + schedule.getGreenFood() + schedule.getBrownFood() + schedule.getRepeatType() + schedule.getRepeatDetails());
                    ArrayList<FeedSchedule> feedSchedules = new ArrayList<>();
                    feedSchedules.add(schedule);
                    tank.setFeedSchedule(feedSchedules);
                    saveToFirebase(tank);
                    Bundle result = new Bundle();
                    result.putString("scheduleName", name);
                    getParentFragmentManager().setFragmentResult("requestKey", result);
                    dismiss();
                    Toast.makeText(getActivity(), schedule.getScheduleName() + " saved", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancel.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(null, "In CreateSchedule");
        user = getArguments().getParcelable(ARG_USER);
        tank = getArguments().getParcelable(ARG_TANK);
        greenFood = getArguments().getStringArrayList(ARG_GREEN);
        brownFood = getArguments().getStringArrayList(ARG_BROWN);
        date = getArguments().getString(ARG_DATE);
    }

    public void setVisible(String section){
        switch (section) {
            case "green":
                green.setTextColor(Color.parseColor("#FFFFFFFF"));
                greenCancel.setVisibility(View.VISIBLE);
                greenLine.setVisibility(View.VISIBLE);
                greenRecycler.setVisibility(View.VISIBLE);
                green.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_add_24, 0, 0, 0);
                break;
            case "brown":
                brown.setTextColor(Color.parseColor("#FFFFFFFF"));
                brownCancel.setVisibility(View.VISIBLE);
                brownLine.setVisibility(View.VISIBLE);
                brownRecycler.setVisibility(View.VISIBLE);
                brown.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_add_24, 0, 0, 0);
                break;
            case "repeat":
                repeatText.setTextColor(Color.parseColor("#FFFFFFFF"));
                repeatCancel.setVisibility(View.VISIBLE);
                repeatLine.setVisibility(View.VISIBLE);
                repeatOptions.setVisibility(View.VISIBLE);
                break;
            case "noti":
                notiText.setTextColor(Color.parseColor("#FFFFFFFF"));
                notiCancel.setVisibility(View.VISIBLE);
                notiLine.setVisibility(View.VISIBLE);
                notiOptions.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void setGone(String section) {
        switch (section) {
            case "green":
                if (gAdapter.getSelectedFoods() != null && !gAdapter.getSelectedFoods().isEmpty()){
                    for (int i = 0; i < gAdapter.getSelectedFoods().size(); i++){
                        if (!gAdapter.getSelectedFoods().get(i).getName().isEmpty()){
                            greenList = gAdapter.getSelectedFoods();
                            Log.i("setGone", "greenList: " + greenList.get(0).getName());
                            green.setTextColor(Color.parseColor("#FFFFFFFF"));
                            green.setText("Green: " + greenList.size() + " items selected");
                        }
                    }
                }
                else {
                    green.setText("Green");
                    green.setTextColor(Color.parseColor("#DFD9C5"));
                }
                greenCancel.setVisibility(View.GONE);
                greenLine.setVisibility(View.GONE);
                greenRecycler.setVisibility(View.GONE);
                green.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.outline_nest_eco_leaf_24, 0, 0, 0);
                break;
            case "brown":
                if (bAdapter.getSelectedFoods() != null && !bAdapter.getSelectedFoods().isEmpty()){
                    brownList = bAdapter.getSelectedFoods();
                    brown.setTextColor(Color.parseColor("#FFFFFFFF"));
                    brown.setText("Brown: " + brownList.size() + " items selected");
                }
                else brown.setTextColor(Color.parseColor("#DFD9C5"));
                brownCancel.setVisibility(View.GONE);
                brownLine.setVisibility(View.GONE);
                brownRecycler.setVisibility(View.GONE);
                brown.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.outline_nest_eco_leaf_24, 0, 0, 0);
                break;
            case "repeat":
                if (!repeatType.isEmpty()){
                    repeatText.setTextColor(Color.parseColor("#FFFFFFFF"));
                    repeatText.setText(repeatType);
                }
                else repeatText.setTextColor(Color.parseColor("#DFD9C5"));
                repeatCancel.setVisibility(View.GONE);
                repeatLine.setVisibility(View.GONE);
                repeatOptions.setVisibility(View.GONE);
                break;
            case "noti":
                if (!notiType.isEmpty()){
                    notiText.setTextColor(Color.parseColor("#FFFFFFFF"));
                    notiText.setText(notiType);
                }
                else notiText.setTextColor(Color.parseColor("#DFD9C5"));
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
                        Log.i("FirebaseUpdate", "Feed Schedule: "+ user.getTanks().get(0).getFeedSchedule().size());
                        // Dismiss the dialog or close the activity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                    }
                });
    }

}

