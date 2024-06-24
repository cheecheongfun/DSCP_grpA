package sg.edu.np.mad.greencycle.FeedingLog;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.greencycle.R;

public class LogViewHolderNew extends RecyclerView.ViewHolder{

    TextView foodText;
    CheckBox check;

    LogViewHolderNew(@NonNull View itemView) {
        super(itemView);
        foodText = itemView.findViewById(R.id.foodText2);
        check = itemView.findViewById(R.id.check);
    }
}
