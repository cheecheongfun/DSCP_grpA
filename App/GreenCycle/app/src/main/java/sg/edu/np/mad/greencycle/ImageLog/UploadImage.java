package sg.edu.np.mad.greencycle.ImageLog;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.Tank;
import sg.edu.np.mad.greencycle.LiveData.TankSelection;
import sg.edu.np.mad.greencycle.R;

public class UploadImage extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 101;

    private StorageReference mStorageRef;
    private Uri imageUri;
    private ImageView imageView;
    private Button btnCancel, btnUpload;
    private TextView back;

    User user;
    Tank tank;
    private boolean isCancelled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        imageView = findViewById(R.id.imageView);
        btnCancel = findViewById(R.id.btnCancel);
        btnUpload = findViewById(R.id.btnUploadImage);
        back = findViewById(R.id.backButton);
        btnCancel.setVisibility(View.GONE);

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");

        findViewById(R.id.btnChooseImage).setOnClickListener(v -> chooseImage());
        btnUpload.setOnClickListener(v -> uploadImage());
        findViewById(R.id.btnUseCamera).setOnClickListener(v -> requestCameraPermission());

        btnCancel.setOnClickListener(v -> {
            isCancelled = true;
            resetImageView();
        });

        back.setOnClickListener(view -> {
            Intent intent = new Intent(UploadImage.this, DisplayImage.class);
            startActivity(intent);
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("Camera", "Error creating file", ex);
            }
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                imageUri = data.getData();
                imageView.setImageURI(imageUri);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST) {
                imageView.setImageURI(imageUri);
            }
            btnCancel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use the camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            isCancelled = false;
            btnUpload.setEnabled(false);
            Glide.with(this)
                    .asBitmap()
                    .load(imageUri)
                    .centerCrop()
                    .override(500, 500)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            if (!isCancelled) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                resource.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                                byte[] data = baos.toByteArray();
                                StorageReference fileRef = mStorageRef.child("images/" + System.currentTimeMillis() + ".jpg");
                                fileRef.putBytes(data).addOnSuccessListener(taskSnapshot ->
                                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                            if (!isCancelled) {
                                                uploadImageDetailsToFirestore(uri.toString());
                                            }
                                        }).addOnFailureListener(e -> {
                                            Toast.makeText(UploadImage.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            btnUpload.setEnabled(true);
                                        })
                                ).addOnFailureListener(e -> {
                                    Toast.makeText(UploadImage.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    btnUpload.setEnabled(true);
                                });
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageDetailsToFirestore(String imageUrl) {
        if (!isCancelled) {
            Map<String, Object> imageDetails = new HashMap<>();
            imageDetails.put("image_url", imageUrl);
            imageDetails.put("timestamp", new Timestamp(new Date()));
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(user.getUsername())
                    .collection("Tanks").document(String.valueOf(tank.getTankID()))
                    .collection("Images").document()
                    .set(imageDetails)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(UploadImage.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        resetImageView();
                        btnUpload.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UploadImage.this, "Upload Error", Toast.LENGTH_SHORT).show();
                        btnUpload.setEnabled(true);
                    });
        }
    }

    private void resetImageView() {
        btnUpload.setEnabled(true);
        imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.uploadicon));
        imageUri = null;
        btnCancel.setVisibility(View.GONE);
    }
}
