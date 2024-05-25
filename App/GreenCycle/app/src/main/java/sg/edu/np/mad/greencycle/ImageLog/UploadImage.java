package sg.edu.np.mad.greencycle.ImageLog;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.content.Intent;
import android.net.Uri;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.Timestamp;

import androidx.appcompat.app.AppCompatActivity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.Tank;
import sg.edu.np.mad.greencycle.R;

public class UploadImage extends AppCompatActivity {
    private StorageReference mStorageRef;
    private Uri imageUri;
    int targetTankId;

    User user;
    Tank tank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");
        targetTankId = tank.getTankID();
        Log.i(null, "Tank ID: " + targetTankId);

        findViewById(R.id.btnChooseImage).setOnClickListener(v -> chooseImage());
        findViewById(R.id.btnUploadImage).setOnClickListener(v -> uploadImage());
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            // Setting up the reference for the image in Firebase Storage
            StorageReference fileRef = mStorageRef.child("images/" + user.getUsername() + ".jpg");

            // Uploading the image to Firebase Storage
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Getting the download URL of the uploaded image
                        String imageUrl = uri.toString();

                        // Properly creating the Timestamp from System.currentTimeMillis()
                        long currentTimeMillis = System.currentTimeMillis();
                        long seconds = currentTimeMillis / 1000; // Convert milliseconds to seconds
                        int nanoseconds = (int) ((currentTimeMillis % 1000) * 1000000); // Convert the leftover milliseconds to nanoseconds

                        // Creating the Timestamp object for Firestore
                        Timestamp timestamp = new Timestamp(seconds, nanoseconds);

                        // Preparing data to be saved in Firestore
                        Map<String, Object> imageDetails = new HashMap<>();
                        imageDetails.put("image_url", imageUrl);
                        imageDetails.put("timestamp", timestamp);

                        // Assume you already have the user and tank objects from the intent or another source
                        if (user != null && tank != null) {
                            // Get user ID and Tank ID
                            String userId = user.getUsername() ;// Ensure you have getUserId() in your User class
                            int tankId = tank.getTankID();

                            // Saving the data in Firestore under the user and tank specific document
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("Users").document(userId)
                                    .collection("Tanks").document(String.valueOf(tankId))
                                    .collection("Images").document()  // Auto-generating document ID for the image
                                    .set(imageDetails)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(UploadImage.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(UploadImage.this, "Upload Error", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(UploadImage.this, "User or Tank data not found", Toast.LENGTH_SHORT).show();
                        }
                    })).addOnFailureListener(e -> {
                Toast.makeText(UploadImage.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Please Select an Image", Toast.LENGTH_SHORT).show();
        }
    }

}
