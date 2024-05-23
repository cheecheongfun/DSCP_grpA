package sg.edu.np.mad.greencycle.NPKvalue;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sg.edu.np.mad.greencycle.R;


public class BalancedFragment extends Fragment {
    TextView BalRec;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EdgeToEdge.enable(requireActivity());
        // Inflate the layout for this fragment
        View view;
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_balanced, container, false);
        BalRec = view.findViewById(R.id.balanced_rec);
        String balancedSoilInfo = "1:1:1\n"
                + "• Maintain a pH level between 6.0 and 7.0\n"
                + "• Add a mix of green (nitrogen-rich) and brown (carbon-rich) materials to your compost\n"
                + "• Rotate crops to maintain soil health and prevent nutrient depletion\n\n"
                + "Uses of a Balanced Fertilizer\n"
                + "• Promotes even growth of roots, stems, and leaves\n"
                + "• Ideal for general garden use and all-purpose feeding\n"
                + "• Supports healthy flowering and fruiting\n"
                + "• Enhances soil microbial activity and overall soil health";

        BalRec.setText(balancedSoilInfo);
        BalRec.setTypeface(null, android.graphics.Typeface.BOLD);

        return view;
    }
}
