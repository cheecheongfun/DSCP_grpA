package sg.edu.np.mad.greencycle.StartUp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.Fragments.Chatbot.ChatBotFragment;
import sg.edu.np.mad.greencycle.Fragments.Home.HomeFragment;
import sg.edu.np.mad.greencycle.Fragments.Resources.ResourcesFragment;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.databinding.ActivityMainBinding;
// Fionn, S10240073K
public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        int color = ContextCompat.getColor(this, R.color.active_tab);
        binding.bottomNavigationView.setItemActiveIndicatorColor(ColorStateList.valueOf(color));

        Intent receivingEnd = getIntent();
        String tab = receivingEnd.getExtras().getString("tab");
        // User user = receivingEnd.getParcelableExtra("user");


        if (tab.equals("resources_tab")){
            replaceFragment(new ResourcesFragment());

            // Sets navigation bar item to Resources
            binding.bottomNavigationView.setSelectedItemId(R.id.navigation_resources);

        } else if (tab.equals("chatbot_tab")) {
            replaceFragment(new ChatBotFragment());

            // Sets navigation bar item to chatbot
            binding.bottomNavigationView.setSelectedItemId(R.id.navigation_chatbot);

        } else{
            replaceFragment(new HomeFragment());
        }

        // Switching of navigation bar tabs
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.navigation_home){
                //Replace fragment with HomeFragment
                replaceFragment(new HomeFragment());
            }
            if (itemID == R.id.navigation_chatbot){
                //Replace fragment with Chatbot
                replaceFragment(new ChatBotFragment());
            }
            if (itemID == R.id.navigation_resources){
                //Replace fragment with TaskFragment
                replaceFragment(new ResourcesFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_frame, fragment);
        fragmentTransaction.commit();
    }




}