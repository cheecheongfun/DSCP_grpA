package sg.edu.np.mad.greencycle.Profile;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class EditUsername extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private FirebaseFirestore firestore;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_username);

        user = getIntent().getParcelableExtra("user");
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        firestore = FirebaseFirestore.getInstance();

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        Button doneButton = findViewById(R.id.button);
        doneButton.setOnClickListener(view -> {
            EditText usernameEditText = findViewById(R.id.editTextUsername);
            String newUsername = usernameEditText.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                checkUsernameAvailability(newUsername);
            } else {
                Toast.makeText(EditUsername.this, "Please enter a username.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUsernameAvailability(final String newUsername) {
        if (!newUsername.equals(user.getUsername())) {  // Check if the username is actually being changed
            databaseReference.orderByChild("username").equalTo(newUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(EditUsername.this, "Username is already taken.", Toast.LENGTH_SHORT).show();
                    } else {
                        updateUser(newUsername);
                        user.setUsername(newUsername);  // Update the username in the user object

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EditUsername.this, "Error checking username.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateUser(newUsername);  // Username hasn't changed, just update other details
        }
    }

    private void updateUser(String newUsername) {
        String oldUsername = user.getUsername(); // Store old username

        // Reference to the old user node
        DatabaseReference oldUserRef = databaseReference.child(oldUsername);

        // First, remove the old user node
        oldUserRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Successfully deleted the old user node, now create a new node with the updated username
                user.setUsername(newUsername);  // Update the username in the user object
                DatabaseReference newUserRef = databaseReference.child(newUsername);
                newUserRef.setValue(user)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditUsername.this, "User updated successfully in Realtime Database.", Toast.LENGTH_SHORT).show();
                            // Duplicate user in Firestore
                            duplicateFirestoreUser(oldUsername, newUsername);
                        })
                        .addOnFailureListener(e -> Toast.makeText(EditUsername.this, "Failed to create new user node in Realtime Database.", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(EditUsername.this, "Failed to delete old user node in Realtime Database.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void duplicateFirestoreUser(String oldUsername, String newUsername) {
        Log.d("EditUsername", "Old username: " + oldUsername + ", New username: " + newUsername);

        // Retrieve the old document
        firestore.collection("Users").document(oldUsername).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get data from the old document
                        Map<String, Object> userData = documentSnapshot.getData();

                        if (userData != null) {
                            Log.d("EditUsername", "Old user data: " + userData.toString());
                            // Set the new document with the new username using the retrieved data
                            firestore.collection("Users").document(newUsername).set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // If successful, delete the old document
                                        firestore.collection("Users").document(oldUsername).delete()
                                                .addOnSuccessListener(aVoidDelete -> {
                                                    Log.d("EditUsername", "Old Firestore document deleted successfully");
                                                    Toast.makeText(EditUsername.this, "User updated successfully in Firestore.", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(eDelete -> {
                                                    Log.e("EditUsername", "Failed to delete old Firestore document", eDelete);
                                                    Toast.makeText(EditUsername.this, "Failed to delete old user in Firestore.", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("EditUsername", "Failed to create new Firestore document", e);
                                        Toast.makeText(EditUsername.this, "Failed to update user in Firestore.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(EditUsername.this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EditUsername.this, "Old user document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EditUsername", "Failed to retrieve old Firestore document", e);
                    Toast.makeText(EditUsername.this, "Failed to retrieve old user in Firestore.", Toast.LENGTH_SHORT).show();
                });
    }





}