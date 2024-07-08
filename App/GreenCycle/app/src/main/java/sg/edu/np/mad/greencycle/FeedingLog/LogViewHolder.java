package sg.edu.np.mad.greencycle.FeedingLog;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.greencycle.R;

public class LogViewHolder extends RecyclerView.ViewHolder{

    TextView foodText;
    CheckBox check;

    LogViewHolder(@NonNull View itemView) {
        super(itemView);
        foodText = itemView.findViewById(R.id.foodText2);
        check = itemView.findViewById(R.id.check);
    }
}
