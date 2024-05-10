package sg.edu.np.mad.greencycle.Fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import sg.edu.np.mad.greencycle.Fragments.Chatbot.ChatBotFragment;
import sg.edu.np.mad.greencycle.Fragments.Home.HomeFragment;
import sg.edu.np.mad.greencycle.Fragments.Resources.ResourcesFragment;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent receivingEnd = getIntent();
        String tab = receivingEnd.getExtras().getString("tab");


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