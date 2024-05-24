package sg.edu.np.mad.greencycle.FeedingLog;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.R;

public class LogAdapter extends RecyclerView.Adapter<LogViewHolder> {
    Context context;
    ArrayList<Log> feedingLog;
    String day,month;
    public LogAdapter(ArrayList<Log> feedingLog, Context context){
        this.feedingLog = feedingLog;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position){
        Log log = feedingLog.get(position);
        return position;
    }

    @Override
    public int getItemCount() { return feedingLog.size();}

    @Override
    public LogViewHolder onCreateViewHolder(
            ViewGroup parent,
            int viewType) {
        return new LogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feeding_card, parent, false));
    }
    @Override
    public void onBindViewHolder(
            LogViewHolder holder,
            int position){
        Log log = feedingLog.get(position);
        String[] dateParts = log.getLogDate().split("/");
        day = dateParts[0];
        month = getMonthName(Integer.parseInt(dateParts[1]));
        holder.dateFed.setText(day + " " + month);
    }

    public static String getMonthName(int month) {
        String[] monthNames = {
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };
        return monthNames[month - 1];
    }

}
