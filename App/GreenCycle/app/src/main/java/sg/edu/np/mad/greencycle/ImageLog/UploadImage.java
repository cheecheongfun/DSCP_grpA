package sg.edu.np.mad.greencycle.ImageLog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.Tank;
import sg.edu.np.mad.greencycle.LiveData.TankSelection;
import sg.edu.np.mad.greencycle.NPKvalue.npk_value;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.StartUp.MainActivity;

public class UploadImage extends AppCompatActivity {
    private StorageReference mStorageRef;
    private Uri imageUri;
    private ImageView imageView;
    private Button btnCancel;

    User user;
    Tank tank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        imageView = findViewById(R.id.imageView);
        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setVisibility(View.GONE);

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");

        findViewById(R.id.btnChooseImage).setOnClickListener(v -> chooseImage());
        findViewById(R.id.btnUploadImage).setOnClickListener(v -> {
            uploadImage();
        });

        btnCancel.setOnClickListener(v -> {
            resetImageView();
            btnCancel.setVisibility(View.GONE);  // Hide the cancel button again after reset
        });
    }

    private void chooseImage() {
        btnCancel.setVisibility(View.VISIBLE);
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
            imageView.setImageURI(imageUri);
        }
    }


    private void uploadImage() {
        btnCancel.setVisibility(View.VISIBLE);
        Toast.makeText(UploadImage.this, "Uploading Image", Toast.LENGTH_LONG).show();
        if (imageUri != null) {
            // Use Glide to load the image as a bitmap
            Glide.with(this)
                    .asBitmap()
                    .load(imageUri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            // Resize and compress the image
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            resource.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                            byte[] data = baos.toByteArray();

                            // Create a reference to the Firebase Storage
                            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("images/" + System.currentTimeMillis() + ".jpg");

                            // Upload the byte array to Firebase Storage
                            fileRef.putBytes(data).addOnSuccessListener(taskSnapshot ->
                                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        uploadImageDetailsToFirestore(uri.toString());
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(UploadImage.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                            ).addOnFailureListener(e -> {
                                Toast.makeText(UploadImage.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        } else {
            Toast.makeText(this, "Please Select an Image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageDetailsToFirestore(String imageUrl) {
        Map<String, Object> imageDetails = new HashMap<>();
        imageDetails.put("image_url", imageUrl);
        imageDetails.put("timestamp", new Timestamp(new java.util.Date()));
        btnCancel.setVisibility(View.GONE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUsername())
                .collection("Tanks").document(String.valueOf(tank.getTankID()))
                .collection("Images").document()
                .set(imageDetails)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UploadImage.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    resetImageView();
                    btnCancel.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(UploadImage.this, "Upload Error", Toast.LENGTH_SHORT).show());
    }

    private void resetImageView() {
        imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.uploadicon));
        imageUri = null; // Clear the image URI
        btnCancel.setVisibility(View.GONE);  // Ensure the cancel button is hidden after reset
    }
}
