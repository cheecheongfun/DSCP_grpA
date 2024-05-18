package sg.edu.np.mad.greencycle.Fragments.Resources;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.greencycle.R;

public class ResourceViewHolder  extends RecyclerView.ViewHolder{
    TextView resourceinfo, resourcetitle;
    ImageView resourceimage;

    public ResourceViewHolder(@NonNull View itemView) {
        super(itemView);
        resourceinfo = itemView.findViewById(R.id.resource_info);
        resourcetitle = itemView.findViewById(R.id.link);
        resourceimage = itemView.findViewById(R.id.resource_image);
    }
}

