package sg.edu.np.mad.greencycle.FeedingLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sg.edu.np.mad.greencycle.Classes.FeedSchedule;
import sg.edu.np.mad.greencycle.Classes.Food;
import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class FeedScheduleAdapter extends ArrayAdapter<FeedSchedule> {
    private Context context;
    private ArrayList<FeedSchedule> feedSchedules, original;
    private int selectedIndex = 0;
    FirebaseDatabase database;
    DatabaseReference reference;
    User user;
    Tank newTank;
    Boolean viewStatus = false;
    Boolean dropped = false;
    String notiType;

    public FeedScheduleAdapter(Context context, ArrayList<FeedSchedule> feedSchedules, User user, Tank tank, ArrayList<FeedSchedule> original) {
        super(context, 0, feedSchedules);
        this.context = context;
        this.feedSchedules = feedSchedules;
        this.user = user;
        this.newTank = tank;
        this.original = original;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeedSchedule feedSchedule = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.schedule, parent, false);
        }

        TextView scheduleName1 = convertView.findViewById(R.id.foodText3);
        RadioButton radioButton = convertView.findViewById(R.id.radioButton);
        TextView delete = convertView.findViewById(R.id.delete);
        TextView greens = convertView.findViewById(R.id.greens);
        TextView browns = convertView.findViewById(R.id.browns);

        scheduleName1.setText(feedSchedule.getScheduleName());

        ArrayList<Food> green = new ArrayList<>();
        if (feedSchedule.getGreenFood() == null || feedSchedule.getGreenFood().isEmpty()){
            greens.setVisibility(View.GONE);
        }
        else {
            green = feedSchedule.getGreenFood();
            greens.setVisibility(View.VISIBLE);
            greens.setText("Greens: " + getFoodDescription(green, 27));
        }

        ArrayList<Food> brown = new ArrayList<>();
        if (feedSchedule.getBrownFood() == null || feedSchedule.getBrownFood().isEmpty()){
            browns.setVisibility(View.GONE);
        }
        else {
            brown = feedSchedule.getBrownFood();
            browns.setVisibility(View.VISIBLE);
            browns.setText("Browns: " + getFoodDescription(brown, 27));
        }

        delete.setVisibility(View.VISIBLE);
        // Manage selection state
        radioButton.setChecked(position == selectedIndex);
        radioButton.setOnClickListener(v -> {
            selectedIndex = position;
            notifyDataSetChanged();
        });

        // Manage deletion of schedule
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("delete schedule", "delete clicked");

                original.remove(feedSchedule);
                feedSchedules.remove(feedSchedule);
                newTank.setFeedSchedule(original);

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                for (int i = 0; i < user.getTanks().size(); i++) {
                    Tank tank = user.getTanks().get(i);
                    if (tank.getTankID() == newTank.getTankID()) {
                        user.getTanks().set(i, newTank);
                        user.setTanks(user.getTanks());
                        break;
                    }
                }

                Log.i(null, "after set tank");
                reference.child(user.getUsername()).setValue(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("FirebaseUpdate", "Feed Schedule: "+ user.getTanks().get(0).getFeedSchedule().size());
                                // Dismiss the dialog or close the activity
                                notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("FirebaseUpdate", "Failed to update user tank list.", e);
                            }
                        });
            }
        });


        return convertView;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public String getSelectedNotificationType(){
        return notiType;
    }

    public FeedSchedule getNewFeedSchedule() {
        FeedSchedule feedSchedule = feedSchedules.get(selectedIndex);
        String name = feedSchedule.getScheduleName();
        for (FeedSchedule schedule: original){
            if (schedule.getScheduleName().equals(name)){
                Log.i("getNewFeedSchedule", "in original: " + original.contains(schedule));
                Log.i("getNewFeedSchedule", "extracted schedule: " + schedule);
                return schedule;
            }
        }
        return null;
    }
    private String getFoodDescription(ArrayList<Food> foodList, int maxLength) {
        StringBuilder description = new StringBuilder();
        for (Food food : foodList) {
            if (description.length() > 0) {
                description.append(", ");
            }
            description.append(food.getName() + " " + food.getAmount());
        }

        if (description.length() > maxLength) {
            return description.substring(0, maxLength - 3) + "...";
        } else {
            return description.toString();
        }
    }
}


