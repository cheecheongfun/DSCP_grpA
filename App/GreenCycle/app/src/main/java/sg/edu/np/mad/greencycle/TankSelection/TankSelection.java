package sg.edu.np.mad.greencycle.TankSelection;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import sg.edu.np.mad.greencycle.Classes.HashUtils;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.StartUp.LoginPage;
import sg.edu.np.mad.greencycle.StartUp.MainActivity;
import sg.edu.np.mad.greencycle.R;
// Fionn, S1040073K
public class TankSelection extends AppCompatActivity {

    private TextView noTankText, tankNotFound;
    RecyclerView tankRecycler;
    ArrayList<Double> npk;
    ArrayList<Tank> tankList;
    User user;
    FirebaseDatabase database;
    DatabaseReference reference, referenceTank;
    SearchView searchView;
    TankAdapter mAdapter;
    FloatingActionButton add;
    String purpose, today;
    Button confirm;
    Dialog searchTank, tankDialog;

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

        referenceTank = database.getReference("Tanks").child("data");

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        purpose = receivingEnd.getExtras().getString("where");

        tankRecycler = findViewById(R.id.tankList);
        noTankText = findViewById(R.id.noTankText);
        tankNotFound = findViewById(R.id.notFound);
        add = findViewById(R.id.addTank);

        refreshTankRecyclerView(null);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        today = formatter.format(new Date());
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
        int width = (int) (TankSelection.this.getResources().getDisplayMetrics().widthPixels * 0.8);
        searchTank = new Dialog(TankSelection.this, R.style.CustomDialog);
        searchTank.setContentView(R.layout.search_tank);
        searchTank.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        searchTank.setCancelable(true);
        confirm = searchTank.findViewById(R.id.submitCodeButton);

        tankDialog = new Dialog(TankSelection.this, R.style.CustomDialog);
        tankDialog.setContentView(R.layout.tank_dialog);
        tankDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        tankDialog.setCancelable(true);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTank.show();
                EditText code1 = searchTank.findViewById(R.id.codeBox4);
                EditText code2 = searchTank.findViewById(R.id.codeBox5);
                EditText code3 = searchTank.findViewById(R.id.codeBox6);

                code1.setText("");
                code1.requestFocus();
                code2.setText("");
                code3.setText("");

                // Array of EditText boxes
                final EditText[] boxes = {code1, code2, code3};

                // Set TextChangedListener for all boxes except the last one
                for (int i = 0; i < boxes.length; i++) {
                    final int currentIndex = i;
                    boxes[currentIndex].addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            // If character is deleted (count > after), handle backspace focus
                            if (count > after && start == 0) {
                                if (currentIndex > 0) {
                                    // Move focus to the previous box
                                    boxes[currentIndex - 1].requestFocus();
                                }
                            }
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            // If character is added (count > before), handle forward focus
                            if (count > before && s.length() == 1) {
                                if (currentIndex < boxes.length - 1) {
                                    // Move focus to the next box
                                    boxes[currentIndex + 1].requestFocus();
                                }
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });
                }

                confirm.setText("Find");
                confirm.setEnabled(true);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (code1.getText().toString().isEmpty() || code2.getText().toString().isEmpty() || code3.getText().toString().isEmpty()){
                            Toast.makeText(TankSelection.this, "Input device ID", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            confirm.setText("Finding...");
                            confirm.setEnabled(false);
                            String deviceId = "NDS" + code1.getText().toString() + code2.getText().toString() + code3.getText().toString();
                            ArrayList<String> deviceIDs = new ArrayList<>();
                            if (!user.getTanks().isEmpty() && user.getTanks() != null){
                                for (Tank tank : user.getTanks()){
                                    deviceIDs.add(tank.getDeviceID());
                                }
                            }
                            if (deviceIDs.contains(deviceId)){
                                Toast.makeText(TankSelection.this, "Tank already registered", Toast.LENGTH_SHORT).show();
                                searchTank.dismiss();
                            }
                            else {
                                referenceTank.child(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            List<String> timestampStrings = new ArrayList<>();
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                String timestampString = snapshot.getKey();
                                                if (timestampString != null) {
                                                    timestampStrings.add(timestampString);
                                                }
                                            }
                                            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                                            LocalDateTime latestTimestamp = null;
                                            if (!timestampStrings.isEmpty()){
                                                for (String timestampString : timestampStrings) {
                                                    try {
                                                        LocalDateTime timestamp = LocalDateTime.parse(timestampString, formatter);
                                                        if (latestTimestamp == null || timestamp.isAfter(latestTimestamp)) {
                                                            latestTimestamp = timestamp;
                                                        }
                                                    } catch (Exception e) {
                                                        System.err.println("Error parsing timestamp: " + timestampString);
                                                        e.printStackTrace();
                                                    }
                                                }
                                                String queryTime = latestTimestamp.toString();
                                                retrieveLatestTankData(deviceId, queryTime);
                                            }
                                        } else {
                                            code1.setText("");
                                            code2.setText("");
                                            code3.setText("");
                                            confirm.setText("Find");
                                            confirm.setEnabled(true);
                                            code1.requestFocus();
                                            Toast.makeText(TankSelection.this, "Device ID not found", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e("FirebaseError", "Error: " + databaseError.getMessage());
                                    }
                                });
                            }
                        }

                    }
                });
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
    public void refreshTankRecyclerView(String text) {
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
        Log.i(null, "In filter List");
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

        Log.i("TankSelection", "tanklist size : " + list.size() + " from " + purpose);
        mAdapter = new TankAdapter(list, this, user, purpose);
        tankRecycler.setLayoutManager(new LinearLayoutManager(this));
        tankRecycler.setAdapter(mAdapter);
    }
    private void retrieveLatestTankData(String deviceId, String timestamp) {
        Log.e("retrieve", "in retrieve: " + deviceId);
        referenceTank.child(deviceId).child(timestamp).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Map the data to the Tank class
                    Tank tank = mapDataSnapshotToTank(dataSnapshot, deviceId);
                    // open a new dialog
                    confirm.setText("Search");
                    tankDialog.show();
                    TextView deviceText = tankDialog.findViewById(R.id.deviceText);
                    deviceText.setText(deviceId);
                    EditText name = tankDialog.findViewById(R.id.etName);
                    EditText worm = tankDialog.findViewById(R.id.etWorm);

                    name.setText("");
                    name.requestFocus();
                    worm.setText("");

                    Button add = tankDialog.findViewById(R.id.add);

                    add.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.e("add", "Worm: " + worm.getText().toString() + " Name: "+ name.getText().toString());
                            String tankName = name.getText().toString();
                            if (tankName.isEmpty()){
                                tank.setTankName(deviceId);
                                name.setText(deviceId);
                            } else tank.setTankName(tankName);

                            String wormNo = worm.getText().toString();
                            if (wormNo.isEmpty()) {
                                tank.setNumberOfWorms(0);
                                worm.setText("0");
                            } else tank.setNumberOfWorms(Integer.parseInt(wormNo));
                            addTankToUserAccount(tank);
                            refreshTankRecyclerView(null);
                        }
                    });
                } else {
                    Toast.makeText(TankSelection.this, "No data available for the latest timestamp", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error: " + databaseError.getMessage());
            }
        });
    }
    private Tank mapDataSnapshotToTank(DataSnapshot dataSnapshot, String deviceID) {

        npk = new ArrayList<>();
        npk.add(dataSnapshot.child("Soil - Nitrogen").getValue(Double.class));
        npk.add(dataSnapshot.child("Soil - Phosphorus").getValue(Double.class));
        npk.add(dataSnapshot.child("Soil - Potassium").getValue(Double.class));

        Log.e("mapData", "NPK: " + npk.get(0) + " " + npk.get(1) + " " + npk.get(2));
        double pHLevel = dataSnapshot.child("Soil - PH").getValue(Double.class);
        double temperature = dataSnapshot.child("Soil - Temperature").getValue(Double.class);
        double moisture = dataSnapshot.child("Soil - Moisture").getValue(Double.class);
        double EC = dataSnapshot.child("Soil - EC").getValue(Double.class);
        int size;
        if (user.getTanks()!=null){
            size = user.getTanks().size();
        } else size = 0;
        return new Tank(size, deviceID, "New Tank", null, 0, npk, temperature, EC, pHLevel, moisture, today,  null, null);
    }
    private void addTankToUserAccount(Tank tank) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUsername());
        ArrayList<Tank> userTanks = user.getTanks();

        if (user.getTanks() != null){
            boolean tankExists = false;
            for (Tank userTank : userTanks) {
                if (userTank.getTankID() == (tank.getTankID())) {
                    tankExists = true;
                    break;
                }
            }
            userTanks.add(tank);
            user.setTanks(userTanks);
            userReference.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(TankSelection.this, tank.getTankName() + " successfully registered", Toast.LENGTH_SHORT).show();
                    searchTank.dismiss();
                    tankDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("FirebaseError", "Error: " + e.getMessage());
                }
            });
        }
        else {
            userTanks = new ArrayList<>();
            userTanks.add(tank);
            user.setTanks(userTanks);
            userReference.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(TankSelection.this, tank.getTankName() + " successfully registered", Toast.LENGTH_SHORT).show();
                    searchTank.dismiss();
                    tankDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("FirebaseError", "Error: " + e.getMessage());
                }
            });
        }
    }
}

