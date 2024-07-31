package sg.edu.np.mad.greencycle.SolarForecast;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import sg.edu.np.mad.greencycle.R;

public class Forecast extends AppCompatActivity {

    private TextView back;
    private DataViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.solar_forecast);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forecastLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        back = findViewById(R.id.backButton);


        back.setOnClickListener(v -> finish());



        // Load ForecastWeatherFragment
        if (savedInstanceState == null) {
            ForecastWeatherFragment fragment = new ForecastWeatherFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(DataViewModel.class);

        // Load data
        Log.e("Forecast", "in forecast");
        viewModel.loadData();
    }

    private void refreshFragment() {
        // Reload the fragment
        ForecastWeatherFragment fragment = new ForecastWeatherFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
