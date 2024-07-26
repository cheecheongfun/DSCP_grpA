package sg.edu.np.mad.greencycle.SolarForecast;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import sg.edu.np.mad.greencycle.R;

public class Forecast extends AppCompatActivity {

    private TextView back, refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.solar_forecast);

        back = findViewById(R.id.backButton);
        refresh = findViewById(R.id.refresh);

        back.setOnClickListener(v -> finish());

        refresh.setOnClickListener(v -> {
            Log.i("RefreshTag", "onClick: refresh");
            // Optionally, refresh or update data here
            // You may need to refresh data within the fragment
            refreshFragment();
        });

        // Load ForecastWeatherFragment
        if (savedInstanceState == null) {
            ForecastWeatherFragment fragment = new ForecastWeatherFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    private void refreshFragment() {
        // Reload the fragment
        ForecastWeatherFragment fragment = new ForecastWeatherFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
