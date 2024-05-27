package sg.edu.np.mad.greencycle.Goals;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class Goals_Notification {

    public void updateGoalsCompletion(User user, Context context) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference tanksRef = database.child("users").child(user.getUsername()).child("tanks");

        tanksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tankSnapshot : dataSnapshot.getChildren()) {
                    String tankId = tankSnapshot.getKey(); // Get tank ID
                    String tankName = tankSnapshot.child("tankName").getValue(String.class); // Get tank name
                    int numberOfWorms = tankSnapshot.child("numberOfWorms").getValue(Integer.class);
                    DatabaseReference tankRef = tanksRef.child(tankId);

                    for (DataSnapshot goalSnapshot : tankSnapshot.child("goals").getChildren()) {
                        DatabaseReference goalRef = tankRef.child("goals").child(goalSnapshot.getKey());
                        int goalsNumber = goalSnapshot.child("goals_number").getValue(Integer.class);
                        String goals_completion = goalSnapshot.child("goals_completion").getValue(String.class);
                        String goals_name = goalSnapshot.child("goal_name").getValue(String.class);
                        String goals_enddate = goalSnapshot.child("end_date").getValue(String.class);

                        //Goal Expire Logic
                        LocalDate today = LocalDate.now(); // Current date
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate enddate = LocalDate.parse(goals_enddate, formatter);

                        // Goals Completion Logic
                        if (goals_name.contains("worm") && goals_completion.contains("Incomplete")) {

                            if (enddate.isBefore(today)) {
                                goalRef.child("goals_completion").setValue("Expired");
                                String text = "Tank " + tankName + "'s desired worms population of " + goalsNumber + " goal has been achieved!";
                                showCustomToast(text,4,context);
                            }

                            else {

                                if (numberOfWorms >= goalsNumber) {
                                    // Update goals_completion, include tank ID and tank name
                                    goalRef.child("goals_completion").setValue("Complete");
                                    String text = "Tank " + tankName + "'s desired worms population of " + goalsNumber + " has been achieved!";
                                    showCustomToast(text, 1, context);
                                }
                            }
                        }

                        else if (goals_name.contains("Compost")&& goals_completion.contains("Incomplete")) {

                            //currently placeholder value
                            int compost = 0;

                            if (enddate.isBefore(today)) {
                                goalRef.child("goals_completion").setValue("Expired");
                                String text = "Tank " + tankName + "'s goal of producing " + goalsNumber + " grams of Compost has expired!";
                                showCustomToast(text,5,context);
                            }

                            else {

                                if (compost >= goalsNumber) {
                                    // Update goals_completion, include tank ID and tank name
                                    goalRef.child("goals_completion").setValue("Complete");
                                    String text = "Tank " + tankName + " has produced the desired amount of " + goalsNumber + " grams of Compost!";
                                    showCustomToast(text, 2, context);
                                }
                            }

                        }

                        else if (goals_name.contains("waste")&& goals_completion.contains("Incomplete")){

                            //currently placeholder value
                            int waste = 0;

                            if (enddate.isBefore(today)) {
                                goalRef.child("goals_completion").setValue("Expired");
                                String text = "Tank " + tankName + "'s goal of reducing' " + goalsNumber + " grams of waste has expired!";
                                showCustomToast(text,6,context);
                            }
                            else {

                                if (waste >= goalsNumber) {
                                    // Update goals_completion, include tank ID and tank name
                                    goalRef.child("goals_completion").setValue("Complete");
                                    String text = "Tank " + tankName + " have helped reduce the desired waste of " + goalsNumber + " grams!";
                                    showCustomToast(text, 3, context);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void showCustomToast(String text, int choice, Context context) {
        // Inflate custom layout for the toast
        LayoutInflater inflater = LayoutInflater.from(context);
        View customToastView = inflater.inflate(R.layout.toast, null);


        // Set text for the custom toast
        TextView toastTextView = customToastView.findViewById(R.id.toast_text);
        ImageView toastImageView = customToastView.findViewById(R.id.toast_image);
        LinearLayout main = customToastView.findViewById(R.id.main);

        if (choice == 1) {
            toastImageView.setImageResource(R.drawable.thumbs_up_worm);
        } else if (choice == 2) {
            toastImageView.setImageResource(R.drawable.compost);
        } else if (choice == 3){
            toastImageView.setImageResource(R.drawable.food_waste);
        } else if (choice ==4){

        }else{
            toastImageView.setImageResource(R.drawable.thumbs_down);
            toastTextView.setTextColor(context.getResources().getColor(R.color.red ));
        }


        toastTextView.setText(text);

        // Create and show the custom toast
        Toast customToast = new Toast(context);
        customToast.setDuration(Toast.LENGTH_LONG);
        customToast.setView(customToastView);

        customToast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);
        customToast.show();
    }

}
