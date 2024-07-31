package sg.edu.np.mad.greencycle.Fragments.Home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.Forum.Forum;
import sg.edu.np.mad.greencycle.NPKvalue.npk_value;
import sg.edu.np.mad.greencycle.Profile.options;
import sg.edu.np.mad.greencycle.Profile.profile;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.SolarForecast.Forecast;
import sg.edu.np.mad.greencycle.SolarInsight.Insight;
import sg.edu.np.mad.greencycle.TankSelection.TankSelection;
import sg.edu.np.mad.greencycle.UnitConversion.Conversion;

public class HomeFragment extends Fragment {

    ImageButton liveDataBtn, feedingLogBtn, analyticsBtn, goalsBtn, soilTypeBtn, solarForecastBtn, communityBtn, settingsBtn, ConversionBtn, insightsBtn;
    TextView username;

    User user;

    private DrawerLayout drawerLayout;
    private CircleImageView imageView;

    private ViewPager viewPager;

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
        user = receivingEnd.getParcelableExtra("user");

        // Layout elements
        username = view.findViewById(R.id.userWelcome);
        liveDataBtn = view.findViewById(R.id.liveDataButton);
        feedingLogBtn = view.findViewById(R.id.feedingLogButton);
        analyticsBtn = view.findViewById(R.id.analyticsButton);
        goalsBtn = view.findViewById(R.id.goalsButton);
        ConversionBtn = view.findViewById(R.id.circleButton);
        insightsBtn = view.findViewById(R.id.insightsBtn);
        soilTypeBtn = view.findViewById(R.id.soilTypeButton);
        communityBtn = view.findViewById(R.id.communityButton);
        solarForecastBtn = view.findViewById(R.id.solarForecast);
        settingsBtn = view.findViewById(R.id.settings);
        drawerLayout = view.findViewById(R.id.drawer_layout);
        LinearLayout profileLayout = view.findViewById(R.id.nav_profile_layout);
        LinearLayout optionLayout = view.findViewById(R.id.nav_option_two_layout);
        imageView = view.findViewById(R.id.profileImageView);
        viewPager = view.findViewById(R.id.viewPager);

        if (username != null) {
            username.setText("Welcome, " + user.getDisplayname());
            loadProfileImage(user);
        }

        profileLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), profile.class);
            intent.putExtra("user", user);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.END);
        });

        optionLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), options.class);
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

        goalsBtn.setOnClickListener(view15 -> {
            Intent goals = new Intent(getContext(), TankSelection.class);
            goals.putExtra("user", user);
            goals.putExtra("where", "GOALS");
            startActivity(goals);
        });

        ConversionBtn.setOnClickListener(v -> {
            Conversion dialog = new Conversion(getContext());
            dialog.show();
        });

        communityBtn.setOnClickListener(viewcom -> {
            Intent intent = new Intent(getActivity(), Forum.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });

        solarForecastBtn.setOnClickListener(view16 -> {
            Intent forecast = new Intent(getContext(), Forecast.class);
            forecast.putExtra("user", user);
            startActivity(forecast);
        });

        insightsBtn.setOnClickListener(viewis -> {
            Intent insights = new Intent(getContext(), Insight.class);
            insights.putExtra("user", user);
            startActivity(insights);
        });

        // Set up the ViewPager
        CardPagerAdapter adapter = new CardPagerAdapter(getContext(), this,user.getUsername());
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        // Set up tap to change functionality
        viewPager.setOnClickListener(v -> {
            int nextItem = (viewPager.getCurrentItem() + 1) % adapter.getCount();
            viewPager.setCurrentItem(nextItem, true);
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
