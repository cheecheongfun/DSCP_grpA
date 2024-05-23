package sg.edu.np.mad.greencycle.NPKvalue;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sg.edu.np.mad.greencycle.R;


public class HighPotassiumFragment extends Fragment {
    TextView PotRec;



    public HighPotassiumFragment() {
        // Required empty public constructor
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
        view = inflater.inflate(R.layout.fragment_high_potassium, container, false);
        PotRec = view.findViewById(R.id.potassium_rec);
        String highPotassiumSoilInfo = "• 1:1:3\n"
                + "• Add banana peels, wood ash, and greensand to your vermicompost\n"
                + "• Include composted seaweed which is high in potassium\n"
                + "• Avoid adding too many nitrogen-rich materials to maintain high potassium levels\n"
                + "• Use vermicompost tea or diluted vermicompost leachate high in potassium as a natural fertilizer\n"
                + "• Rotate crops to maintain soil health and prevent nutrient depletion\n\n"
                + "Uses of High Potassium Fertilizer\n"
                + "• Promotes overall plant health and disease resistance\n"
                + "• Ideal for flowering plants, root vegetables, and fruiting plants\n"
                + "• Enhances drought tolerance\n"
                + "• Supports the development of strong, healthy plants";

        PotRec.setText(highPotassiumSoilInfo);
        PotRec.setTypeface(null, android.graphics.Typeface.BOLD);  // Make text bold

        return view;
    }
}