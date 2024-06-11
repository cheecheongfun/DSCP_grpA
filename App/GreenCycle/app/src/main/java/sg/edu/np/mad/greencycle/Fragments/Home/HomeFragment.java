package sg.edu.np.mad.greencycle.Fragments.Home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.TankSelection;
import sg.edu.np.mad.greencycle.NPKvalue.npk_value;
import sg.edu.np.mad.greencycle.Profile.profile;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.SolarForecast.Forecast;
import sg.edu.np.mad.greencycle.Goals.Goals_Notification;

public class HomeFragment extends Fragment {

    ImageButton liveDataBtn, feedingLogBtn, analyticsBtn, goalsBtn, imageLogBtn, soilTypeBtn, solarForecastBtn, communityBtn, converterBtn, settingsBtn;
    NumberPicker inputNo, inputUnit, outputUnit;
    TextView username, outputNo;
    String newInputNo, newInputUnit, newOutputUnit;

    User user;

    private DrawerLayout drawerLayout;
    private CircleImageView imageView;

    public HomeFragment() {
        // Constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EdgeToEdge.enable(getActivity());
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Log.i(null, "Home Fragment");

        // Receiving intent
        Intent receivingEnd = getActivity().getIntent();
        String tab = receivingEnd.getExtras().getString("tab");
        user = receivingEnd.getParcelableExtra("user");



        Log.v("test", user.getUsername());
        Log.v("test", user.getDisplayname());
        Goals_Notification goalsNotification = new Goals_Notification();
        goalsNotification.updateGoalsCompletion(user, getContext());

        // Layout elements
        username = view.findViewById(R.id.userWelcome);
        liveDataBtn = view.findViewById(R.id.liveDataButton);
        feedingLogBtn = view.findViewById(R.id.feedingLogButton);
        analyticsBtn = view.findViewById(R.id.analyticsButton);
        goalsBtn = view.findViewById(R.id.goalsButton);
        imageLogBtn = view.findViewById(R.id.imageLog);
        soilTypeBtn = view.findViewById(R.id.soilTypeButton);
        communityBtn = view.findViewById(R.id.communityButton);
        converterBtn = view.findViewById(R.id.conversionButton);
        inputNo = view.findViewById(R.id.inputNo);
        inputUnit = view.findViewById(R.id.inputUnit);
        outputNo = view.findViewById(R.id.outputNo);
        outputUnit = view.findViewById(R.id.outputUnit);
        solarForecastBtn = view.findViewById(R.id.solarForecast);
        settingsBtn = view.findViewById((R.id.settings));
        drawerLayout = view.findViewById(R.id.drawer_layout);
        LinearLayout profileLayout = view.findViewById(R.id.nav_profile_layout);
        imageView = view.findViewById(R.id.profileImageView);

        username.setText("Welcome, " + user.getDisplayname());
        loadProfileImage(user);

        profileLayout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Profile clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), profile.class);
            intent.putExtra("user", user);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.END);
        });

        soilTypeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), npk_value.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });



        settingsBtn.setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.openDrawer(GravityCompat.END);
            } else {
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });

        liveDataBtn.setOnClickListener(view1 -> {
            Intent tankSelect = new Intent(getContext(), TankSelection.class);
            tankSelect.putExtra("user", user);
            tankSelect.putExtra("where", "LiveData");
            startActivity(tankSelect);
        });

        feedingLogBtn.setOnClickListener(view12 -> {
            Intent tankSelect = new Intent(getContext(), TankSelection.class);
            tankSelect.putExtra("user", user);
            tankSelect.putExtra("where", "Feeding");
            startActivity(tankSelect);
        });

        analyticsBtn.setOnClickListener(view13 -> {
            Intent tankSelect = new Intent(getContext(), TankSelection.class);
            tankSelect.putExtra("user", user);
            tankSelect.putExtra("where", "Analytics");
            startActivity(tankSelect);
        });

        imageLogBtn.setOnClickListener(view14 -> {
            Intent tankSelect = new Intent(getContext(), TankSelection.class);
            tankSelect.putExtra("user", user);
            tankSelect.putExtra("where", "Identify");
            startActivity(tankSelect);
        });

        goalsBtn.setOnClickListener(view15 -> {
            Intent goals = new Intent(getContext(), TankSelection.class);
            goals.putExtra("user", user);
            goals.putExtra("where", "GOALS");
            startActivity(goals);
        });

        inputNo.setTextColor(ContextCompat.getColor(getContext(), R.color.textColour));
        inputNo.setMaxValue(999);
        inputNo.setMinValue(0);
        inputNo.setValue(0);
        inputNo.setWrapSelectorWheel(true);
        inputNo.setOnValueChangedListener((numberPicker, i, i1) -> newInputNo = String.valueOf(i1));

        String[] units = {"ml", "g", "tbsp", "tsp", "cup"};
        inputUnit.setTextColor(ContextCompat.getColor(getContext(), R.color.textColour));
        inputUnit.setMinValue(0);
        inputUnit.setMaxValue(units.length - 1);
        inputUnit.setDisplayedValues(units);
        inputUnit.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        outputUnit.setMinValue(0);
        outputUnit.setTextColor(ContextCompat.getColor(getContext(), R.color.textColour));
        outputUnit.setMaxValue(units.length - 1);
        outputUnit.setDisplayedValues(units);
        outputUnit.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        outputUnit.setOnValueChangedListener((numberPicker, i, i1) -> newOutputUnit = units[i1]);

        solarForecastBtn.setOnClickListener(view16 -> {
            Intent forecast = new Intent(getContext(), Forecast.class);
            forecast.putExtra("user", user);
            startActivity(forecast);
        });

        return view;
    }



    private void loadProfileImage(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUsername())
                .collection("Profile Picture").document("Profile Image ID")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("imageUrl")) {
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            loadProfilePicture(imageUrl);
                        }
                    } else {
                        // Handle case where there is no image
                        imageView.setImageResource(R.drawable.green_cycle_icon); // Default image if none
                    }
                })
                .addOnFailureListener(e -> Log.d("Firestore", "Error getting documents: ", e));
    }

    private void loadProfilePicture(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the original data and the transformed image
                .placeholder(R.drawable.green_cycle_icon) // Shown during loading
                .error(R.drawable.green_cycle_icon)       // Shown on error
                .into(imageView);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Assuming 'user' is a class variable that holds the current user's data
        if (user != null && user.getUsername() != null) {
            SharedPreferences sharedPref = getActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            // Use username as part of the key to make it user-specific
            String displayNameKey = user.getUsername() + "_DisplayName";
            String displayName = sharedPref.getString(displayNameKey, "User"); // Default to "User" if not found
            username.setText("Welcome, " + displayName);
        }
    }




}
