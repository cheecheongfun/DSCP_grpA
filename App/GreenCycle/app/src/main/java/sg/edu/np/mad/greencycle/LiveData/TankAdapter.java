package sg.edu.np.mad.greencycle.LiveData;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.R;

public class TankAdapter extends RecyclerView.Adapter<TankViewHolder>{
    Context context;
    ArrayList<Tank> tankList;
    public TextView noTankText;
    public TankAdapter(ArrayList<Tank> tankList, Context context){
        this.tankList=tankList;
        this.context = context;
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
    public void updateEmptyView(){
        if (getItemCount() == 0) {
            noTankText.setVisibility(VISIBLE);
        }
        else noTankText.setVisibility(INVISIBLE);
    }
    @Override
    public TankViewHolder onCreateViewHolder(
            ViewGroup parent,
            int viewType) {
        return new TankViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tank_selection, parent, false));
    }
    @Override
    public void onBindViewHolder(
            TankViewHolder holder,
            int position) {
        Tank tank = tankList.get(position);
        holder.tankName.setText(tank.getTankName());
        holder.numberOfWorms.setText(tank.getNumberOfWorms());
        holder.dateCreated.setText(tank.getDateCreated());
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewTank = new Intent(context, TankSelection.class);
                Bundle info = new Bundle();
                info.putParcelable("Tank", tank);
            }
        });
    }

}
