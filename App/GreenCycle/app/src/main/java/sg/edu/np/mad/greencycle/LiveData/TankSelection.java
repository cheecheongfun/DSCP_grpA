package sg.edu.np.mad.greencycle.LiveData;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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
import sg.edu.np.mad.greencycle.R;

public class TankSelection extends AppCompatActivity {

    private TextView noTankText, tankNotFound;
    RecyclerView tankRecycler;
    ArrayList<Double> npk;
    ArrayList<Tank> tankList;
    User user;
    FirebaseDatabase database;
    DatabaseReference reference;
    SearchView searchView;
    TankAdapter mAdapter;
    FloatingActionButton add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tank_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tankRecyclerView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");

        tankRecycler = findViewById(R.id.tankList);
        noTankText = findViewById(R.id.noTankText);
        tankNotFound = findViewById(R.id.notFound);
        add = findViewById(R.id.addTank);

        refreshTaskRecyclerView(null);

        // Search Tank
        searchView = findViewById(R.id.searchTank);
        searchView.setQueryHint("Enter tank name");
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        // Add Tank
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                npk = new ArrayList<>();
                npk.add(180.0);
                npk.add(100.0);
                npk.add(100.0);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String today = formatter.format(new Date());
                Tank tank = new Tank(tankList.size(), "Test", "Testing", 30, npk, 30.2, 89.9, 7.0, today);
                tankList.add(tank);
                user.setTanks(tankList);
                reference.child(user.getUsername()).setValue(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // update success
                                Log.i("Firebase", "User tank list updated. Tank ID: " + tank.getTankID());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // update failed
                                Log.e("Firebase", "Failed to update user tank list.", e);
                            }
                        });
                refreshTaskRecyclerView(null);
            }
        });

        // back to home
        TextView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TankSelection.this, MainActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("tab", "home_tab");
                startActivity(intent);
            }
        });
    }

    public void refreshTaskRecyclerView(String text) {
        reference.child(user.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // retrieve user data from Firebase
                    User updatedUser = snapshot.getValue(User.class);
                    if (updatedUser != null && updatedUser.getTanks() != null) {
                        user.setTanks(updatedUser.getTanks());
                        tankList = updatedUser.getTanks();
                    } else {
                        tankList = new ArrayList<>();
                    }
                } else {
                    tankList = new ArrayList<>();
                }

                if (text != null && !text.isEmpty()) {
                    filterList(text);
                } else {
                    updateRecyclerView(tankList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Log.e("Firebase", "Failed to read user data.", error.toException());
            }
        });
    }

    private void filterList(String text) {
        ArrayList<Tank> filteredList = new ArrayList<>();
        for (Tank tank : tankList) {
            if (tank.getTankName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(tank);
            }
        }

        if (filteredList.isEmpty()) {
            tankNotFound.setVisibility(View.VISIBLE);
        } else {
            tankNotFound.setVisibility(View.INVISIBLE);
        }

        updateRecyclerView(filteredList);
    }

    private void updateRecyclerView(ArrayList<Tank> list) {
        if (list.isEmpty()) {
            noTankText.setVisibility(View.VISIBLE);
            tankNotFound.setVisibility(View.INVISIBLE);
        } else {
            noTankText.setVisibility(View.INVISIBLE);
            tankNotFound.setVisibility(View.INVISIBLE);
        }

        Log.i("TankSelection", "tanklist size : " + list.size());
        mAdapter = new TankAdapter(list, this, user);
        tankRecycler.setLayoutManager(new LinearLayoutManager(this));
        tankRecycler.setAdapter(mAdapter);
    }
}
