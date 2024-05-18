package sg.edu.np.mad.greencycle.StartUp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.User;

public class RegistrationPage extends AppCompatActivity {

    EditText registerusername;
    EditText registerpassword;
    Button successfulregister;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration_page);
        Intent receivingEnd = getIntent();

        registerusername = findViewById(R.id.registerusername);
        registerpassword = findViewById(R.id.Registerpassword);
        successfulregister = findViewById(R.id.registerbutton);

        successfulregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                final String username = registerusername.getText().toString();
                final String password = registerpassword.getText().toString();

                // Check if the username already exists
                reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Username already exists
                            Toast.makeText(RegistrationPage.this, "Username already exists!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Username does not exist, create new user
                            User newuser = new User(username, password); // Assuming User class handles password securely
                            reference.child(username).setValue(newuser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegistrationPage.this, "Successful Sign Up!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegistrationPage.this,LoginPage.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(RegistrationPage.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(RegistrationPage.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}


//public class RegistrationPage extends AppCompatActivity {
//
//    EditText registerusername;
//    EditText registerpassword;
//
//    Button successfulregister;
//
//    FirebaseDatabase database;
//
//    DatabaseReference reference;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_registration_page);
//        Intent receivingEnd = getIntent();
//
//        registerusername = findViewById(R.id.registerusername);
//        registerpassword = findViewById(R.id.Registerpassword);
//        successfulregister = findViewById(R.id.registerbutton);
//
//        successfulregister.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                database = FirebaseDatabase.getInstance();
//                reference = database.getReference("users");
//
//                String username = registerusername.getText().toString();
//                String password = registerpassword.getText().toString();
//                User newuser = new User(username,password);
//                reference.child(username).setValue(newuser);
//                Toast.makeText(RegistrationPage.this,"Successful Sign Up!",Toast.LENGTH_SHORT).show();
//            }
//        });
//
//
//
//    }
//}