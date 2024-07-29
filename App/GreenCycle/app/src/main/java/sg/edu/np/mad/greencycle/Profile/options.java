package sg.edu.np.mad.greencycle.Profile;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.StartUp.ResetPassword;
import sg.edu.np.mad.greencycle.StartUp.LoginPage;

public class options extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);

        User user = getIntent().getParcelableExtra("user");
        Log.v("username",user.getUsername());
        String username = user.getUsername();
        String password = user.getPassword();
        String salt = user.getSalt();

        ImageButton back = findViewById(R.id.buttonBack);
        TextView changepassword = findViewById(R.id.buttonChangePassword);
        TextView changeemail = findViewById(R.id.buttonChangeEmail);
        TextView delete = findViewById(R.id.buttonDeleteAccount);
        TextView logout = findViewById(R.id.buttonLogout);

        back.setOnClickListener(v -> {
            finish();
        });

        changepassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, changePassword.class);
            intent.putExtra("username", username);
            intent.putExtra("password",password);
            intent.putExtra("salt",salt);
            startActivity(intent);
        });

        changeemail.setOnClickListener(v -> {
            Intent intent = new Intent(this, changeemail.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });

        delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm Delete");
            builder.setMessage("Are you sure you want to delete?");

            // Add the buttons
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked Yes button, perform delete operation here
                    String username = user.getUsername(); // Assuming user is your User class instance

                    // Firebase reference
                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(username);
                    // Remove the entire branch under this username
                    usersRef.removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Data successfully deleted
                                    Toast.makeText(getApplicationContext(), "User data deleted successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(options.this, LoginPage.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle any errors
                                    Toast.makeText(getApplicationContext(), "Failed to delete user data", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked No button, dismiss the dialog
                    dialog.dismiss();
                }
            });

            // Create and show the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        logout.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm Logout");
            builder.setMessage("Are you sure you want to Logout?");

            // Add the buttons
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked Yes button, perform Logout operation here
                    Intent intent = new Intent(options.this, LoginPage.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked No button, dismiss the dialog
                    dialog.dismiss();
                }
            });

            // Create and show the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });




    }
}

