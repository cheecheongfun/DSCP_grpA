package sg.edu.np.mad.greencycle.Profile;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.Forum.Post;
import sg.edu.np.mad.greencycle.Forum.PostAdapter;
import sg.edu.np.mad.greencycle.Fragments.Home.HomeFragment;
import sg.edu.np.mad.greencycle.R;

public class profile extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 101;

    private static final int EDIT_DISPLAY_NAME_REQUEST = 100;

    private Uri imageUri;
    private User user;

    private TextView usernameTextView;
    private TextView displayNameTextView,name;
    private CircleImageView imageView;

    private RecyclerView recyclerView;
    private ProfilePostAdapter postAdapter;
    private List<Post> postList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    ImageButton back,editusername,editDisplayName,editpfpbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        imageView = findViewById(R.id.profileImageView);
        back = findViewById(R.id.backButton);
        usernameTextView = findViewById(R.id.usernameText);
        editDisplayName = findViewById(R.id.editNameButton);
        name = findViewById(R.id.nametext);


        user = getIntent().getParcelableExtra("user");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new ProfilePostAdapter(postList, profile.this, user);
        recyclerView.setAdapter(postAdapter);

        fetchPostsByTag();

        if (user != null) {
            usernameTextView.setText(user.getUsername());

        }
        loadProfileImage();

        imageView.setOnClickListener(view -> showBottomSheetDialog());

        editDisplayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(profile.this,EditDisplayName.class);
                intent.putExtra("user", user);
                startActivityForResult(intent, EDIT_DISPLAY_NAME_REQUEST);
            }
        });


        back.setOnClickListener(view -> {
            finish(); // Just finish the current activity to go back
        });



    }
    private void loadDisplayName() {
        if (user != null && user.getUsername() != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUsername()).child("displayname");
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String displayName = dataSnapshot.getValue(String.class);
                    name.setText(displayName);
                    user.setDisplayname(displayName); // Update user object
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("DatabaseError", databaseError.getMessage()); // Don't ignore errors
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDisplayName();
    }



    private void loadProfileImage() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUsername())
                .collection("Profile Picture").document("Profile Image ID")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("imageUrl")) {
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            loadProfilePicture(imageUrl);
                        }
                    } else {
                        // Handle case where there is no image
                        imageView.setImageResource(R.drawable.green_cycle_icon); // Default image if none
                    }
                })
                .addOnFailureListener(e -> Log.d("Firestore", "Error getting documents: ", e));
    }

    private void loadProfilePicture(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the original data and the transformed image
                .placeholder(R.drawable.green_cycle_icon) // Shown during loading
                .error(R.drawable.green_cycle_icon)       // Shown on error
                .into(imageView);
    }

    private void preloadProfileImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload();
    }



    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.dialog_bottom_sheet);

        bottomSheetDialog.findViewById(R.id.btn_choose_from_library).setOnClickListener(v -> chooseImage());
        bottomSheetDialog.findViewById(R.id.btn_take_photo).setOnClickListener(v -> launchCamera());
        bottomSheetDialog.findViewById(R.id.btn_remove_picture).setOnClickListener(v -> removeProfilePicture());

        bottomSheetDialog.show();
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void launchCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e("Camera", "Error creating file", ex);
                }
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            getPackageName() + ".provider",
                            photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                "JPEG_" + timeStamp,
                ".jpg",
                storageDir
        );
        imageUri = Uri.fromFile(image);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                imageUri = data.getData();
                imageView.setImageURI(imageUri);
                uploadImageToStorage(imageUri);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST) {
                imageView.setImageURI(imageUri);
                uploadImageToStorage(imageUri);
            }
        }
        if (requestCode == EDIT_DISPLAY_NAME_REQUEST && resultCode == RESULT_OK) {
            String newDisplayName = data.getStringExtra("newDisplayName");
            user.setDisplayname(newDisplayName);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToStorage(Uri uri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("Profile_Pictures/" + user.getUsername() + ".jpg");
        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(this::saveImageUrlToFirestore))
                .addOnFailureListener(e -> Toast.makeText(profile.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveImageUrlToFirestore(Uri uri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUsername())
                .collection("Profile Picture")
                .document("Profile Image ID").set(new HashMap<String, Object>() {{
                    put("imageUrl", uri.toString());
                }})
                .addOnSuccessListener(aVoid -> {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("newProfileUri", uri.toString());
                    setResult(RESULT_OK, returnIntent);
                    Toast.makeText(profile.this, "Profile Picture Updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(profile.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
    }


    private void removeProfilePicture() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUsername())
                .collection("Profile Picture").document("Profile Image ID")
                .delete()
                .addOnSuccessListener(aVoid -> {
                    imageView.setImageResource(R.drawable.green_cycle_icon); // Set to default image
                    Toast.makeText(profile.this, "Profile Picture Removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(profile.this, "Error removing picture", Toast.LENGTH_SHORT).show());
    }

    private void fetchPostsByTag() {
        // Execute a new query based on the selected tag
        Query query = db.collection("Post").document("posts").collection("posts")
                .whereEqualTo("user", user.getUsername())
                .orderBy("timestamp", Query.Direction.DESCENDING);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error);
                    return;
                }

                postList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Post post = doc.toObject(Post.class);
                    post.setId(doc.getId());
                    if (post.getLikedBy() == null) {
                        post.setLikedBy(new ArrayList<>());
                    }
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }
        });
    }
}
