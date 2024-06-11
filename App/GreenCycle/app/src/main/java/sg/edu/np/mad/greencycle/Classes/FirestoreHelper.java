//package sg.edu.np.mad.greencycle.Classes;
//
//import android.content.Context;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.Transaction;
//
//public class FirestoreHelper {
//    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private final Context context;
//
//    public FirestoreHelper(Context context) {
//        this.context = context;
//    }
//
//    public void renameUser(String oldUsername, String newUsername) {
//        Log.d("FirestoreHelper", "Attempting to rename from: " + oldUsername + " to: " + newUsername);
//        db.runTransaction(transaction -> {
//            DocumentReference oldUserRef = db.collection("Users").document(oldUsername);
//            DocumentSnapshot snapshot = transaction.get(oldUserRef);
//
//            if (!snapshot.exists()) {
//                Log.e("FirestoreHelper", "User not found for: " + oldUsername);
//                throw new IllegalStateException("User not found!");
//            }
//
//            DocumentReference newUserRef = db.collection("Users").document(newUsername);
//            transaction.set(newUserRef, snapshot.getData());
//            transaction.delete(oldUserRef);
//
//            return null;
//        }).addOnSuccessListener(aVoid -> {
//            Toast.makeText(context, "User renamed successfully in Firestore!", Toast.LENGTH_LONG).show();
//            Log.d("FirestoreHelper", "User renamed successfully from: " + oldUsername + " to: " + newUsername);
//        }).addOnFailureListener(e -> {
//            Toast.makeText(context, "Error renaming user in Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
//            Log.e("FirestoreHelper", "Error renaming user in Firestore: " + e.getMessage());
//        });
//    }
//}
