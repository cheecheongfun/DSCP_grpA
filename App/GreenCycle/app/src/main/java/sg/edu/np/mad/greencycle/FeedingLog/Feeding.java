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
import sg.edu.np.mad.greencycle.Fragments.MainActivity;
import sg.edu.np.mad.greencycle.LiveData.Tank;
import sg.edu.np.mad.greencycle.LiveData.TankAdapter;
import sg.edu.np.mad.greencycle.LiveData.TankSelection;
import sg.edu.np.mad.greencycle.R;

public class Feeding extends AppCompatActivity {
    User user;
    Tank tank;
    FirebaseDatabase database;
    DatabaseReference reference;
    RecyclerView logRecycler, greenRecycler, brownRecycler;
    TextView noLogText, backButton, date, editNote, waterAmt,calendar;
    ArrayList<String> green,brown, greenFood, brownFood;
    ArrayList<sg.edu.np.mad.greencycle.FeedingLog.Log> feedingLog;
    ArrayList<Tank> tankList;
    FloatingActionButton add;
    LogAdapter mAdapter;
    FoodAdapter gAdapter, bAdapter;
    int targetTankId;
    String dateFed;
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
                BottomSheetDialog addLog = new BottomSheetDialog(Feeding.this);
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
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String today = formatter.format(new Date());
                date.setText(today);

                // date selection or put todays date
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
                                dateFed = date.getText().toString(); // setting the dueDate variable for the task object
                            }
                        },
                                year, month, day);
                        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis()); // minimum date will be day of creation, selection of days before will be restricted
                        datePickerDialog.show();
                    }

                });



                // water amount
                String water = waterAmt.getText().toString().trim();

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
                gAdapter = new FoodAdapter(greenFood, greenRecycler);
                greenRecycler.setLayoutManager(new LinearLayoutManager(Feeding.this));
                greenRecycler.setAdapter(gAdapter);

                bAdapter = new FoodAdapter(brownFood, brownRecycler);
                brownRecycler.setLayoutManager(new LinearLayoutManager(Feeding.this));
                brownRecycler.setAdapter(bAdapter);
                addLog.show();
                // show bottom view
//                green = new ArrayList<>();
//                green.add("1 leaf");
//                green.add("5 coffee ground");
//                brown = new ArrayList<>();
//                brown.add("2 cardboard");
//                brown.add("2 paper");
//                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//                String today = formatter.format(new Date());
//                sg.edu.np.mad.greencycle.FeedingLog.Log log = new sg.edu.np.mad.greencycle.FeedingLog.Log(feedingLog.size(), targetTankId, today, green, brown, null);
//                Log.i(null, "Log: " + log.getLogDate());
//                feedingLog.add(log);
//                for (Tank tank : user.getTanks()){
//                    if (targetTankId == tank.getTankID()){
//                        tank.setFeedingLog(feedingLog);
//                        tankList.set(targetTankId,tank);
//                        user.setTanks(tankList);
//                        break;
//                    }
//                }
//                reference.child(user.getUsername()).setValue(user)
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                // update success
//                                Log.i("FirebaseUpdate", "User tank list updated. Log ID: " + tank.getFeedingLog().size());
//                                Log.i(null, "Check list" + user.getTanks().get(0).getFeedingLog());
//                                refreshLogRecyclerView();
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                // update failed
//                                Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
//                            }
//                        });
            }
        });
    }
    public void refreshLogRecyclerView() {
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
        greenFood = new ArrayList<>();
        brownFood = new ArrayList<>();
        for (Tank tank : user.getTanks()){
            if (tank.getFeedingLog() != null){
                for (sg.edu.np.mad.greencycle.FeedingLog.Log log : tank.getFeedingLog()){
                    greenFood.addAll(log.getGreens());
                    brownFood.addAll(log.getBrowns());
                    break;
                }
            }
        }
        if (greenFood != null){
            for (String s : greenFood){
                int index = greenFood.indexOf(s);
                greenFood.set(index, s.replaceAll("[^a-zA-Z]", ""));
            }
        }
        if (brownFood != null){
            for (String s : brownFood){
                int index = brownFood.indexOf(s);
                brownFood.set(index, s.replaceAll("[^a-zA-Z]", ""));
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
}