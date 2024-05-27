package sg.edu.np.mad.greencycle.FeedingLog;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.Tank;
import sg.edu.np.mad.greencycle.LiveData.TankSelection;
import sg.edu.np.mad.greencycle.R;

public class Feeding extends AppCompatActivity {
    User user;
    Tank tank;
    FirebaseDatabase database;
    DatabaseReference reference;
    RecyclerView logRecycler, greenRecycler, brownRecycler;
    public TextView noLogText, backButton, date, editNote, waterAmt,calendar, addGreen, addBrown ;
    ArrayList<String> green,brown, greenFood, brownFood;
    public ArrayList<sg.edu.np.mad.greencycle.FeedingLog.Log> feedingLog;
    ArrayList<Tank> tankList;
    FloatingActionButton add;
    LogAdapter mAdapter;
    FoodAdapter gAdapter, bAdapter;
    int targetTankId, water;
    String dateFed, notes;
    public Button confirm;
    public BottomSheetDialog addLog;
    sg.edu.np.mad.greencycle.FeedingLog.Log log;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.feeding_log);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.feedingRecyclerView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");
        targetTankId = tank.getTankID();
        Log.i(null, "Tank ID: " + targetTankId);
        tankList = user.getTanks();

        logRecycler = findViewById(R.id.log);
        noLogText = findViewById(R.id.noLogText);
        backButton = findViewById(R.id.backButton);
        add = findViewById(R.id.addLog);
        refreshLogRecyclerView();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Feeding.this, TankSelection.class);
                intent.putExtra("user", user);
                intent.putExtra("where", "Feeding");
                startActivity(intent);
                finish();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLog = new BottomSheetDialog(Feeding.this);
                addLog.setContentView(R.layout.add_feeding_bottom);
                addLog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                addLog.setCancelable(true);
                addLog.setDismissWithAnimation(true);

                date = addLog.findViewById(R.id.date);
                calendar = addLog.findViewById(R.id.calendar);
                greenRecycler = addLog.findViewById(R.id.greenRecycler);
                brownRecycler = addLog.findViewById(R.id.brownRecycler);
                waterAmt = addLog.findViewById(R.id.editWater);
                editNote = addLog.findViewById(R.id.notesDescription);
                addGreen = addLog.findViewById(R.id.addGreen);
                addBrown = addLog.findViewById(R.id.addBrown);
                confirm = addLog.findViewById(R.id.confirm);

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String today = formatter.format(new Date());
                // set todays date first
                date.setText(today);
                dateFed = date.getText().toString().trim();

                // date selection
                calendar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH);
                        DatePickerDialog datePickerDialog = new DatePickerDialog(Feeding.this, R.style.CustomDatePickerDialog, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                String day1 = String.valueOf(dayOfMonth);
                                String month1 = String.valueOf(monthOfYear + 1);
                                if (day1.length() != 2) {
                                    day1 = "0" + day1;
                                }
                                if (month1.length() != 2) {
                                    month1 = "0" + month1;
                                }
                                date.setText(day1 + "/" + month1 + "/" + year); // sets the text to the chosen date
                            }
                        },
                                year, month, day);
                        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis()); // max date will be day of creation, selection of days after will be restricted
                        datePickerDialog.show();
                    }

                });
                // add notes
                editNote.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        editNote.setMinLines(3);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                // green and brown foods
                addGreen.setOnClickListener(v -> gAdapter.addItem());
                addBrown.setOnClickListener(v -> bAdapter.addItem());

                log = new sg.edu.np.mad.greencycle.FeedingLog.Log(feedingLog.size(), targetTankId, dateFed, new ArrayList<>(), new ArrayList<>(), notes, water);
                Log.i(null, "Log id: " + log.getLogId() + log.getWaterAmt());
                gAdapter = new FoodAdapter(greenFood, greenRecycler, "green", user, log, Feeding.this);
                greenRecycler.setLayoutManager(new LinearLayoutManager(Feeding.this));
                greenRecycler.setAdapter(gAdapter);

                bAdapter = new FoodAdapter(brownFood, brownRecycler, "brown", user, log, Feeding.this);
                brownRecycler.setLayoutManager(new LinearLayoutManager(Feeding.this));
                brownRecycler.setAdapter(bAdapter);
                if (!log.getGreens().isEmpty()){
                    Log.i(null, "GreenLog: " + log.getGreens().get(0));
                }
                // to add the log
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        handleConfirmClick();
                        addLog.dismiss();
                    }
                });
                addLog.show();
            }
        });
    }
    public void refreshLogRecyclerView() {
        Log.i(null, "in refresh log");
        reference.child(user.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // retrieve user data from Firebase
                    User updatedUser = snapshot.getValue(User.class);
                    if (updatedUser != null && updatedUser.getTanks() != null) {
                        user.setTanks(updatedUser.getTanks());
                        for (Tank tank : user.getTanks()) {
                            if (tank.getTankID() == targetTankId) {
                                if (tank.getFeedingLog()!=null){
                                    feedingLog = tank.getFeedingLog();
                                }
                                else {
                                    feedingLog = new ArrayList<sg.edu.np.mad.greencycle.FeedingLog.Log>();
                                }
                            }
                        }
                    } else {
                        feedingLog = new ArrayList<sg.edu.np.mad.greencycle.FeedingLog.Log>();
                    }
                } else {
                    feedingLog = new ArrayList<sg.edu.np.mad.greencycle.FeedingLog.Log>();
                }
                updateRecyclerView(feedingLog);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Log.e("Firebase", "Failed to read user data.", error.toException());
            }
        });
        Log.i(null, "after firebase");
        greenFood = new ArrayList<>();
        brownFood = new ArrayList<>();
        for (Tank tank : user.getTanks()){
            if (tank.getFeedingLog() != null){
                for (sg.edu.np.mad.greencycle.FeedingLog.Log log : tank.getFeedingLog()){
                    if (log.getGreens() != null){
                        greenFood.addAll(log.getGreens());
                    }
                    if (log.getBrowns() != null) {
                        brownFood.addAll(log.getBrowns());
                    }
                }
            }
        }
        if (greenFood != null){
            for (String s : greenFood){
                int index = greenFood.indexOf(s);
                greenFood.set(index, s.replaceAll("[^a-zA-Z ]", "").trim());

            }
        }
        if (brownFood != null){
            for (String s : brownFood){
                int index = brownFood.indexOf(s);
                brownFood.set(index, s.replaceAll("[^a-zA-Z ]", "").trim());
            }
        }
        // get foods from all feeding log
        Set<String> greenSet = new HashSet<>(greenFood);
        greenFood.clear();
        greenFood.addAll(greenSet);

        Set<String> brownSet = new HashSet<>(brownFood);
        brownFood.clear();
        brownFood.addAll(brownSet);

    }
    private void updateRecyclerView(ArrayList<sg.edu.np.mad.greencycle.FeedingLog.Log> list) {
        if (list.isEmpty()) {
            noLogText.setVisibility(View.VISIBLE);
        } else {
            Log.i(null, "not empty");
            noLogText.setVisibility(View.INVISIBLE);
        }
        mAdapter = new LogAdapter(feedingLog, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        logRecycler.setLayoutManager(linearLayoutManager);
        logRecycler.setAdapter(mAdapter);
    }
    private void handleConfirmClick() {
        Log.i(null, "in confirm Click");
        ArrayList<String> selectedGreens = gAdapter.getSelectedFoods();
        ArrayList<String> selectedBrowns = bAdapter.getSelectedFoods();

        if (selectedGreens != null){
            log.getGreens().addAll(selectedGreens);
        }
        if (selectedBrowns != null){
            log.getBrowns().addAll(selectedBrowns);
        }
        if (!waterAmt.getText().toString().isEmpty()){
             water = Integer.parseInt(waterAmt.getText().toString());
        } else water = 0;

        String date = this.date.getText().toString();
        String notes = editNote.getText().toString();
        Log.i(null, "after get texts");
        log.setLogDate(date);
        log.setWaterAmt(water);
        log.setNotes(notes);

        feedingLog.add(log);
        Log.i(null, "after add log");
        // Assuming User and DatabaseReference setup is done
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        for (Tank tank : user.getTanks()) {
            if (log.getTankId() == tank.getTankID()) {
                Log.i(null, "feedingLog size: " + feedingLog.size());
                tank.setFeedingLog(feedingLog);
                user.getTanks().set(log.getTankId(), tank);
                user.setTanks(user.getTanks());
                break;
            }
        }
        Log.i(null, "after set tank");
        reference.child(user.getUsername()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(null, "Check list" + user.getTanks().get(0).getFeedingLog().size());
                        // Dismiss the dialog or close the activity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                    }
                });
        refreshLogRecyclerView();
    }
}