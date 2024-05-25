package sg.edu.np.mad.greencycle.Goals;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.List;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.LiveData.Tank;
import sg.edu.np.mad.greencycle.R;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsViewHolder> {
    private List<Goals> GoalsList;
    private Context context;
    User user;
    Tank tank;
    FirebaseDatabase database;
    DatabaseReference reference;

    public GoalsAdapter(List<Goals> GoalsList, Context context, User user, Tank tank) {
        this.GoalsList = GoalsList;
        this.context = context;
        this.user = user;
        this.tank = tank;
    }

    @NonNull
    @Override
    public GoalsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goals, parent, false);
        return new GoalsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalsViewHolder holder, int position) {
        Goals goals = GoalsList.get(position);
        holder.goalstitle.setText(goals.getGoal_name());
        Log.v("user",user.getUsername());
        Log.v("tank",tank.getTankName());

        LocalDate today = LocalDate.now(); // Current date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate enddate= LocalDate.parse(goals.getEnd_date(), formatter);

        // Calculate the difference in days
        long daysBetween = ChronoUnit.DAYS.between(today,enddate);

        // Set the remaining days of Goals
        holder.goalsremaining.setText(String.valueOf(daysBetween) + " " +"Days Remaining");

        // Set the maximum value for the ProgressBar
        holder.progressBar.setMax(goals.getGoals_number());

        // Set OnClickListener for the delete ImageView
        holder.deleteImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Goals goalsToDelete = GoalsList.get(adapterPosition);

                    // Create and show a confirmation dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Confirm Delete");
                    builder.setMessage("Are you sure you want to remove this Goal?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Handle delete action here
                            // For example, you can delete the item from your data list
                            GoalsList.remove(adapterPosition);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference goalsRef = database.getReference()
                                    .child("users")
                                    .child(user.getUsername())
                                    .child("tanks")
                                    .child(String.valueOf(tank.getTankID()))
                                    .child("goals")
                                    .child(String.valueOf(goals.getGoalid()));
                            goalsRef.removeValue();

                            notifyDataSetChanged(); // Notify adapter of data change
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing or handle cancel action
                            dialog.dismiss(); // Dismiss the dialog
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });




    }

    @Override
    public int getItemCount() {
        return GoalsList.size();
    }


    public void setGoalsList(List<Goals> GoalList) {
        this.GoalsList = GoalsList;
        notifyDataSetChanged();
    }


}
