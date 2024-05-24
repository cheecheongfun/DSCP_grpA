package sg.edu.np.mad.greencycle.LiveData;

import sg.edu.np.mad.greencycle.Analytics.Analytics;
import sg.edu.np.mad.greencycle.Classes.User;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.FeedingLog.Feeding;
import sg.edu.np.mad.greencycle.R;

public class TankAdapter extends RecyclerView.Adapter<TankViewHolder>{
    Context context;
    ArrayList<Tank> tankList;
    String purpose;
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
                }
            }
        });
    }

}
