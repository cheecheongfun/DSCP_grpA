package sg.edu.np.mad.greencycle.StartUp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.NoSuchAlgorithmException;

import sg.edu.np.mad.greencycle.Classes.HashUtils;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.Classes.User;


public class RegistrationPage extends AppCompatActivity {

    EditText registerusername, registerpassword;
    Button successfulregister;
    TextView availabilityStatus;
    FirebaseDatabase database;
    DatabaseReference reference;
    CardView frameOne, frameTwo, frameThree;
    boolean passChar8 = false, passUpper = false, passNum = false, isRegistrationClickable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.registration_page);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        registerusername = findViewById(R.id.registerusername);
        registerpassword = findViewById(R.id.registerpassword);
        successfulregister = findViewById(R.id.registerbutton);
        availabilityStatus = findViewById(R.id.availabilityStatus);

        frameOne = findViewById(R.id.frameOne);
        frameTwo = findViewById(R.id.frameTwo);
        frameThree = findViewById(R.id.frameThree);

        Intent receivingEnd = getIntent();
        String email = receivingEnd.getExtras().getString("email");


        // Text watcher to check username as user types
        registerusername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validateData();
            }
        });

        registerpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validateData();
            }
        });

        successfulregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = registerusername.getText().toString();
                final String password = registerpassword.getText().toString();

                if (!isValidUsername(username)) {
                    return;
                }

                // Proceed to registration if username is valid and available
                registerNewUser(username, password, email);
            }
        });
    }

    private void validateUsername(String username) {
        availabilityStatus.setVisibility(View.VISIBLE);
        if (username.length() < 5) {
            availabilityStatus.setText("Username must be at least 5 characters long");
        } else if (username.length() > 12) {
            availabilityStatus.setText("Username must be no more than 12 characters long");
        } else if (!username.matches("[a-zA-Z0-9]+")) {
            availabilityStatus.setText("Username must contain only letters and numbers");
        } else {
            checkUsernameAvailability(username);
        }
    }

    private boolean isValidUsername(String username) {
        return username.matches("[a-zA-Z0-9]{5,12}");
    }

    private void checkUsernameAvailability(String username) {
        reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                availabilityStatus.setVisibility(View.VISIBLE);
                if (dataSnapshot.exists()) {
                    availabilityStatus.setText("Username already taken");
                } else {
                    availabilityStatus.setText("Username available");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                availabilityStatus.setText("Error checking username");
            }
        });
    }

    private void registerNewUser(String username, String password, String email) {
        try {
            String salt = HashUtils.getSalt();
            String hashedPassword = HashUtils.hashPassword(password, salt);

            User newUser = new User(username, hashedPassword, username, email, null, null, null, salt);
            reference.child(username).setValue(newUser)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegistrationPage.this, "Successful Sign Up!", Toast.LENGTH_SHORT).show();

                                // Save user info in SharedPreferences
                                saveUserToSharedPreferences(newUser);

                                // Proceed to main activity
                                Intent intent = new Intent(RegistrationPage.this, MainActivity.class);
                                intent.putExtra("user", newUser);
                                intent.putExtra("tab", "home_tab");
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RegistrationPage.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (NoSuchAlgorithmException e) {
            Toast.makeText(RegistrationPage.this, "Error generating salt", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserToSharedPreferences(User user) {
        SharedPreferences sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(user.getUsername() + "_DisplayName", user.getDisplayname());
        editor.apply();
    }

    @SuppressLint("ResourceType")
    private void validateData() {
        String password = registerpassword.getText().toString();
        String name = registerusername.getText().toString();
        // Check for empty name and password fields and display error messages if empty
        validateUsername(name);

        // Check if password contains at least 8 characters and less than 20
        if (password.length() >= 8 && password.length() <= 20) {
            passChar8 = true;
            frameOne.setCardBackgroundColor(Color.parseColor(getString(R.color.mid_green))); // Set frameOne background to accent color
        } else {
            passChar8 = false;
            frameOne.setCardBackgroundColor(Color.parseColor(getString(R.color.light_grey))); // Set frameOne background to default color
        }

        // Check if password contains at least one uppercase letter and one lowercase letter
        if (password.matches("(?=.*[A-Z])(?=.*[a-z]).+")) {
            passUpper = true;
            frameTwo.setCardBackgroundColor(Color.parseColor(getString(R.color.mid_green))); // Set frameTwo background to accent color
        } else {
            passUpper = false;
            frameTwo.setCardBackgroundColor(Color.parseColor(getString(R.color.light_grey))); // Set frameTwo background to default color
        }

        // Check if password contains at least one numeric digit
        if (password.matches("(.*[0-9].*)")) {
            passNum = true;
            frameThree.setCardBackgroundColor(Color.parseColor(getString(R.color.mid_green))); // Set frameThree background to accent color
        } else {
            passNum = false;
            frameThree.setCardBackgroundColor(Color.parseColor(getString(R.color.light_grey))); // Set frameThree background to default color
        }

        // Check all password requirements and update UI
        if (passChar8 && passUpper && passNum) {
            // If all password requirements are met, enable save button and change background color
            isRegistrationClickable = true;
            successfulregister.setBackgroundColor(Color.parseColor(getString(R.color.light_green))); // Set background color to light green
        } else {
            // If any password requirement is not met, disable registration button and change background color
            isRegistrationClickable = false;
            successfulregister.setBackgroundColor(Color.parseColor(getString(R.color.light_grey))); // Set default background color
        }
    }


}
