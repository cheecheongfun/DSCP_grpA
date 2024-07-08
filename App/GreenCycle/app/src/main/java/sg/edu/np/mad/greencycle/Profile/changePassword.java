package sg.edu.np.mad.greencycle.Profile;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import sg.edu.np.mad.greencycle.Classes.HashUtils;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.StartUp.LoginPage;

public class changePassword extends AppCompatActivity {

    private EditText etUsername, etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnSavePassword,back;
    private DatabaseReference usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSavePassword = findViewById(R.id.submitNewPasswordButton);
        back = findViewById(R.id.backToLoginFromChangeButton);

        back.setText("Back");


        btnSavePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void changePassword() {

        Intent intent = getIntent();
        String current_username = intent.getStringExtra("username");
        String current_password = intent.getStringExtra("password");
        String salt = intent.getStringExtra("salt");

        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String hashedCurrentPassword = HashUtils.hashPassword(currentPassword, salt);
        Log.v("p",current_password);
        Log.v("p",salt);
        Log.v("p",hashedCurrentPassword);
        Log.v("p",newPassword);
        Log.v("p",confirmPassword);



        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Please enter current password");
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Please enter new password");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm new password");
            return;
        }

        if (!current_password.equals(hashedCurrentPassword)){
            etCurrentPassword.setError("Current Password does not match");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("New Passwords do not match");
            return;
        }

        // Update the password in Firebase
        usersRef.child(current_username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String newPassword = HashUtils.hashPassword(confirmPassword, salt);
                    dataSnapshot.getRef().child("password").setValue(newPassword);
                    Toast.makeText(changePassword.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(changePassword.this, "Username not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(changePassword.this, "Error updating password", Toast.LENGTH_SHORT).show();
            }
        });

    }
}

