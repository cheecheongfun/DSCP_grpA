package sg.edu.np.mad.greencycle.SolarForecast;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import me.relex.circleindicator.CircleIndicator3;
import sg.edu.np.mad.greencycle.R;

public class Forecast extends AppCompatActivity {

    private TextView back, refresh;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private CircleIndicator3 indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.solar_forecast);

        back = findViewById(R.id.backButton);
        refresh = findViewById(R.id.refresh);
        viewPager = findViewById(R.id.viewPager);
        indicator = findViewById(R.id.indicator);

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        indicator.setViewPager(viewPager);

        viewPagerAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());

        refresh.setOnClickListener(v -> {
            Log.i("RefreshTag", "onClick: refresh");
            // Optionally, refresh or update data here
        });

        back.setOnClickListener(v -> {
           finish();
        });
    }
}

