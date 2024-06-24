package sg.edu.np.mad.greencycle.SolarForecast;

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
import androidx.lifecycle.ViewModelProvider;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.StartUp.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
// Fionn, S10240073K
public class Forecast extends AppCompatActivity {

    TextView refresh, back, humidity;
    private WeatherViewModel viewModel;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.solar_forecast);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forecastPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");

        back = findViewById(R.id.backButton);
        refresh = findViewById(R.id.refresh);
        humidity = findViewById(R.id.humidity);

        viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);
        fetchData();

        // Handle button click to refresh data
        refresh.setOnClickListener(v -> {
            Log.i(null, "on click refresh");
            fetchData();
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Forecast.this, MainActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("tab", "home_tab");
                startActivity(intent);
            }
        });

    }

    public void fetchData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        viewModel.fetchHumidityData(currentDate);
        viewModel.getHumidityLiveData().observe(this, stationReadings -> {
            if (stationReadings != null && !stationReadings.isEmpty()) {
                // Assuming you want to display the first station's humidity
                for (int i = 0; i<stationReadings.size(); i++){
                    if (stationReadings.get(i).station_id.equals("S50")){
                        humidity.setText("Humidity: " + stationReadings.get(i).value + "%");
                        break;
                    }
                    else humidity.setText("No data");
                }
            }
        });
    }

}