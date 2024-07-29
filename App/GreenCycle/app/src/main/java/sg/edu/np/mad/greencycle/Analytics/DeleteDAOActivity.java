package sg.edu.np.mad.greencycle.Analytics;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import sg.edu.np.mad.greencycle.Analytics.AppDatabase;
import sg.edu.np.mad.greencycle.Analytics.HourlyDataDao;
import sg.edu.np.mad.greencycle.R;

public class DeleteDAOActivity extends AppCompatActivity {
    private AppDatabase db;
    private HourlyDataDao hourlyDataDao;
    private Executor databaseExecutor;
    private ImageButton back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_daoactivity);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name")
                .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
                .build();
        hourlyDataDao = db.hourlyDataDao();
        databaseExecutor = Executors.newSingleThreadExecutor();
        back = findViewById(R.id.imageButton3);

        Button deleteButton = findViewById(R.id.btnDeleteData);
        deleteButton.setOnClickListener(v -> showConfirmationDialog());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete all data?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> clearAllData())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void clearAllData() {
        databaseExecutor.execute(() -> {
            hourlyDataDao.deleteAll();
            runOnUiThread(() -> Toast.makeText(this, "All data wiped", Toast.LENGTH_SHORT).show());
        });
    }
}
