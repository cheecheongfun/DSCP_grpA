package sg.edu.np.mad.greencycle.Goals;


import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.LiveData;
import sg.edu.np.mad.greencycle.LiveData.Tank;
import sg.edu.np.mad.greencycle.R;

public class ViewGoals extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GoalsAdapter adapter;
    private TextView goaltext, back;
    private ImageView backbutton,editbutton;
    private List<Goals> goalsList;
    User user;
    Tank tank;

    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_viewgoal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recycler_view);
        goaltext = findViewById(R.id.title);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        back = findViewById(R.id.backButton);
        editbutton = findViewById(R.id.create_button);

        database = FirebaseDatabase.getInstance();

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");
        Log.v("goals",user.getUsername());


        // Construct the database reference path based on tank ID and user
        reference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUsername())
                .child("tanks")
                .child(String.valueOf(tank.getTankID()))
                .child("goals");


        // Initialize the goals list
        goalsList = new ArrayList<>();

        //Set Goal Page Title
        goaltext.setText("Tank " + tank.getTankName() +" Goals" );

        // Set up the RecyclerView adapter with the fetched data
        adapter = new GoalsAdapter(goalsList, ViewGoals.this,user,tank);
        recyclerView.setAdapter(adapter);

        // Fetch data from Firebase Realtime Database
        fetchGoalsData();
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewGoals.this, LiveData.class);
                intent.putExtra("user", user);
                intent.putExtra("tank",tank);
                startActivity(intent);
                finish();
            }
        });

        editbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewGoals.this, CreateGoals.class);
                intent.putExtra("user", user);
                intent.putExtra("tank",tank);
                startActivity(intent);

            }
        });


    }

    private void fetchGoalsData() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goalsList.clear(); // Clear the existing list

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Extract data from the snapshot
                    String goalName = snapshot.child("goal_name").getValue(String.class);
                    String goalsCompletion = snapshot.child("goals_completion").getValue(String.class);
                    String createdDate = snapshot.child("create_date").getValue(String.class);
                    String endDate = snapshot.child("end_date").getValue(String.class);
                    int goalsId = snapshot.child("goalid").getValue(Integer.class);
                    int goalsNumber = snapshot.child("goals_number").getValue(Integer.class);

                    if (goalsCompletion.contains("Incomplete")) {


                        // Create a Goals object from Firebase data
                        Goals goal = new Goals(goalsId, goalsNumber, goalsCompletion, goalName, createdDate, endDate);
                        goalsList.add(goal);

                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}

