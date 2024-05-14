package sg.edu.np.mad.greencycle.LiveData;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.greencycle.R;

public class TankViewHolder  extends RecyclerView.ViewHolder{
    TextView tankName, dateCreated, numberOfWorms;
    ImageView tankImage;
    RelativeLayout card;

    public TankViewHolder(@NonNull View itemView) {
        super(itemView);
        tankName = itemView.findViewById(R.id.tankName);
        dateCreated = itemView.findViewById(R.id.dateCreatedText);
        numberOfWorms = itemView.findViewById(R.id.numberOfWormsText);
        tankImage = itemView.findViewById(R.id.tankImage);
        card = itemView.findViewById(R.id.tankCard);
    }
}
