package sg.edu.np.mad.greencycle.LiveData;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.Goals.ViewGoals;
import sg.edu.np.mad.greencycle.R;

public class LiveData extends AppCompatActivity {
    User user;
    Tank tank;
    FirebaseDatabase database;
    DatabaseReference reference;
    TextView backButton, temp, humidity, pH, nitrogen,phosphorous,potassium, feedback1, feedback2, tankName,goalbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.live_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");

        // Call layout elements
        tankName = findViewById(R.id.tankName);
        backButton = findViewById(R.id.backButton);
        temp = findViewById(R.id.temperatureData);
        humidity = findViewById(R.id.humidityData);
        pH = findViewById(R.id.phData);
        nitrogen = findViewById(R.id.nitrogenData);
        phosphorous = findViewById(R.id.phosphorousData);
        potassium = findViewById(R.id.potassiumData);
        feedback1 = findViewById(R.id.point1);
        feedback2 = findViewById(R.id.point2);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LiveData.this, TankSelection.class);
                intent.putExtra("user", user);
                intent.putExtra("where", "LiveData");
                startActivity(intent);
                finish();
            }
        });
        

        tankName.setText(tank.getTankName());
        temp.setText("Temperature: " + tank.getTemperature() + "°C");
        humidity.setText("Humidity: " + tank.getHumidity() + "%");
        pH.setText("pH Level: " + tank.getPHValue());
        nitrogen.setText("Nitrogen: " + tank.getNpkValues().get(0));
        phosphorous.setText("Phosphorous: " + tank.getNpkValues().get(1));
        potassium.setText("Potassium: " + tank.getNpkValues().get(2));
    }
}