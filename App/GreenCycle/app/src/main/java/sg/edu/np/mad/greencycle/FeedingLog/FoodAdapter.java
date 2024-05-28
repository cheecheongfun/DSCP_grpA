package sg.edu.np.mad.greencycle.FeedingLog;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.Tank;
import sg.edu.np.mad.greencycle.R;

// Fionn, S10240073K
public class FoodAdapter extends RecyclerView.Adapter<FoodViewHolder> {
    private ArrayList<String> foodList;
    private ArrayList<Boolean> checkedStateArray;
    private RecyclerView recycler;
    private String foodType, foodString, amt, notes, date;
    private ArrayList<String> amtList;
    private ArrayList<String> customFoodList;
    User user;
    Log log;
    FirebaseDatabase database;
    DatabaseReference reference;
    Feeding feeding;
    int water;

    public FoodAdapter(ArrayList<String> foodList, RecyclerView recycler, String foodType, User user,  Log log, Feeding feeding) {
        this.foodList = foodList;
        checkedStateArray = new ArrayList<>();
        for (int i = 0; i < foodList.size(); i++) {
            checkedStateArray.add(false);
        }
        this.recycler = recycler;
        this.foodType = foodType;
        this.user = user;
        this.log = log;
        this.feeding = feeding;
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");
        this.amtList = new ArrayList<>();
        this.customFoodList = new ArrayList<>();
        for (int i = 0; i < foodList.size(); i++) {
            amtList.add("");
            customFoodList.add("");
        }
    }
    @Override
    public int getItemViewType(int position) {
        String food = foodList.get(position);
        return food.isEmpty() ? 1 : 0;
    }
    @Override
    public int getItemCount() {
        return foodList.size();
    }

    @Override
    public FoodViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        if (viewType == 1){
            return new FoodViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_card_edit, parent, false));
        }
        else return new FoodViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_card, parent, false));
    }

    @Override
    public void onBindViewHolder(FoodViewHolder holder, int position) {
        String food = foodList.get(position);
        android.util.Log.i(null, "adapter Log id and date: " + log.getLogId() + " " + log.getLogDate());

        if (getItemViewType(position)==1){
            android.util.Log.i(null, "in view 1");
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
                    holder.colon.setVisibility(View.VISIBLE);
                    holder.editAmt.setVisibility(View.VISIBLE);
                    customFoodList.set(position, editable.toString());
                }
            });
            holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkedStateArray.set(position, isChecked);
                holder.editCard.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                foodList.remove(position);
                notifyDataSetChanged();
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
        }
        else {
            holder.foodText.setText(food);
            holder.check.setChecked(checkedStateArray.get(position));
            foodString = food;
            holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkedStateArray.set(position, isChecked);
                String foodTextFormat = holder.itemView.getContext().getString(R.string.food_item);
                String formattedFoodText = String.format(foodTextFormat, foodString);
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
            // Set initial visibility based on checked state
            holder.editAmt.setVisibility(checkedStateArray.get(position) ? View.VISIBLE : View.GONE);
        }

    }
    public void addItem() {
        android.util.Log.i(null, "in addItem");
        foodList.add("");
        checkedStateArray.add(false);
        amtList.add("");
        customFoodList.add("");
        notifyItemInserted(foodList.size() - 1);
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
    public ArrayList<String> getSelectedFoods() {
        ArrayList<String> selectedFoods = new ArrayList<>();
        for (int i = 0; i < foodList.size(); i++) {
            if (checkedStateArray.get(i) || foodList.get(i).isEmpty()) {
                String food = foodList.get(i).isEmpty() ? customFoodList.get(i) : foodList.get(i);
                selectedFoods.add(food + " " + amtList.get(i));
            }
        }
        return selectedFoods;
    }
}
