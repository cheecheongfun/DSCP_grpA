package sg.edu.np.mad.greencycle.FeedingLog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    RecyclerView logRecycler;
    TextView noLogText, backButton;
    ArrayList<String> green,brown;
    ArrayList<sg.edu.np.mad.greencycle.FeedingLog.Log> feedingLog;
    ArrayList<Tank> tankList;
    FloatingActionButton add;
    LogAdapter mAdapter;
    int targetTankId;

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
                green = new ArrayList<>();
                brown = new ArrayList<>();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String today = formatter.format(new Date());
                sg.edu.np.mad.greencycle.FeedingLog.Log log = new sg.edu.np.mad.greencycle.FeedingLog.Log(feedingLog.size(), targetTankId, today, green, brown, null);
                Log.i(null, "Log: " + log.getLogDate());
                feedingLog.add(log);
                for (Tank tank : user.getTanks()){
                    if (targetTankId == tank.getTankID()){
                        tank.setFeedingLog(feedingLog);
                        tankList.set(targetTankId,tank);
                        user.setTanks(tankList);
                        break;
                    }
                }
                reference.child(user.getUsername()).setValue(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // update success
                                Log.i("FirebaseUpdate", "User tank list updated. Log ID: " + tank.getFeedingLog().size());
                                Log.i(null, "Check list" + user.getTanks().get(0).getFeedingLog());
                                refreshLogRecyclerView();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // update failed
                                Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                            }
                        });
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
                Log.i(null, "size" + String.valueOf(feedingLog.isEmpty()));
                updateRecyclerView(feedingLog);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Log.e("Firebase", "Failed to read user data.", error.toException());
            }
        });
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