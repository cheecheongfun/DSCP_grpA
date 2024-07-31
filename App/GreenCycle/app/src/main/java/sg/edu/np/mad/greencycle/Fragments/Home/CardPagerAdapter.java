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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.SolarForecast.ForecastViewModel;
import sg.edu.np.mad.greencycle.SolarForecast.OpenMeteoResponse;

public class CardPagerAdapter extends PagerAdapter {

    private Context mContext;
    private ForecastViewModel forecastViewModel;
    private DatabaseReference databaseReference;

    private String username1;

    public CardPagerAdapter(Context context, ViewModelStoreOwner owner,String username) {
        mContext = context;
        forecastViewModel = new ViewModelProvider(owner).get(ForecastViewModel.class);
        username1 = username;
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
            // Reference to the user's tanks in Firebase


            // Reference to the user's tanks in Firebase
            databaseReference = FirebaseDatabase.getInstance().getReference("users/" + username1 + "/tanks");

            // Retrieve and display tank data
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Access values directly from DataSnapshot
                        String tankName = snapshot.getKey();
                        String scheduleName = snapshot.child("feedSchedule/0/scheduleName").getValue(String.class);
                        String refDate = snapshot.child("feedSchedule/0/refDate").getValue(String.class);
                        String TankName = snapshot.child("tankName").getValue(String.class);

                        // Ignore tanks that have no feeding schedule
                        if (scheduleName == null || refDate == null) {
                            continue;
                        }

                        // Extract brownFood data if available
                        StringBuilder brownFoodStringBuilder = new StringBuilder();
                        DataSnapshot brownFoodSnapshot = snapshot.child("feedSchedule/0/brownFood");
                        if (brownFoodSnapshot.exists()) {
                            for (DataSnapshot brownFoodItem : brownFoodSnapshot.getChildren()) {
                                String brownFoodName = brownFoodItem.child("name").getValue(String.class);
                                Long brownFoodAmount = brownFoodItem.child("amount").getValue(Long.class);
                                if (brownFoodName != null && brownFoodAmount != null) {
                                    brownFoodStringBuilder.append(brownFoodAmount).append("g ").append(brownFoodName).append("\n");
                                }
                            }
                        }
                        String brownFoodString = brownFoodStringBuilder.toString().trim();

                        // Extract greenFood data if available
                        StringBuilder greenFoodStringBuilder = new StringBuilder();
                        DataSnapshot greenFoodSnapshot = snapshot.child("feedSchedule/0/greenFood");
                        if (greenFoodSnapshot.exists()) {
                            for (DataSnapshot greenFoodItem : greenFoodSnapshot.getChildren()) {
                                String greenFoodName = greenFoodItem.child("name").getValue(String.class);
                                Long greenFoodAmount = greenFoodItem.child("amount").getValue(Long.class);
                                if (greenFoodName != null && greenFoodAmount != null) {
                                    greenFoodStringBuilder.append(greenFoodAmount).append("g ").append(greenFoodName).append("\n");
                                }
                            }
                        }
                        String greenFoodString = greenFoodStringBuilder.toString().trim();

                        // Find views in card_placeholder layout
                        TextView scheduleNameTextView = view.findViewById(R.id.scheduleName);
                        TextView refDateTextView = view.findViewById(R.id.refDate);
                        TextView brownFoodTextView = view.findViewById(R.id.brownFood);
                        TextView greenFoodTextView = view.findViewById(R.id.greenFood);
                        TextView daysUntilFeedTextView = view.findViewById(R.id.refDate);

                        // Populate views with data
                        scheduleNameTextView.setText("Tank " + TankName);
                        refDateTextView.setText(refDate + " days");
                        brownFoodTextView.setText(brownFoodString.isEmpty() ? "N/A" : "Browns: " +  brownFoodString);
                        greenFoodTextView.setText(greenFoodString.isEmpty() ? "N/A" : "Greens: " + greenFoodString);

                        // Calculate the nearest feeding schedule date
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            Date currentDate = new Date();
                            Date feedDate = dateFormat.parse(refDate);
                            long diff = feedDate.getTime() - currentDate.getTime();
                            long daysUntilFeed = TimeUnit.MILLISECONDS.toDays(diff);

                            if (daysUntilFeed >= 0) {
                                Log.d("TankInfo", "Nearest feeding date: " + refDate);
                                daysUntilFeedTextView.setText(String.format("Days until next feed: %d", daysUntilFeed));
                            } else {
                                Log.d("TankInfo", "Feeding date has passed.");
                                daysUntilFeedTextView.setText("Feeding date has passed.");
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            daysUntilFeedTextView.setText("Date format error.");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("TankInfo", "loadTank:onCancelled", databaseError.toException());
                }
            });
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
