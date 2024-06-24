package sg.edu.np.mad.greencycle.FeedingLog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.Classes.FeedSchedule;
import sg.edu.np.mad.greencycle.Classes.Food;
import sg.edu.np.mad.greencycle.R;

public class LogAdapterNew extends RecyclerView.Adapter<LogViewHolderNew>{

    ArrayList<Food> foodList;
    ArrayList<FeedSchedule> scheduleList;
    String type;

    public LogAdapterNew(ArrayList<Food> foodList, String type, ArrayList<FeedSchedule> scheduleList) {
        this.foodList = foodList;
        this.type = type;
        this.scheduleList = scheduleList;
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
        else if (type.equals("schedule")){
            return 2;
        }
        else return 3;
    }
    @Override
        public LogViewHolderNew onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0 || viewType ==1){
            return new LogViewHolderNew(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_new, parent, false));
        }
        if (viewType == 2){
            return new LogViewHolderNew(LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule, parent, false));
        }
        return new LogViewHolderNew(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_new, parent, false));
    }

    @Override
    public void onBindViewHolder(LogViewHolderNew holder, int position) {
        if (getItemViewType(position) == 0){
            // Green Food
            Food food = foodList.get(position);
            holder.foodText.setText(food.getName() + " " + food.getAmount() + food.getUnit());
        }
        else if (getItemViewType(position) == 1){
            // Brown Food
            Food food = foodList.get(position);
            holder.foodText.setText(food.getName() + " " + food.getAmount() + food.getUnit());
        }
        else if (getItemViewType(position) == 2){
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
