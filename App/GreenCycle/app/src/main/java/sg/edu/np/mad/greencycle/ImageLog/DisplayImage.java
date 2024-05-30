package sg.edu.np.mad.greencycle.ImageLog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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
import sg.edu.np.mad.greencycle.LiveData.Tank;
import sg.edu.np.mad.greencycle.LiveData.TankSelection;
import sg.edu.np.mad.greencycle.R;
// Oh Ern Qi S10243067K
public class DisplayImage extends AppCompatActivity {
    User user;
    Tank tank;
    ImageButton upload,gallery;
    RecyclerView recyclerView;
    ImageAdapter adapter;
    TextView back;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");

        upload = findViewById(R.id.Compostcamera);
        recyclerView = findViewById(R.id.rvTanks);
        gallery = findViewById(R.id.gallery);
        back = findViewById(R.id.backButton);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DisplayImage.this, TankSelection.class);
                intent.putExtra("user", user);
                intent.putExtra("where", "Identify");
                startActivity(intent);
            }
        });

        upload.setOnClickListener(view -> {
            Intent feed = new Intent(getApplicationContext(), UploadImage.class);
            Bundle info = new Bundle();
            info.putParcelable("tank", tank);
            info.putParcelable("user", user);
            feed.putExtras(info);
            startActivity(feed);
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent feed = new Intent(getApplicationContext(), CompostGalleryActivity.class);
                Bundle info = new Bundle();
                info.putParcelable("tank", tank);
                info.putParcelable("user", user);
                feed.putExtras(info);
                startActivity(feed);

            }
        });

        adapter = new ImageAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        loadImages();
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
                        imageDataList.add(imageData);
                    });
                    adapter = new ImageAdapter(this, imageDataList);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    // Handle the error appropriately
                });
    }
}
