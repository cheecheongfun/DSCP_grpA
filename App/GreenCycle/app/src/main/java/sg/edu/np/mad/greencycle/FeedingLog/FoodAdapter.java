package sg.edu.np.mad.greencycle.FeedingLog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.R;

public class FoodAdapter extends RecyclerView.Adapter<FoodViewHolder> {
    private ArrayList<String> foodList;
    private ArrayList<Boolean> checkedStateArray;
    private RecyclerView recycler;

    public FoodAdapter(ArrayList<String> foodList, RecyclerView recycler) {
        this.foodList = foodList;
        checkedStateArray = new ArrayList<>();
        for (int i = 0; i < foodList.size(); i++) {
            checkedStateArray.add(false);
        }
        this.recycler = recycler;
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    @Override
    public FoodViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        return new FoodViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_card, parent, false));
    }

    @Override
    public void onBindViewHolder(FoodViewHolder holder, int position) {
        String food = foodList.get(position);

        holder.foodText.setText(food);
        holder.check.setChecked(checkedStateArray.get(position));

        holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedStateArray.set(position, isChecked);
            String foodTextFormat = holder.itemView.getContext().getString(R.string.food_item);
            String formattedFoodText = String.format(foodTextFormat, food);
            holder.foodText.setText(formattedFoodText);
            updateItemList();
            holder.editAmt.setHint("input amount without unit");
            holder.editAmt.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Set initial visibility based on checked state
        holder.editAmt.setVisibility(checkedStateArray.get(position) ? View.VISIBLE : View.GONE);
    }

    private void updateItemList() {
        ArrayList<String> newList = new ArrayList<>();
        ArrayList<Boolean> newCheckedStates = new ArrayList<>();

        // Separate checked and unchecked items
        for (int i = 0; i < foodList.size(); i++) {
            if (checkedStateArray.get(i)) {
                newList.add(foodList.get(i));
                newCheckedStates.add(true);
            }
        }

        for (int i = 0; i < foodList.size(); i++) {
            if (!checkedStateArray.get(i)) {
                newList.add(foodList.get(i));
                newCheckedStates.add(false);
            }
        }

        foodList = newList;
        checkedStateArray = newCheckedStates;

        recycler.post(() -> notifyDataSetChanged());

    }
}
