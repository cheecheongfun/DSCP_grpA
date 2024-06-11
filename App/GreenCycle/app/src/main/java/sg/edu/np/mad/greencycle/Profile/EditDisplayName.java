package sg.edu.np.mad.greencycle.Profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class EditDisplayName extends AppCompatActivity {
    private User user;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_display_name);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        EditText editTextUsername = findViewById(R.id.editTextUsername);
        Button button = findViewById(R.id.button);

        findViewById(R.id.backButton).setOnClickListener(view -> finish());

        user = getIntent().getParcelableExtra("user");

        button.setOnClickListener(view -> {
            String newDisplayName = editTextUsername.getText().toString();
            if (!newDisplayName.isEmpty()) {
                updateDisplayName(newDisplayName);
            } else {
                Toast.makeText(EditDisplayName.this, "Display name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDisplayName(String displayName) {
        if (user != null && user.getUsername() != null) {
            mDatabase.child("users").child(user.getUsername()).child("displayname").setValue(displayName)
                    .addOnSuccessListener(aVoid -> {
                        // Save display name in Shared Preferences
                        SharedPreferences sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("DisplayName", displayName);
                        editor.apply();

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("newDisplayName", displayName);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditDisplayName.this, "Failed to update display name", Toast.LENGTH_SHORT).show());
        }
    }

}
