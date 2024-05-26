package sg.edu.np.mad.greencycle.FeedingLog;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class FoodAdapter extends RecyclerView.Adapter<FoodViewHolder> {
    private ArrayList<String> foodList;
    private ArrayList<Boolean> checkedStateArray;
    private RecyclerView recycler;
    private String foodType, adding, foodAll, foodString;
    User user;
    Log log;

    public FoodAdapter(ArrayList<String> foodList, RecyclerView recycler, String foodType, User user,  Log log) {
        this.foodList = foodList;
        checkedStateArray = new ArrayList<>();
        for (int i = 0; i < foodList.size(); i++) {
            checkedStateArray.add(false);
        }
        this.recycler = recycler;
        this.foodType = foodType;
        this.user = user;
        this.log = log;
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
        android.util.Log.i(null, "adapter Log id: " + log.getLogId() + log.getLogDate());
        if (adding.equals("yes")){
            holder.foodText.setVisibility(View.INVISIBLE);
            holder.editFood.setVisibility(View.VISIBLE);
            holder.check.setChecked(true);

            holder.editFood.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }
                @Override
                public void afterTextChanged(Editable editable) {
                    foodString = holder.editFood.getText().toString().trim();
                    holder.editAmt.setHint("amount");
                    holder.editAmt.setVisibility(View.VISIBLE);
                }
            });

        }
        else {
            holder.foodText.setText(food);
            holder.check.setChecked(checkedStateArray.get(position));
            foodString = food;
        }
        holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedStateArray.set(position, isChecked);
            String foodTextFormat = holder.itemView.getContext().getString(R.string.food_item);
            String formattedFoodText = String.format(foodTextFormat, foodString);
            holder.foodText.setText(formattedFoodText);
            updateItemList();
            holder.editAmt.setHint("amount");
            holder.editAmt.setVisibility(isChecked ? View.VISIBLE : View.GONE);

            String foodAmt = holder.editAmt.getText().toString().trim();
            if (foodType == "green"){
                String foodAll = food + " " + foodAmt;
                log.getGreens().add(foodAll);
            }
            else if (foodType == "brown"){
                String foodAll = food + " " + foodAmt;
                log.getBrowns().add(foodAll);
            }
        });
        // Set initial visibility based on checked state
        holder.editAmt.setVisibility(checkedStateArray.get(position) ? View.VISIBLE : View.GONE);
    }
    public void addItem() {
        foodList.add("");
        checkedStateArray.add(false);
        notifyItemInserted(foodList.size() - 1);
        android.util.Log.i(null, "in add item");
        adding = "yes";
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
