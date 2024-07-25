package sg.edu.np.mad.greencycle.TankSelection;

import sg.edu.np.mad.greencycle.Analytics.Analytics;
import sg.edu.np.mad.greencycle.Classes.User;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import sg.edu.np.mad.greencycle.FeedingLog.Feeding;
import sg.edu.np.mad.greencycle.Goals.ViewGoals;
import sg.edu.np.mad.greencycle.LiveData.LiveData;
import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.R;
// Fionn, S10240073K
public class TankAdapter extends RecyclerView.Adapter<TankViewHolder>{
    Context context;
    ArrayList<Tank> tankList;
    String purpose;
    FirebaseDatabase database;
    DatabaseReference reference;
    User user;
    public TankAdapter(ArrayList<Tank> tankList, Context context, User user, String purpose){
        this.tankList=tankList;
        this.context = context;
        this.user = user;
        this.purpose = purpose;
    }
    @Override
    public int getItemViewType(int position)
    {
        Tank tank = tankList.get(position);
        return position;
    }
    @Override
    public int getItemCount() {
        return tankList.size();
    }
    @Override
    public TankViewHolder onCreateViewHolder(
            ViewGroup parent,
            int viewType) {
        return new TankViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tank, parent, false));
    }
    @Override
    public void onBindViewHolder(
            TankViewHolder holder,
            int position) {
        Tank tank = tankList.get(position);
        holder.tankName.setText(tank.getTankName());
        holder.numberOfWorms.setText("Number of worms: " + String.valueOf(tank.getNumberOfWorms()));
        holder.dateCreated.setText("Date Started: " +tank.getDateCreated());
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(null, "Tank ID: " + tank.getTankID());
                if (purpose.equals("LiveData")){
                    Intent viewTank = new Intent(context, LiveData.class);
                    Bundle info = new Bundle();
                    info.putParcelable("tank", tank);
                    info.putParcelable("user", user);
                    viewTank.putExtras(info);
                    context.startActivity(viewTank);
                } else if (purpose.equals("Feeding")) {
                    Intent feed = new Intent(context, Feeding.class);
                    Bundle info = new Bundle();
                    info.putParcelable("tank", tank);
                    info.putParcelable("user", user);
                    feed.putExtras(info);
                    context.startActivity(feed);
                }else if (purpose.equals("Analytics")) {
                    Intent feed = new Intent(context, Analytics.class);
                    Bundle info = new Bundle();
                    info.putParcelable("tank", tank);
                    info.putParcelable("user", user);
                    feed.putExtras(info);
                    context.startActivity(feed);
                }else if (purpose.equals("GOALS")) {
                    Intent feed = new Intent(context, ViewGoals.class);
                    Bundle info = new Bundle();
                    info.putParcelable("tank", tank);
                    info.putParcelable("user", user);
                    feed.putExtras(info);
                    context.startActivity(feed);
                }
            }
        });
        holder.card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.8);
                Dialog edit = new Dialog(context, R.style.CustomDialog);
                edit.setContentView(R.layout.tank_edit_dialog);
                edit.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                edit.setCancelable(true);


                edit.show();
                EditText name = edit.findViewById(R.id.etName);
                EditText worm = edit.findViewById(R.id.etWorm);

                name.setText(tank.getTankName());
                name.requestFocus();
                worm.setText(String.valueOf(tank.getNumberOfWorms()));

                Button save = edit.findViewById(R.id.save);
                Button delete = edit.findViewById(R.id.delete);

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("add", "Worm: " + worm.getText().toString() + " Name: " + name.getText().toString());
                        String tankName = name.getText().toString();
                        if (tankName.isEmpty()) {
                            tank.setTankName(tank.getTankName());
                            name.setText(tank.getTankName());
                        } else tank.setTankName(tankName);

                        String wormNo = worm.getText().toString();
                        if (wormNo.isEmpty()) {
                            tank.setNumberOfWorms(tank.getNumberOfWorms());
                            worm.setText(String.valueOf(tank.getNumberOfWorms()));
                        } else tank.setNumberOfWorms(Integer.parseInt(wormNo));

                        int id = tank.getTankID();
                        database = FirebaseDatabase.getInstance();
                        reference = database.getReference("users");

                        for (int i = 0; i < user.getTanks().size(); i++) {
                            Tank fbtank = user.getTanks().get(i);
                            if (fbtank.getTankID() == id) {
                                user.getTanks().set(i, tank);
                                user.setTanks(user.getTanks());
                                break;
                            }
                        }

                        Log.i(null, "after set tank");
                        reference.child(user.getUsername()).setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("FirebaseUpdate", "Feed Schedule: "+ user.getTanks().size());
                                        notifyDataSetChanged();
                                        edit.dismiss();
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

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tankList.remove(tank);

                        int id = tank.getTankID();
                        database = FirebaseDatabase.getInstance();
                        reference = database.getReference("users");

                        for (int i = 0; i < user.getTanks().size(); i++) {
                            Tank tank = user.getTanks().get(i);
                            if (tank.getTankID() == id) {
                                user.getTanks().remove(id);
                                user.setTanks(user.getTanks());
                                break;
                            }
                        }

                        Log.i(null, "after set tank");
                        reference.child(user.getUsername()).setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("FirebaseUpdate", "Feed Schedule: "+ user.getTanks().size());
                                        notifyDataSetChanged();
                                        edit.dismiss();
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
                return false;
            }
        });
    }

}
