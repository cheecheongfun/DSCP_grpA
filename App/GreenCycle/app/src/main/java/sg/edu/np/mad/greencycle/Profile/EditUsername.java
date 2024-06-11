//package sg.edu.np.mad.greencycle.Profile;
//
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//import androidx.activity.EdgeToEdge;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import sg.edu.np.mad.greencycle.Classes.FirestoreHelper;
//import sg.edu.np.mad.greencycle.Classes.User;
//import sg.edu.np.mad.greencycle.R;
//
//public class EditUsername extends AppCompatActivity {
//    private DatabaseReference databaseReference;
//    private FirebaseFirestore firestore;
//    private FirestoreHelper firestoreHelper;
//    private User user;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_edit_username);
//
//        user = getIntent().getParcelableExtra("user");
//        databaseReference = FirebaseDatabase.getInstance().getReference("users");
//        firestore = FirebaseFirestore.getInstance();
//        firestoreHelper = new FirestoreHelper(this); // Initialize the FirestoreHelper with context
//
//        setupButtonListeners();
//    }
//
//    private void setupButtonListeners() {
//        Button doneButton = findViewById(R.id.button);
//        doneButton.setOnClickListener(view -> {
//            EditText usernameEditText = findViewById(R.id.editTextUsername);
//            String newUsername = usernameEditText.getText().toString().trim();
//            if (!newUsername.isEmpty()) {
//                checkUsernameAvailability(newUsername);
//            } else {
//                Toast.makeText(EditUsername.this, "Please enter a username.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void checkUsernameAvailability(final String newUsername) {
//        if (!newUsername.equals(user.getUsername())) {
//            databaseReference.orderByChild("username").equalTo(newUsername).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        Toast.makeText(EditUsername.this, "Username is already taken.", Toast.LENGTH_SHORT).show();
//                    } else {
//                        updateUser(newUsername);
//                        user.setUsername(newUsername);  // Update the username in the user object
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Toast.makeText(EditUsername.this, "Error checking username.", Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            updateUser(newUsername);
//        }
//    }
//
//    private void updateUser(String newUsername) {
//        String oldUsername = user.getUsername();
//        DatabaseReference oldUserRef = databaseReference.child(oldUsername);
//        oldUserRef.removeValue().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                user.setUsername(newUsername);
//                DatabaseReference newUserRef = databaseReference.child(newUsername);
//                newUserRef.setValue(user)
//                        .addOnSuccessListener(aVoid -> {
//                            Toast.makeText(EditUsername.this, "User updated successfully in Realtime Database.", Toast.LENGTH_SHORT).show();
//                            // Use FirestoreHelper to duplicate the user in Firestore
//                            firestoreHelper.renameUser(oldUsername, newUsername);
//                        })
//                        .addOnFailureListener(e -> Toast.makeText(EditUsername.this, "Failed to create new user node in Realtime Database.", Toast.LENGTH_SHORT).show());
//            } else {
//                Toast.makeText(EditUsername.this, "Failed to delete old user node in Realtime Database.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}
