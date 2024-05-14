package sg.edu.np.mad.greencycle.LiveData;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.R;

public class TankSelection extends AppCompatActivity {

    private TextView noTankText;
    RecyclerView tankRecycler;
    ArrayList<Tank> tankList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tank_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tankRecyclerView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tankRecycler = findViewById(R.id.tankList);
        noTankText = findViewById(R.id.noTankText);
        FloatingActionButton add = findViewById(R.id.addTank);

        tankList = new ArrayList<>();
        refreshTaskRecyclerView();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
    public void refreshTaskRecyclerView(){
        TankAdapter mAdapter = new TankAdapter(tankList, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        tankRecycler.setLayoutManager(mLayoutManager);
        mAdapter.noTankText = noTankText;
        if (tankList == null || tankList.size()==0){
            mAdapter.updateEmptyView();
        }
        tankRecycler.setAdapter(mAdapter);
    }
}