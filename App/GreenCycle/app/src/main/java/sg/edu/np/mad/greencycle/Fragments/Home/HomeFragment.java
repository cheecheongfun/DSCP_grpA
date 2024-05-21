package sg.edu.np.mad.greencycle.Fragments.Home;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import sg.edu.np.mad.greencycle.Fragments.Home.NPKvalue.npk_value;
import sg.edu.np.mad.greencycle.Fragments.MainActivity;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.StartUp.LoginPage;

public class HomeFragment extends Fragment {

   ImageButton npkButton;


    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        npkButton = view.findViewById(R.id.soilTypeButton);
        npkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), npk_value.class);
                startActivity(intent);
            }
        });

        return view;
    }

}