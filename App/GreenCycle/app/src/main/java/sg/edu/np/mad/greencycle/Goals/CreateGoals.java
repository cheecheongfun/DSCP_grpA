package sg.edu.np.mad.greencycle.Goals;
//Lee Jun Rong S10242663

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.Tank;
import sg.edu.np.mad.greencycle.R;

public class CreateGoals extends AppCompatActivity {

    private ImageButton backButton;
    private Spinner goalSpinner,dayspinner,monthspinner,yearspinner;
    private TextView setNumberLabel,selectEndDateLabel,selectGoalLabel;
    private EditText goalNumber;
    private Button saveGoalButton;
    private DatabaseReference goalsRef;
    User user;
    Tank tank;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_goals);

        // Initialize views
        selectGoalLabel = findViewById(R.id.select_goal_label);
        goalSpinner = findViewById(R.id.goal_spinner);
        setNumberLabel = findViewById(R.id.set_number_label);
        goalNumber = findViewById(R.id.goal_number);
        selectEndDateLabel = findViewById(R.id.select_end_date_label);
        dayspinner = findViewById(R.id.day_spinner);
        monthspinner = findViewById(R.id.month_spinner);
        yearspinner = findViewById(R.id.year_spinner);
        saveGoalButton = findViewById(R.id.save_goal_button);

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");

        // Get the current date
        Calendar today = Calendar.getInstance();

        String tyear = String.valueOf(today.get(Calendar.YEAR));
        int tmonthsnnum = today.get(Calendar.MONTH) + 1;
        // Get the full month name for the current month
        String tmonths = new DateFormatSymbols().getMonths()[today.get(Calendar.MONTH)];
        String tday = String.valueOf(today.get(Calendar.DAY_OF_MONTH));


        // Set Goal spinner items
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.goal_options, R.layout.spinner_items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goalSpinner.setAdapter(adapter);

        //Set Year Spinner items
        List<String> yearOptionsList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        for (int year = currentYear; year <= currentYear + 10; year++) {
            yearOptionsList.add(String.valueOf(year));
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, R.layout.spinner_items, yearOptionsList);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearspinner.setAdapter(yearAdapter);
        setDefaultSpinnerValue(tyear,yearOptionsList,yearspinner);

        //Set Up Month Spinner items

        String[] monthOptions = new DateFormatSymbols().getMonths();
        List<String> monthOptionsList = Arrays.asList(monthOptions);
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, R.layout.spinner_items, monthOptionsList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthspinner.setAdapter(monthAdapter);
        setDefaultSpinnerValue(tmonths,monthOptionsList,monthspinner);



        //Set Up Days Spinner items
        List<String> dayOptionsList = new ArrayList<>();
        for (int day = 1; day <= 31; day++) {
            dayOptionsList.add(String.valueOf(day));
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, R.layout.spinner_items, dayOptionsList);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayspinner.setAdapter(dayAdapter);
        setDefaultSpinnerValue(tday,dayOptionsList,dayspinner);

        // Set onClickListener for save button
        saveGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGoal();
            }
        });
    }

    private void saveGoal() {
        String selectedGoal = goalSpinner.getSelectedItem().toString();
        String goalNumberStr = goalNumber.getText().toString();
        int day = Integer.parseInt(dayspinner.getSelectedItem().toString());
        int month = monthspinner.getSelectedItemPosition() + 1; // Month is zero-based in Calendar
        int year = Integer.parseInt(yearspinner.getSelectedItem().toString());

        // Validate the goal number input
        if (goalNumberStr.isEmpty()) {
            goalNumber.setError("Goal number is required");
            goalNumber.requestFocus();
            return;
        }

        int goalNumberInt;
        try {
            goalNumberInt = Integer.parseInt(goalNumberStr);
        } catch (NumberFormatException e) {
            goalNumber.setError("Please enter a valid number");
            goalNumber.requestFocus();
            return;
        }


        // Create a calendar object for the selected date
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day);

        // Get the current date
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 23);
        today.set(Calendar.MINUTE, 59);
        today.set(Calendar.SECOND, 59);
        today.set(Calendar.MILLISECOND, 59);

        String tyear = String.valueOf(today.get(Calendar.YEAR));
        int tmonthsnnum = today.get(Calendar.MONTH) + 1;
        String tmonths = String.valueOf(tmonthsnnum);
        String tday = String.valueOf(today.get(Calendar.DAY_OF_MONTH));

        String todaydate = tday + "/" + tmonths + "/" + tyear;


        // Validate that the selected date is after today's date
        if (!selectedDate.after(today)) {
            Toast.makeText(this, "End date must be after today's date", Toast.LENGTH_SHORT).show();
            return;
        }

        String sday;
        String smonth;

        // Create a date string in dd/MM/yyyy format
        // month+1 since datepicker is zero-based
        if (day < 10){
            sday = "0" + String.valueOf(day);}
        else {
            sday = String.valueOf(day);
        }

        if (month < 10){
            smonth = "0" + String.valueOf(month);}
        else {
            smonth = String.valueOf(month);
        }
        String endDate = sday + "/" + smonth  + "/" + year;

        System.out.println("Selected Goal: " + selectedGoal);
        System.out.println("Goal Number: " + goalNumberInt);
        System.out.println("End Date: " + endDate);
        System.out.println("Today Date: " + todaydate);

        goalsRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUsername()).child("tanks").child(String.valueOf(tank.getTankID())).child("goals");
        goalsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Goals> goalsList = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Assuming your goal data structure in Firebase matches your Goals class
                        Goals goal = snapshot.getValue(Goals.class);
                        goalsList.add(goal);
                    }

                    // Now you have all goals in the goalsList
                    int highestGoalId = 0;

                    // Find the highest goalid
                    for (Goals goal : goalsList) {
                        if (goal.getGoalid() > highestGoalId) {
                            highestGoalId = goal.getGoalid();
                        }
                    }

                    // Increment the highest goalid by 1 for the next goal
                    int nextGoalId = highestGoalId + 1;

                    // Now you can use the nextGoalId for the new goal
                    Goals newGoal = new Goals(nextGoalId,goalNumberInt,"Incomplete",selectedGoal,todaydate,endDate);

                    // Save newGoal to Firebase
                    goalsRef.child(String.valueOf(nextGoalId)).setValue(newGoal);

                    // Save the new goal to Firebase or perform further operations
                    // firebaseSave(newGoal);
                } else {
                    // Handle case where there are no existing goals in Firebase
                    // For example, set the nextGoalId to 1 if there are no existing goals
                    int nextGoalId = 1;
                    Goals newGoal = new Goals(nextGoalId,goalNumberInt,"Incomplete",selectedGoal,todaydate,endDate);
                    // Save newGoal to Firebase
                    goalsRef.child(String.valueOf(nextGoalId)).setValue(newGoal);

                    // Save the new goal to Firebase or perform further operations
                    // firebaseSave(newGoal);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors here
            }

        });

        Toast.makeText(this, "Goal saved successfully!", Toast.LENGTH_SHORT).show();
        finish();

    }

    // Method to set the default value in the Spinner
    private void setDefaultSpinnerValue(String value,List list,Spinner spinner) {
        // Find the position of the defaultYear in the yearOptionsList
        int defaultPosition = list.indexOf(value);
        // Set the Spinner selection to the defaultPosition
        if (defaultPosition != -1) {
            spinner.setSelection(defaultPosition);
        }
    }

}
