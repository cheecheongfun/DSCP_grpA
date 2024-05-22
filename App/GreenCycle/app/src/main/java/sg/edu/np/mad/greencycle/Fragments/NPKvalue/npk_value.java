package sg.edu.np.mad.greencycle.Fragments.NPKvalue;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import sg.edu.np.mad.greencycle.Fragments.Home.HomeFragment;
import sg.edu.np.mad.greencycle.R;

public class npk_value extends AppCompatActivity {
    Button btnBalanced,btnHighNitrogen,btnHighPhosphorous,btnHighPotassium;
    ImageButton npkBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_npk_value);

        // Get buttons from layout
        btnBalanced = findViewById(R.id.btn_balanced);
        btnHighNitrogen = findViewById(R.id.btn_high_nitrogen);
        btnHighPhosphorous = findViewById(R.id.btn_high_phosphorous);
        btnHighPotassium = findViewById(R.id.btn_high_potassium);
        npkBack = findViewById(R.id.backbuttonnpk);

        npkBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backhome = new Intent(npk_value.this, HomeFragment.class);
                startActivity(backhome);
            }
        });


        // Set click listeners
        btnBalanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new BalancedFragment());
            }
        });

        btnHighNitrogen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new HighNitrogenFragment());
            }
        });

        btnHighPhosphorous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new HighPhosphorousFragment());
            }
        });

        btnHighPotassium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new HighPotassiumFragment());
            }
        });

        // Load default fragment
        loadFragment(new BalancedFragment());
    }

    private void loadFragment(Fragment fragment) {
        // Create fragment manager
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Create fragment transaction
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Replace the fragment on container
        fragmentTransaction.replace(R.id.npkfragmentContainer, fragment);
        // Commit the transaction
        fragmentTransaction.commit();
    }
}
