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
    private RecyclerView recycler;
    private String foodType;
    private ArrayList<String> amtList, customFoodList;
    private ArrayList<Food> selectedFoods;
    FirebaseDatabase database;
    DatabaseReference reference;

    public FoodAdapter(ArrayList<String> foodList, RecyclerView recycler, String foodType, ArrayList<Food> selectedFoods) {
        this.foodList = foodList;
        this.checkedStateArray = new ArrayList<>();
        for (int i = 0; i < foodList.size(); i++) {
            checkedStateArray.add(false);
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
    }

    @Override
    public int getItemViewType(int position) {
        String food;
        if (!selectedFoods.isEmpty() && position < selectedFoods.size()){
            return 2;
        }
        else {
            if (!customFoodList.get(position).isEmpty() && customFoodList.size() == 1){
                food = customFoodList.get(position);
            }
            else food = foodList.get(position);
            return food.isEmpty() ? 1 : 0;
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
        } else if (viewType == 0){
            return new FoodViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_card, parent, false));
        }
        else return new FoodViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_new, parent, false));
    }

    @Override
    public void onBindViewHolder(FoodViewHolder holder, int position) {
        String food = foodList.get(position);

        if (getItemViewType(position) == 1) {
            android.util.Log.i(null, "in view 1");
            // editing food or adding food
            holder.editFood.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    holder.check.setChecked(true);
                    String newFood = editable.toString();
                    holder.colon.setVisibility(View.VISIBLE);
                    holder.editAmt.setVisibility(View.VISIBLE);
                    customFoodList.set(position, newFood);
                }
            });
            Log.i(null, "empty edit: " +  holder.editFood.getText().toString().isEmpty());
            holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkedStateArray.set(position, isChecked);
                holder.editCard.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (!isChecked || holder.editFood.getText().toString().isEmpty()) {
                    foodList.remove(position);
                    amtList.remove(position);
                    customFoodList.remove(position);
                    checkedStateArray.remove(position);
                    notifyDataSetChanged();
                }
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
        } else if (getItemViewType(position) == 0) {
            Log.i(null, "in view 0");
            // food is present in database
            holder.foodText.setText(food);
            holder.check.setChecked(checkedStateArray.get(position));
            holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkedStateArray.set(position, isChecked);
                String foodTextFormat = holder.itemView.getContext().getString(R.string.food_item);
                String formattedFoodText = String.format(foodTextFormat, food);
                holder.foodText.setText(formattedFoodText);
                updateItemList();
                holder.colon.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                holder.editAmt.setVisibility(isChecked ? View.VISIBLE : View.GONE);
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
        else {
            Log.i(null, "in view 2");
            Food foodDetails = selectedFoods.get(position);
            holder.check.setChecked(true);
            holder.check.setVisibility(View.VISIBLE);
            holder.food.setText(foodDetails.getName() + ": " + foodDetails.getAmount() + " g");
            holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!holder.check.isChecked()){
                    selectedFoods.remove(foodDetails);
                    Log.i(null, "selectedFoods in view 2 remove" + selectedFoods.size());
                }
                else {
                    selectedFoods.add(foodDetails);
                    Log.i(null, "selectedFoods in view 2 add " + selectedFoods.size());
                }
            });
        }
    }

    public void addItem() {
        android.util.Log.i(null, "in addItem");
        ArrayList<String> empty = new ArrayList<>();
        for (String f : customFoodList){
            if (f.isEmpty()){
                empty.add(f);
            }
        }
        if (empty.size() <= 1){
            foodList.add("");
            checkedStateArray.add(false);
            amtList.add("");
            customFoodList.add("");
            notifyItemInserted(foodList.size() - 1);
        }
    }



    private void updateItemList() {
        ArrayList<String> newList = new ArrayList<>();
        ArrayList<Boolean> newCheckedStates = new ArrayList<>();

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
}
