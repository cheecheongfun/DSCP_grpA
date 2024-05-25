package sg.edu.np.mad.greencycle.Goals;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.Tank;
import sg.edu.np.mad.greencycle.R;

public class CreateGoals extends AppCompatActivity {

    User user;
    Tank tank;

    FirebaseDatabase database;
    DatabaseReference reference;

    private ImageButton backButton;
    private TextView selectGoalLabel;
    private Spinner goalSpinner;
    private TextView setNumberLabel;
    private EditText goalNumber;
    private TextView selectEndDateLabel;
    private DatePicker goalDatePicker;
    private Button saveGoalButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_goals);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        backButton = findViewById(R.id.back_button);
        selectGoalLabel = findViewById(R.id.select_goal_label);
        goalSpinner = findViewById(R.id.goal_spinner);
        setNumberLabel = findViewById(R.id.set_number_label);
        goalNumber = findViewById(R.id.goal_number);
        selectEndDateLabel = findViewById(R.id.select_end_date_label);
        goalDatePicker = findViewById(R.id.goal_date_picker);
        saveGoalButton = findViewById(R.id.save_goal_button);

        // Set spinner items
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.goal_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goalSpinner.setAdapter(adapter);

        database = FirebaseDatabase.getInstance();

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");
        Log.v("goals",user.getUsername());



    }
}

