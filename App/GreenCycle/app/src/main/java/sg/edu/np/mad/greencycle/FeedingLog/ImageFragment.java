package sg.edu.np.mad.greencycle.FeedingLog;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class ImageFragment extends Fragment implements galleryAdapter.ImageDeletionListener {

    private GridView gridView;
    private galleryAdapter adapter;
    private User user;
    private Tank tank;
    private Context context;
    private LocalDate selectedDate;
    private int size = 0;


    public ImageFragment(User user, Tank tank, Context context, LocalDate selectedDate) {
        this.user = user;
        this.tank = tank;
        this.context = context;
        this.selectedDate = selectedDate;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_images, container, false);
        gridView = view.findViewById(R.id.gridView);

        fetchImages();

        return view;
    }

    private void fetchImages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

// Convert LocalDate to start and end of the day using Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth(), 0, 0, 0);
        Date startDate = calendar.getTime();
        Timestamp startTimestamp = new Timestamp(startDate);


        calendar.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth(), 23, 59, 59);
        Date endDate = calendar.getTime();
        Timestamp endTimestamp = new Timestamp(endDate);

        db.collection("Users").document(user.getUsername())
                .collection("Tanks").document(String.valueOf(tank.getTankID()))
                .collection("Images")
                .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                .whereLessThanOrEqualTo("timestamp", endTimestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, String>> imageDataList = new ArrayList<>();
                    queryDocumentSnapshots.forEach(document -> {
                        Map<String, String> imageData = new HashMap<>();
                        imageData.put("imageUrl", document.getString("image_url"));
                        imageData.put("timestamp", sdf.format(document.getTimestamp("timestamp").toDate()));
                        imageData.put("docId", document.getId());  // Include document ID
                        imageDataList.add(imageData);
                        Log.e("fetchImages", "success");
                    });
                    adapter = new galleryAdapter(getContext(), imageDataList, this);
                    gridView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load images.", Toast.LENGTH_SHORT).show();
                });

    }

    @Override
    public void onDeleteImage(Map<String, String> imageData) {
        new AlertDialog.Builder(context)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Yes", (dialog, which) -> deleteImageFromFirestore(imageData))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteImageFromFirestore(Map<String, String> imageData) {
        String docId = imageData.get("docId");  // Retrieve the document ID
        if (docId == null) {
            Toast.makeText(context, "Document ID is null", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(context, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error deleting image", Toast.LENGTH_SHORT).show();
                });
    }

}
