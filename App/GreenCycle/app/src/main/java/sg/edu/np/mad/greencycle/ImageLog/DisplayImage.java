package sg.edu.np.mad.greencycle.ImageLog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.TankSelection.TankSelection;
import sg.edu.np.mad.greencycle.R;

public class DisplayImage extends AppCompatActivity {
    User user;
    Tank tank;
    ImageButton upload, gallery,calender;
    RecyclerView recyclerView;
    ImageAdapter adapter;
    TextView back;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");

        upload = findViewById(R.id.Compostcamera);
        gallery = findViewById(R.id.gallery);
        back = findViewById(R.id.backButton);
        calender = findViewById(R.id.Calender);
        recyclerView = findViewById(R.id.rvTanks);

        setupNavigationListeners();
        setupRecyclerView();
        loadImages();
    }

    private void setupNavigationListeners() {
        back.setOnClickListener(view -> {
            Intent intent = new Intent(DisplayImage.this, TankSelection.class);
            intent.putExtra("user", user);
            intent.putExtra("where", "Identify");
            startActivity(intent);
        });

        upload.setOnClickListener(view -> {
            Intent feed = new Intent(getApplicationContext(), UploadImage.class);
            Bundle info = new Bundle();
            info.putParcelable("tank", tank);
            info.putParcelable("user", user);
            feed.putExtras(info);
            startActivity(feed);
        });

        gallery.setOnClickListener(view -> {
            Intent feed = new Intent(getApplicationContext(), CompostGalleryActivity.class);
            Bundle info = new Bundle();
            info.putParcelable("tank", tank);
            info.putParcelable("user", user);
            feed.putExtras(info);
            startActivity(feed);
        });
    }

    private void setupRecyclerView() {
        adapter = new ImageAdapter(this, new ArrayList<>(), imageData -> {
            // Create an AlertDialog to confirm deletion
            new AlertDialog.Builder(DisplayImage.this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Proceed with deletion if user confirms
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Users").document(user.getUsername())
                                .collection("Tanks").document(String.valueOf(tank.getTankID()))
                                .collection("Images").document(imageData.get("docId"))
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    adapter.imageDataList.remove(imageData);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(DisplayImage.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DisplayImage.this, "Error deleting image", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null)  // Dismiss dialog if user cancels
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }


    private void loadImages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUsername())
                .collection("Tanks").document(String.valueOf(tank.getTankID()))
                .collection("Images")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, String>> imageDataList = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    queryDocumentSnapshots.forEach(document -> {
                        Map<String, String> imageData = new HashMap<>();
                        imageData.put("imageUrl", document.getString("image_url"));
                        imageData.put("timestamp", sdf.format(document.getTimestamp("timestamp").toDate()));
                        imageData.put("docId", document.getId());  // Store document ID for deletion
                        imageDataList.add(imageData);
                    });
                    adapter.imageDataList = imageDataList;
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle the error appropriately
                });
    }
}
