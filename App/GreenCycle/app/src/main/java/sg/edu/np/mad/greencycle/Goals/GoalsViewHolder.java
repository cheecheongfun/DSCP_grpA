package sg.edu.np.mad.greencycle.Goals;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.greencycle.R;

public class GoalsViewHolder  extends RecyclerView.ViewHolder{
    TextView goalstitle, goalsremaining;
    ProgressBar progressBar;

    public GoalsViewHolder(@NonNull View itemView) {
        super(itemView);
        goalstitle = itemView.findViewById(R.id.goal_title);
        goalsremaining = itemView.findViewById(R.id.days_remaining);
        progressBar = itemView.findViewById(R.id.progress_bar);
    }
}


