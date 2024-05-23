package sg.edu.np.mad.greencycle.Fragments.Home;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.TankSelection;
import sg.edu.np.mad.greencycle.NPKvalue.npk_value;
import sg.edu.np.mad.greencycle.R;

public class HomeFragment extends Fragment {
    
    ImageButton npkButton, liveDataBtn, feedingLogBtn, analyticsBtn, goalsBtn, identifierBtn, soilTypeBtn;
    TextView username;

    

    public HomeFragment() {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EdgeToEdge.enable(getActivity());
        // Inflate the layout for this fragment
        View view;
        view =  inflater.inflate(R.layout.fragment_home, container, false);
        Log.i(null, "Home Fragment");

        // Receiving intent
        Intent receivingEnd = getActivity().getIntent();
        String tab = receivingEnd.getExtras().getString("tab");
        User user = receivingEnd.getParcelableExtra("user");

        // Calling layout elements
        username = view.findViewById(R.id.userWelcome);
        liveDataBtn = view.findViewById(R.id.liveDataButton);
        feedingLogBtn = view.findViewById(R.id.feedingLogButton);
        analyticsBtn = view.findViewById(R.id.analyticsButton);
        goalsBtn = view.findViewById(R.id.goalsButton);
        identifierBtn = view.findViewById(R.id.identifierButton);
        soilTypeBtn = view.findViewById(R.id.soilTypeButton);
        soilTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), npk_value.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        username.setText("Welcome, " + user.getUsername());

        // buttons to different pages
        liveDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tankSelect = new Intent(getContext(), TankSelection.class);
                tankSelect.putExtra("user", user);
                tankSelect.putExtra("where", "LiveData");
                startActivity(tankSelect);
            }
        });

        feedingLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tankSelect = new Intent(getContext(), TankSelection.class);
                tankSelect.putExtra("user", user);
                tankSelect.putExtra("where", "Feeding");
                startActivity(tankSelect);
            }
        });

        return view;
    }

}