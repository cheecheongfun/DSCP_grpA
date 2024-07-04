package sg.edu.np.mad.greencycle.FeedingLog;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.Classes.FeedSchedule;
import sg.edu.np.mad.greencycle.Classes.Food;
import sg.edu.np.mad.greencycle.R;

public class LogAdapter extends RecyclerView.Adapter<LogViewHolder>{

    ArrayList<Food> foodList;
    ArrayList<FeedSchedule> scheduleList;
    String type;
    Context context;

    public LogAdapter(Context context, ArrayList<Food> foodList, String type, ArrayList<FeedSchedule> scheduleList) {
        this.foodList = foodList;
        this.type = type;
        this.scheduleList = scheduleList;
        this.context = context;
    }

    void setLog(ArrayList<Food> foodList, String type) {
        this.foodList = foodList;
        this.type = type;
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position){
        if (type.equals("green")){
            return 0;
        }
        else if (type.equals("brown")){
            return 1;
        }
        else return 2;
    }
    @Override
        public LogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0 || viewType ==1){
            return new LogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_new, parent, false));
        }
        if (viewType == 2){
            return new LogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule, parent, false));
        }
        return new LogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_new, parent, false));
    }

    @Override
    public void onBindViewHolder(LogViewHolder holder, int position) {
        if (getItemViewType(position) == 0 || getItemViewType(position) == 1){
            // Green Food + Brown Food
            Food food = foodList.get(position);
            holder.foodText.setText("- " + food.getName() + " " + food.getAmount() + " " + food.getUnit());
            holder.foodText.setTextColor(ContextCompat.getColor(context, R.color.textColour));
        }
        else if (getItemViewType(position) == 2){
            // Schedule
            FeedSchedule feedSchedule = scheduleList.get(position);
            holder.foodText.setText(feedSchedule.getScheduleName());
        }
        else Log.i(null, "None View");
    }

    @Override
    public int getItemCount() {
        if (foodList == null) return scheduleList.size();
        else return foodList.size();
    }
}
