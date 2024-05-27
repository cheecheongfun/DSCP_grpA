package sg.edu.np.mad.greencycle.Fragments.Home;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.Goals.ViewGoals;
import sg.edu.np.mad.greencycle.LiveData.TankSelection;
import sg.edu.np.mad.greencycle.NPKvalue.npk_value;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.SolarForecast.Forecast;

public class HomeFragment extends Fragment {

    ImageButton npkButton, liveDataBtn, feedingLogBtn, analyticsBtn, goalsBtn, imageLogBtn, soilTypeBtn, solarForecastBtn;
    TextView username;




    public HomeFragment() {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EdgeToEdge.enable(getActivity());
        // Inflate the layout for this fragment
        View view;
        view =  inflater.inflate(R.layout.fragment_home, container, false);
        Log.i(null, "Home Fragment");

        // Receiving intent
        Intent receivingEnd = getActivity().getIntent();
        String tab = receivingEnd.getExtras().getString("tab");
        User user = receivingEnd.getParcelableExtra("user");

        Log.v("test",user.getUsername());
        updateGoalsCompletion(user);


        // Calling layout elements
        username = view.findViewById(R.id.userWelcome);
        liveDataBtn = view.findViewById(R.id.liveDataButton);
        feedingLogBtn = view.findViewById(R.id.feedingLogButton);
        analyticsBtn = view.findViewById(R.id.analyticsButton);
        goalsBtn = view.findViewById(R.id.goalsButton);
        imageLogBtn = view.findViewById(R.id.imageLog);
        soilTypeBtn = view.findViewById(R.id.soilTypeButton);
        solarForecastBtn = view.findViewById(R.id.solarForecast);
        soilTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), npk_value.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        username.setText("Welcome, " + user.getUsername());

        // buttons to different pages
        liveDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tankSelect = new Intent(getContext(), TankSelection.class);
                tankSelect.putExtra("user", user);
                tankSelect.putExtra("where", "LiveData");
                startActivity(tankSelect);
            }
        });

        feedingLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tankSelect = new Intent(getContext(), TankSelection.class);
                tankSelect.putExtra("user", user);
                tankSelect.putExtra("where", "Feeding");
                startActivity(tankSelect);
            }
        });

        analyticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tankSelect = new Intent(getContext(), TankSelection.class);
                tankSelect.putExtra("user", user);
                tankSelect.putExtra("where", "Analytics");
                startActivity(tankSelect);
            }
        });

        imageLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tankSelect = new Intent(getContext(), TankSelection.class);
                tankSelect.putExtra("user", user);
                tankSelect.putExtra("where", "Identify");
                startActivity(tankSelect);
            }
        });

        goalsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goals = new Intent(getContext(), ViewGoals.class);
                goals.putExtra("user", user);
                startActivity(goals);
            }
        });

        solarForecastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent forecast = new Intent(getContext(), Forecast.class);
                forecast.putExtra("user", user);
                startActivity(forecast);
            }
        });

        return view;
    }

    public void updateGoalsCompletion(User user) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference tanksRef = database.child("users").child(user.getUsername()).child("tanks");

        tanksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tankSnapshot : dataSnapshot.getChildren()) {
                    String tankId = tankSnapshot.getKey(); // Get tank ID
                    String tankName = tankSnapshot.child("tankName").getValue(String.class); // Get tank name
                    int numberOfWorms = tankSnapshot.child("numberOfWorms").getValue(Integer.class);

                    DatabaseReference tankRef = tanksRef.child(tankId);

                    for (DataSnapshot goalSnapshot : tankSnapshot.child("goals").getChildren()) {
                        DatabaseReference goalRef = tankRef.child("goals").child(goalSnapshot.getKey());

                        int goalsNumber = goalSnapshot.child("goals_number").getValue(Integer.class);
                        String goals_completion = goalSnapshot.child("goals_completion").getValue(String.class);
                        String goals_name = goalSnapshot.child("goal_name").getValue(String.class);

                        if (goals_name.contains("worm")) {

                            if (numberOfWorms >= goalsNumber && !goals_completion.equals("Complete")) {
                                // Update goals_completion, include tank ID and tank name
                                goalRef.child("goals_completion").setValue("Complete");
                                String text = "Tank " + tankName + "'s desired worms population of " + goalsNumber + " has been achieved!";
                                showCustomToast(text,1);

                            }
                        }

                        if (goals_name.contains("Compost")) {

                            //currently placeholder value
                            int compost = 0;

                            if (compost >= goalsNumber && !goals_completion.equals("Complete")) {
                                // Update goals_completion, include tank ID and tank name
                                goalRef.child("goals_completion").setValue("Complete");
                                String text = "Tank " + tankName + " has produced the desired amount of " + goalsNumber + " grams of Compost!";
                                showCustomToast(text,2);

                            }
                        }

                        if (goals_name.contains("waste")) {

                            //currently placeholder value
                            int waste = 0;

                            if (waste >= goalsNumber && !goals_completion.equals("Complete")) {
                                // Update goals_completion, include tank ID and tank name
                                goalRef.child("goals_completion").setValue("Complete");
                                String text = "Tank " + tankName + " have helped reduce the desired waste of " + goalsNumber + " grams!";
                                showCustomToast(text,3);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void showCustomToast(String text,int choice) {
        // Inflate custom layout for the toast
        LayoutInflater inflater = getLayoutInflater();
        View customToastView = inflater.inflate(R.layout.toast, null);

        // Set text for the custom toast
        TextView toastTextView = customToastView.findViewById(R.id.toast_text);
        ImageView toastImageView = customToastView.findViewById(R.id.toast_image);

        if (choice == 1){toastImageView.setImageResource(R.drawable.thumbs_up_worm);}
        else if (choice == 2){toastImageView.setImageResource(R.drawable.compost);}
        else{toastImageView.setImageResource(R.drawable.food_waste);}

        toastTextView.setText(text);

        // Create and show the custom toast
        Toast customToast = new Toast(getContext());
        customToast.setDuration(Toast.LENGTH_LONG);
        customToast.setView(customToastView);
        customToast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);
        customToast.show();
    }


}