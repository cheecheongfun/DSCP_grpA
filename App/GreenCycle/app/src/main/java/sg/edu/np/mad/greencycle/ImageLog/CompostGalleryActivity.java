package sg.edu.np.mad.greencycle.ImageLog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
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

public class CompostGalleryActivity extends AppCompatActivity {
    User user;
    Tank tank;

    private GridView gridView;
    private galleryAdapter adapter; // Assume ImageAdapter is adjusted to work with GridView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compost_gallery);

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");


        gridView = findViewById(R.id.gridView);

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
    }

    private void fetchImages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUsername())
                .collection("Tanks").document(String.valueOf(tank.getTankID()))
                .collection("Images")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, String>> imageDataList = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    queryDocumentSnapshots.forEach(document -> {
                        Map<String, String> imageData = new HashMap<>();
                        imageData.put("imageUrl", document.getString("image_url"));
                        imageData.put("timestamp", sdf.format(document.getTimestamp("timestamp").toDate()));
                        imageDataList.add(imageData);
                    });
                    adapter = new galleryAdapter(this, imageDataList);
                    gridView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    // Handle the error appropriately
                });
    }
}
