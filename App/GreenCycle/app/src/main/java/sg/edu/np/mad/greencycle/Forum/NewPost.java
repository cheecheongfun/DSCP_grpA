package sg.edu.np.mad.greencycle.Forum;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class NewPost extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 101;

    private Uri imageUri;
    private StorageReference mStorageRef;
    private EditText editPostTitle, editPostBody;
    private ImageButton btnCamera, btnGallery, back;
    private Button btnPost;
    private ViewPager viewPager;
    private User user;
    private TextView selectedTagsView;

    private List<String> postTags = new ArrayList<>();

    private ArrayList<Uri> imageUris = new ArrayList<>();
    private ViewPagerAdapter viewPagerAdapter;

    private Dialog fullImageDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        editPostTitle = findViewById(R.id.EditPostTitle);
        editPostBody = findViewById(R.id.EditPostbody);
        btnCamera = findViewById(R.id.camera);
        btnGallery = findViewById(R.id.gallery);
        btnPost = findViewById(R.id.post);
        viewPager = findViewById(R.id.viewPager);
        back = findViewById(R.id.backButton);
        selectedTagsView = findViewById(R.id.selectedTags);

        back.setOnClickListener(view -> finish());

        user = getIntent().getParcelableExtra("user");

        viewPagerAdapter = new ViewPagerAdapter(imageUris, this, position -> {
            if (position >= 0 && position < imageUris.size()) {
                viewPagerAdapter.removeImage(position);
                toggleImageVisibility(!imageUris.isEmpty());
            }
        }, this::showFullImageDialog);

        viewPager.setAdapter(viewPagerAdapter);

        btnCamera.setOnClickListener(v -> requestCameraPermission());
        btnGallery.setOnClickListener(v -> chooseImage());
        btnPost.setOnClickListener(v -> {
            if (editPostTitle.getText().toString().trim().isEmpty()) {
                Toast.makeText(NewPost.this, "Title is required", Toast.LENGTH_SHORT).show();
            } else {
                if (imageUris.isEmpty()) {
                    savePost(editPostTitle.getText().toString(), editPostBody.getText().toString(), new ArrayList<>());
                } else {
                    uploadImageAndSavePost();
                }
            }
        });

        ImageButton btnShowTags = findViewById(R.id.btnShowTags);
        btnShowTags.setOnClickListener(v -> showTagDialog());
    }

    private void showTagDialog() {
        TagDialogFragment tagDialog = new TagDialogFragment();
        tagDialog.setSelectedTags(new HashSet<>(postTags)); // Pass the current tags to the dialog
        tagDialog.setTagDialogListener(tags -> {
            postTags.clear();
            postTags.addAll(tags);
            updateSelectedTagsView();
        });
        tagDialog.show(getSupportFragmentManager(), "tagDialog");
    }


    private void updateSelectedTagsView() {
        StringBuilder tagsText = new StringBuilder("Selected Tags: ");
        for (String tag : postTags) {
            tagsText.append(tag).append(" ");
        }
        selectedTagsView.setText(tagsText.toString().trim());
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
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
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageUris.add(imageUri);
                        viewPagerAdapter.notifyDataSetChanged();
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    imageUris.add(imageUri);
                    viewPagerAdapter.notifyDataSetChanged();
                }
                toggleImageVisibility(true);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST) {
                Uri imageUri = this.imageUri;
                imageUris.add(imageUri);
                viewPagerAdapter.notifyDataSetChanged();
                toggleImageVisibility(true);
            }
        } else {
            toggleImageVisibility(false);
        }
    }

    private void uploadImageAndSavePost() {
        setButtonsEnabled(false); // Disable all buttons before uploading

        List<String> uploadedUrls = new ArrayList<>();
        if (imageUris.isEmpty()) {
            savePost(editPostTitle.getText().toString(), editPostBody.getText().toString(), uploadedUrls);
            setButtonsEnabled(true); // Re-enable buttons if no images to upload
        } else {
            for (Uri uri : imageUris) {
                StorageReference fileRef = mStorageRef.child("post_images/" + System.currentTimeMillis() + ".jpg");
                fileRef.putFile(uri)
                        .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            uploadedUrls.add(downloadUri.toString());
                            if (uploadedUrls.size() == imageUris.size()) {
                                savePost(editPostTitle.getText().toString(), editPostBody.getText().toString(), uploadedUrls);
                                setButtonsEnabled(true); // Re-enable buttons after upload
                            }
                        }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(NewPost.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            setButtonsEnabled(true); // Re-enable buttons on failure
                        });
            }
        }
    }


    private void savePost(String title, String content, List<String> imageUrls) {
        Map<String, Object> post = new HashMap<>();
        post.put("title", title);
        post.put("content", content);
        post.put("timestamp", new Timestamp(new Date()));
        post.put("user", user.getUsername());
        post.put("imageUrls", imageUrls);
        post.put("tags", postTags);

        FirebaseFirestore.getInstance()
                .collection("Post")
                .document("posts")
                .collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(NewPost.this, "Post saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NewPost.this, "Error adding post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPost.setEnabled(true);
                });
    }

    public void toggleImageVisibility(boolean isVisible) {
        if (isVisible) {
            viewPager.setVisibility(View.VISIBLE);
            adjustBodyTextPosition(true);
        } else {
            viewPager.setVisibility(View.GONE);
            adjustBodyTextPosition(false);
        }
    }

    public void adjustBodyTextPosition(boolean isImageVisible) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) editPostBody.getLayoutParams();
        if (isImageVisible) {
            layoutParams.addRule(RelativeLayout.BELOW, R.id.viewPager);
        } else {
            layoutParams.addRule(RelativeLayout.BELOW, R.id.EditPostTitle);
        }
        editPostBody.setLayoutParams(layoutParams);
    }

    private void showFullImageDialog(Uri imageUri) {
        fullImageDialog = new Dialog(this);
        fullImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fullImageDialog.setContentView(R.layout.full_image_dialog);

        ImageView fullImageView = fullImageDialog.findViewById(R.id.fullImageView);
        Glide.with(this).load(imageUri).into(fullImageView);

        fullImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fullImageDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fullImageDialog.show();

        fullImageView.setOnClickListener(v -> fullImageDialog.dismiss());
    }
    private void setButtonsEnabled(boolean enabled) {
        btnCamera.setEnabled(enabled);
        btnGallery.setEnabled(enabled);
        btnPost.setEnabled(enabled);
        back.setEnabled(enabled);
        viewPager.setEnabled(enabled);
        findViewById(R.id.btnShowTags).setEnabled(enabled);
    }

}
