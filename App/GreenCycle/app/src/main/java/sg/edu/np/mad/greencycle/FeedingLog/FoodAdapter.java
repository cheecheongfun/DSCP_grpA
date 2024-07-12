package sg.edu.np.mad.greencycle.FeedingLog;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.Classes.Food;
import sg.edu.np.mad.greencycle.FeedingLog.FoodViewHolder;
import sg.edu.np.mad.greencycle.R;

public class FoodAdapter extends RecyclerView.Adapter<FoodViewHolder> {
    private ArrayList<String> foodList;
    private ArrayList<Boolean> checkedStateArray;
    private ArrayList<Boolean> newFoodFlags;
    private RecyclerView recycler;
    private String foodType;
    private ArrayList<String> amtList, customFoodList;
    private ArrayList<Food> selectedFoods;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private Boolean edited = false;

    public FoodAdapter(ArrayList<String> foodList, RecyclerView recycler, String foodType, ArrayList<Food> selectedFoods) {
        this.foodList = foodList;
        this.checkedStateArray = new ArrayList<>();
        this.newFoodFlags = new ArrayList<>();
        for (int i = 0; i < foodList.size(); i++) {
            checkedStateArray.add(false);
            newFoodFlags.add(false);
        }
        this.recycler = recycler;
        this.foodType = foodType;
        this.database = FirebaseDatabase.getInstance();
        this.reference = database.getReference("users");
        this.amtList = new ArrayList<>();
        this.customFoodList = new ArrayList<>();
        for (int i = 0; i < foodList.size(); i++) {
            amtList.add("");
            customFoodList.add("");
        }
        this.selectedFoods = selectedFoods;

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (newFoodFlags.get(position)) {
            return 1; // New custom food item
        } else if (!selectedFoods.isEmpty() && position < selectedFoods.size()) {
            return 2; // Selected food item
        } else {
            return 0; // Food item from the database
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    @Override
    public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new FoodViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_card_edit, parent, false));
        } else if (viewType == 0) {
            return new FoodViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_card, parent, false));
        } else {
            return new FoodViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_new, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(FoodViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case 1:
                setupEditableViewHolder(holder, position);
                break;
            case 0:
                setupDefaultViewHolder(holder, position, foodList.get(position));
                break;
            case 2:
                setupSelectedViewHolder(holder, position);
                break;
        }
    }

    private void setupEditableViewHolder(FoodViewHolder holder, int position) {
        holder.editFood.setText(customFoodList.get(position)); // Set existing text if any
        holder.editFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                edited = true;
                holder.check.setChecked(true);
                String newFood = editable.toString();
                holder.colon.setVisibility(View.VISIBLE);
                holder.editAmt.setVisibility(View.VISIBLE);
                customFoodList.set(position, newFood);
                foodList.set(position, newFood);
                // No need to call notifyItemChanged(position) here
            }
        });
        holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedStateArray.set(position, isChecked);
            holder.editCard.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked || holder.editFood.getText().toString().isEmpty()) {
                foodList.remove(position);
                amtList.remove(position);
                customFoodList.remove(position);
                newFoodFlags.remove(position);
                checkedStateArray.remove(position);
                notifyDataSetChanged(); // Update RecyclerView
            }
        });
        holder.editAmt.setText(amtList.get(position)); // Set existing amount if any
        holder.editAmt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                amtList.set(position, editable.toString());
            }
        });
    }

    private void setupDefaultViewHolder(FoodViewHolder holder, int position, String food) {
        holder.foodText.setText(food);
        Log.i("default food adapter view", "food: " + food);
        holder.check.setChecked(checkedStateArray.get(position));
        holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedStateArray.set(position, isChecked);
            holder.colon.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            holder.editAmt.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updateItemList();
        });
        holder.editAmt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                amtList.set(position, editable.toString());
            }
        });
        holder.editAmt.setVisibility(checkedStateArray.get(position) ? View.VISIBLE : View.GONE);
    }

    private void setupSelectedViewHolder(FoodViewHolder holder, int position) {
        Food foodDetails = selectedFoods.get(position);
        holder.check.setChecked(true);
        holder.check.setVisibility(View.VISIBLE);
        holder.food.setText(foodDetails.getName() + ": " + foodDetails.getAmount() + " g");
        holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!holder.check.isChecked()) {
                selectedFoods.remove(foodDetails);
            } else {
                selectedFoods.add(foodDetails);
            }
        });
    }

    public void addItem() {
        if (!hasBlankCustomFood()) {
            foodList.add("");
            checkedStateArray.add(false);
            amtList.add("");
            customFoodList.add("");
            newFoodFlags.add(true); // Mark the new item as a new custom food
            notifyItemInserted(foodList.size() - 1);
        } else {
            // Optionally, show a message to the user that they need to fill the existing custom food before adding a new one
            Log.i("FoodAdapter", "Please fill in the existing custom food item before adding a new one.");
        }
    }

    private void updateItemList() {
        ArrayList<String> newList = new ArrayList<>();
        ArrayList<Boolean> newCheckedStates = new ArrayList<>();
        ArrayList<Boolean> newFoodFlagsUpdated = new ArrayList<>();

        for (int i = 0; i < foodList.size(); i++) {
            if (checkedStateArray.get(i)) {
                newList.add(foodList.get(i));
                newCheckedStates.add(true);
                newFoodFlagsUpdated.add(newFoodFlags.get(i));
            }
        }

        for (int i = 0; i < foodList.size(); i++) {
            if (!checkedStateArray.get(i)) {
                newList.add(foodList.get(i));
                newCheckedStates.add(false);
                newFoodFlagsUpdated.add(newFoodFlags.get(i));
            }
        }

        foodList = newList;
        checkedStateArray = newCheckedStates;
        newFoodFlags = newFoodFlagsUpdated;

        notifyDataSetChanged();
    }

    public ArrayList<Food> getSelectedFoods() {
        ArrayList<Food> finalSelectedFoods = new ArrayList<>();
        for (int i = 0; i < foodList.size(); i++) {
            if (checkedStateArray.get(i) || foodList.get(i).isEmpty()) {
                String foodName = foodList.get(i).isEmpty() ? customFoodList.get(i) : foodList.get(i);
                if (!foodName.isEmpty()) { // Ensure that foodName is not empty
                    double amount = amtList.get(i).isEmpty() ? 0 : Double.parseDouble(amtList.get(i));
                    finalSelectedFoods.add(new Food(foodName, amount));
                    Log.i("2nd If", "Food: " + foodName + amount);
                }
            }
        }
        finalSelectedFoods.addAll(selectedFoods);
        return finalSelectedFoods;
    }
    private boolean hasBlankCustomFood() {
        for (int i = 0; i < newFoodFlags.size(); i++) {
            if (newFoodFlags.get(i) && customFoodList.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

}