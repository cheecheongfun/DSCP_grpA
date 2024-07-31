package sg.edu.np.mad.greencycle.Fragments.Home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.lifecycle.ViewModelStoreOwner;

import java.util.ArrayList;
import java.util.List;

import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.SolarForecast.ForecastViewModel;
import sg.edu.np.mad.greencycle.SolarForecast.OpenMeteoResponse;

public class CardPagerAdapter extends PagerAdapter {

    private Context mContext;
    private ForecastViewModel forecastViewModel;

    public CardPagerAdapter(Context context, ViewModelStoreOwner owner) {
        mContext = context;
        forecastViewModel = new ViewModelProvider(owner).get(ForecastViewModel.class);
    }

    @Override
    public int getCount() {
        return 2; // Number of cards
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view;

        if (position == 0) {
            view = inflater.inflate(R.layout.card_energy_prediction, container, false);
            TextView soeEnergyText = view.findViewById(R.id.card_soe_energy);
            TextView estateEnergyText = view.findViewById(R.id.card_estate_energy);

            // Observe the forecast data and update the TextViews
            forecastViewModel.getAggregatedDataLiveData().observe((LifecycleOwner) mContext, aggregatedData -> {
                Log.d("CardPagerAdapter", "Aggregated data received: " + aggregatedData);
                if (aggregatedData != null) {
                    double[] model2Output = forecastViewModel.getModel2Outputs("SOE");
                    double[] model3Output = forecastViewModel.getModel3Outputs("SOE");
                    double[] estateOutput = forecastViewModel.getModel1Outputs("Estate");

                    double combinedSoeOutput = (model2Output != null ? model2Output[0] : 0) + (model3Output != null ? model3Output[0] : 0);

                    if (combinedSoeOutput > 0) {
                        soeEnergyText.setText("Predicted SOE Energy for Next Day: " + combinedSoeOutput + " kWh");
                    } else {
                        soeEnergyText.setText("No data available for SOE energy prediction.");
                    }

                    if (estateOutput != null && estateOutput.length > 0) {
                        estateEnergyText.setText("Predicted Estate Energy for Next Day: " + estateOutput[0] + " kWh");
                    } else {
                        estateEnergyText.setText("No data available for Estate energy prediction.");
                    }
                } else {
                    soeEnergyText.setText("No aggregated data available.");
                    estateEnergyText.setText("No aggregated data available.");
                }
            });

            // Fetch forecast data for Singapore coordinates
            Log.d("CardPagerAdapter", "Fetching forecast data for Singapore coordinates.");
            forecastViewModel.fetchForecastData(1.3521, 103.8198, "temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,cloud_cover", "temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,cloud_cover", "Asia/Singapore", 3);

            // Fetch model outputs after forecast data is fetched
            forecastViewModel.getForecastLiveData().observe((LifecycleOwner) mContext, openMeteoResponse -> {
                if (openMeteoResponse != null) {
                    // Extract dates from the forecast data
                    List<String> dates = new ArrayList<>();
                    for (String datetime : openMeteoResponse.hourly.time) {
                        String date = datetime.substring(0, datetime.indexOf('T'));
                        if (!dates.contains(date)) {
                            dates.add(date);
                        }
                    }
                    if (dates.size() >= 1) {
                        // Fetch model outputs after forecast data is fetched
                        forecastViewModel.updateAggregatedData(dates, 0, "SOE");
                    } else {
                        Log.e("CardPagerAdapter", "Not enough dates available in the forecast data.");
                    }
                }
            });
        } else {
            view = inflater.inflate(R.layout.card_placeholder, container, false);
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
