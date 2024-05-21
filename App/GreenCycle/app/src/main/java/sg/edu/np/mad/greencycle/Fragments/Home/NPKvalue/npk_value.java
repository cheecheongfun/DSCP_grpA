package sg.edu.np.mad.greencycle.Fragments.Home.NPKvalue;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import sg.edu.np.mad.greencycle.R;

public class npk_value extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_npk_value);

        // Get buttons from layout
        Button btnBalanced = findViewById(R.id.btn_balanced);
        Button btnHighNitrogen = findViewById(R.id.btn_high_nitrogen);
        Button btnHighPhosphorous = findViewById(R.id.btn_high_phosphorous);
        Button btnHighPotassium = findViewById(R.id.btn_high_potassium);

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
