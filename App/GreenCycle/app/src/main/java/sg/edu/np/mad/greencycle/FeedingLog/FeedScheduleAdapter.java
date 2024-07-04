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

        TextView scheduleName = convertView.findViewById(R.id.foodText2);
        TextView scheduleName1 = convertView.findViewById(R.id.foodText3);
        CheckBox check = convertView.findViewById(R.id.check);
        RadioButton radioButton = convertView.findViewById(R.id.radioButton);
        TextView delete = convertView.findViewById(R.id.delete);
        TextView viewDetails = convertView.findViewById(R.id.view);
        RelativeLayout scheduleDetails = convertView.findViewById(R.id.scheduleDetails);
        TextView greens = convertView.findViewById(R.id.greens);
        TextView browns = convertView.findViewById(R.id.browns);
        TextView water = convertView.findViewById(R.id.water);
        TextView notiText = convertView.findViewById(R.id.notiText);
        TextView dropdown = convertView.findViewById(R.id.dropdown);
        LinearLayout radioGroup = convertView.findViewById(R.id.radioNotiGroup);
        RadioButton radioNone = convertView.findViewById(R.id.radioNotiNone);
        RadioButton radioDay = convertView.findViewById(R.id.radioNotiDay);
        RadioButton radioDayBefore = convertView.findViewById(R.id.radioNotiDayBefore);
        EditText editText = convertView.findViewById(R.id.fourthEdit);

        scheduleName.setVisibility(View.GONE);
        check.setVisibility(View.GONE);
        scheduleName1.setText(feedSchedule.getScheduleName());
        scheduleName1.setVisibility(View.VISIBLE);
        radioButton.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        viewDetails.setVisibility(View.VISIBLE);
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
                notifyDataSetChanged();

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

        viewDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if drawable is view
                if (viewStatus.equals(false)) {
                    Log.i("view schedule details", "view on click : true");
                    scheduleDetails.setVisibility(View.VISIBLE);
                    viewDetails.setCompoundDrawablesWithIntrinsicBounds(R.drawable.disable_view, 0,0,0);
                    viewStatus = true;
                    dropdown.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (dropped.equals(false)) {
                                dropdown.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_keyboard_arrow_up_24, 0,0,0);
                                radioGroup.setVisibility(View.VISIBLE);
                                dropped = true;
                                radioNone.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        radioNone.setChecked(true);
                                        radioDay.setChecked(false);
                                        radioDayBefore.setChecked(false);
                                        notiType = "Don't notify";
                                        notiText.setText(notiType);
                                    }
                                });

                                radioDay.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        radioDay.setChecked(true);
                                        radioNone.setChecked(false);
                                        radioDayBefore.setChecked(false);
                                        notiType = "On the day itself";
                                        notiText.setText(notiType);
                                    }
                                });

                                radioDayBefore.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        radioDayBefore.setChecked(true);
                                        radioDay.setChecked(false);
                                        radioNone.setChecked(false);
                                        if (!editText.getText().toString().isEmpty()){
                                            notiType = editText.getText().toString() + " day before";
                                        }
                                        else {
                                            notiType = "1 day before";
                                        }
                                        notiText.setText(notiType);
                                    }
                                });
                                editText.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                    }

                                    @Override
                                    public void afterTextChanged(Editable editable) {
                                        radioDayBefore.setChecked(true);
                                        radioDay.setChecked(false);
                                        radioNone.setChecked(false);
                                        if (!editable.toString().isEmpty()){
                                            notiType = editable.toString() + " day before";
                                        }
                                        else {
                                            notiType = "1 day before";
                                        }
                                        notiText.setText(notiType);
                                        notiText.setTextColor(Color.parseColor("#FFFFFFFF"));
                                    }
                                });
                            }
                            else{
                                dropdown.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_keyboard_arrow_down_24, 0,0,0);
                                radioGroup.setVisibility(View.GONE);
                                notiText.setText("NotiType");
                                dropped = false;
                            }
                        }
                    });
                }
                else{
                    scheduleDetails.setVisibility(View.GONE);
                    viewDetails.setCompoundDrawablesWithIntrinsicBounds(R.drawable.view_icon, 0,0,0);
                    viewStatus = false;
                }
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
}


