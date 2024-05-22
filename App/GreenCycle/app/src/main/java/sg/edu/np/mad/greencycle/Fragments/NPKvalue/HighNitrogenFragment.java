package sg.edu.np.mad.greencycle.Fragments.NPKvalue;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sg.edu.np.mad.greencycle.R;


public class HighNitrogenFragment extends Fragment {
    TextView HighNitroRec;


    public HighNitrogenFragment() {

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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_high_nitrogen, container, false);
        HighNitroRec = view.findViewById(R.id.nitrogen_rec);
        String highNitrogenSoilInfo = "• Use a high nitrogen fertilizer with a higher N ratio (e.g., 3:1:1)\n"
                + "• Add green materials like grass clippings, kitchen scraps, and coffee grounds to your compost\n"
                + "• Avoid adding too many brown materials (carbon-rich) to maintain high nitrogen levels\n"
                + "• Regularly turn the compost to ensure even decomposition and aeration\n"
                + "• Keep the compost moist but not waterlogged\n"
                + "• Use vermicompost tea or diluted vermicompost leachate high in nitrogen as a natural fertilizer\n"
                + "• Test soil nitrogen levels and adjust as needed\n"
                + "• Rotate nitrogen-fixing crops like legumes to maintain soil health\n\n"
                + "Uses of High Nitrogen Fertilizer\n"
                + "• Promotes vigorous leafy growth\n"
                + "• Ideal for leafy vegetables, lawns, and plants in their vegetative stage\n"
                + "• Enhances the growth of foliage and stems\n"
                + "• Supports the development of strong, healthy plants";

        HighNitroRec.setText(highNitrogenSoilInfo);
        HighNitroRec.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }
}