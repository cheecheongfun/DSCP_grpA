package sg.edu.np.mad.greencycle.ImageLog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.Tank;
import sg.edu.np.mad.greencycle.R;

public class CompostGalleryActivity extends AppCompatActivity implements galleryAdapter.ImageDeletionListener {
    User user;
    Tank tank;

    ImageButton recycle;

    private GridView gridView;
    private galleryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compost_gallery);

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");

        gridView = findViewById(R.id.gridView);
        recycle = findViewById(R.id.imageButton);

        fetchImages();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CompostGalleryActivity.this, DisplayImage.class);
                Map<String, String> item = (Map<String, String>) adapter.getItem(position);
                intent.putExtra("image_url", item.get("imageUrl"));
                startActivity(intent);
            }
        });

        recycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent feed = new Intent(getApplicationContext(), DisplayImage.class);
                Bundle info = new Bundle();
                info.putParcelable("tank", tank);
                info.putParcelable("user", user);
                feed.putExtras(info);
                startActivity(feed);
            }
        });
    }

    private void fetchImages() {
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
                        imageData.put("docId", document.getId());  // Include document ID
                        imageDataList.add(imageData);
                    });
                    adapter = new galleryAdapter(this, imageDataList, this);
                    gridView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load images.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDeleteImage(Map<String, String> imageData) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Yes", (dialog, which) -> deleteImageFromFirestore(imageData))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteImageFromFirestore(Map<String, String> imageData) {
        String docId = imageData.get("docId");  // Retrieve the document ID
        if (docId == null) {
            Toast.makeText(this, "Document ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUsername())
                .collection("Tanks").document(String.valueOf(tank.getTankID()))
                .collection("Images").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    adapter.imageDataList.remove(imageData);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(CompostGalleryActivity.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CompostGalleryActivity.this, "Error deleting image", Toast.LENGTH_SHORT).show();
                });
    }

}
