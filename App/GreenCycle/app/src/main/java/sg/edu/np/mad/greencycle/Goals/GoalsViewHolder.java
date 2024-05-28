package sg.edu.np.mad.greencycle.Goals;
//Lee Jun Rong S10242663
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.greencycle.R;

public class GoalsViewHolder  extends RecyclerView.ViewHolder{
    TextView goalstitle, goalsremaining,progressText;
    ProgressBar progressBar;
    ImageView deleteImageview;

    public GoalsViewHolder(@NonNull View itemView) {
        super(itemView);
        goalstitle = itemView.findViewById(R.id.goal_title);
        goalsremaining = itemView.findViewById(R.id.days_remaining);
        progressBar = itemView.findViewById(R.id.progress_bar);
        deleteImageview = itemView.findViewById(R.id.delete);
        progressText = itemView.findViewById(R.id.progress_text);

    }
}


