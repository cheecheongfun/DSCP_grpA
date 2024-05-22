package sg.edu.np.mad.greencycle.Fragments.NPKvalue;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sg.edu.np.mad.greencycle.R;


public class HighPhosphorousFragment extends Fragment {
    TextView PhosRec;



    public HighPhosphorousFragment() {
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
        View view;
        view = inflater.inflate(R.layout.fragment_high_phosphorous, container, false);
        PhosRec = view.findViewById(R.id.phos_rec );
        String highPhosphorusSoilInfo = "• 1:3:1\n"
                + "• Add bone meal, rock phosphate, and composted manure to your vermicompost\n"
                + "• Include fish meal and crab shells which are high in phosphorus\n"
                + "• Avoid adding too many green materials (nitrogen-rich) to maintain high phosphorus levels\n"
                + "• Use vermicompost tea or diluted vermicompost leachate high in phosphorus as a natural fertilizer\n"
                + "• Rotate crops to maintain soil health and prevent nutrient depletion\n\n"
                + "Uses of High Phosphorus Fertilizer\n"
                + "• Promotes strong root development\n"
                + "• Ideal for root vegetables, flowers, and fruiting plants\n"
                + "• Enhances blooming and fruiting\n"
                + "• Supports the development of strong, healthy plants";

        // Make the text bold and set a larger font size
        PhosRec.setText(highPhosphorusSoilInfo);
        PhosRec.setTypeface(null, android.graphics.Typeface.BOLD);  // Make text bold

        return view;
    }
}