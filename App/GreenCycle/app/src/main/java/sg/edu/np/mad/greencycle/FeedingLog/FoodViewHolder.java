package sg.edu.np.mad.greencycle.FeedingLog;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.greencycle.R;

public class FoodViewHolder extends RecyclerView.ViewHolder{

    TextView foodText;
    CheckBox check;
    EditText editAmt, editFood;

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);
        foodText = itemView.findViewById(R.id.foodText);
        check = itemView.findViewById(R.id.check);
        editAmt = itemView.findViewById(R.id.editAmt);
        editFood = itemView.findViewById(R.id.editFood);
    }
}
