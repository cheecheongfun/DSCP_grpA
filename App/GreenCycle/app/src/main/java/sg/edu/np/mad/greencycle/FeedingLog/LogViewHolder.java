package sg.edu.np.mad.greencycle.FeedingLog;

import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;

import sg.edu.np.mad.greencycle.R;
// Fionn, S10240073K
public class LogViewHolder extends RecyclerView.ViewHolder {

    TextView dateFed, greenDesc, brownDesc;
    RelativeLayout card;
    public LogViewHolder(@NonNull View itemView) {
        super(itemView);
        dateFed = itemView.findViewById(R.id.dateFed);
        greenDesc = itemView.findViewById(R.id.greenDescription);
        brownDesc = itemView.findViewById(R.id.brownDescription);
        card = itemView.findViewById(R.id.feedCard);
    }
}
