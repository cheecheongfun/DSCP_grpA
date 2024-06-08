package sg.edu.np.mad.greencycle.Fragments.Home;


import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.TankSelection;
import sg.edu.np.mad.greencycle.NPKvalue.npk_value;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.SolarForecast.Forecast;
import sg.edu.np.mad.greencycle.Goals.Goals_Notification;
// Fionn, S10240073K
public class HomeFragment extends Fragment {

    ImageButton liveDataBtn, feedingLogBtn, analyticsBtn, goalsBtn, imageLogBtn, soilTypeBtn, solarForecastBtn, communityBtn, converterBtn;
    NumberPicker inputNo, inputUnit, outputUnit;
    TextView username, outputNo;
    String newInputNo, newInputUnit, newOutputUnit;




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

        Log.v("test",user.getUsername());
        Goals_Notification goalsNotification = new Goals_Notification();
        goalsNotification.updateGoalsCompletion(user,getContext());


        // Calling layout elements
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

        analyticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tankSelect = new Intent(getContext(), TankSelection.class);
                tankSelect.putExtra("user", user);
                tankSelect.putExtra("where", "Analytics");
                startActivity(tankSelect);
            }
        });

        imageLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tankSelect = new Intent(getContext(), TankSelection.class);
                tankSelect.putExtra("user", user);
                tankSelect.putExtra("where", "Identify");
                startActivity(tankSelect);
            }
        });

        goalsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goals = new Intent(getContext(), TankSelection.class);
                goals.putExtra("user", user);
                goals.putExtra("where", "GOALS");
                startActivity(goals);
            }
        });


        // Conversion section

        // For input amount
        inputNo.setTextColor(ContextCompat.getColor(getContext(), R.color.textColour));
        inputNo.setMaxValue(999);
        inputNo.setMinValue(0);
        inputNo.setValue(0);
        inputNo.setWrapSelectorWheel(true);
        inputNo.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                newInputNo = String.valueOf(i1);
            }
        });

        // For input unit measurement
        String units[] = {"ml", "g", "tbsp", "tsp", "cup"};
        inputUnit.setTextColor(ContextCompat.getColor(getContext(), R.color.textColour));
        inputUnit.setMinValue(0);
        inputUnit.setMaxValue(units.length - 1);
        inputUnit.setDisplayedValues(units);
        inputUnit.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        outputUnit.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                newInputUnit = units[i1];
            }
        });

        // For output unit measurement
        outputUnit.setMinValue(0);
        outputUnit.setTextColor(ContextCompat.getColor(getContext(), R.color.textColour));
        outputUnit.setMaxValue(units.length - 1);
        outputUnit.setDisplayedValues(units);
        outputUnit.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        outputUnit.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                newOutputUnit = units[i1];
            }
        });


        solarForecastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent forecast = new Intent(getContext(), Forecast.class);
                forecast.putExtra("user", user);
                startActivity(forecast);
            }
        });

        return view;
    }

}