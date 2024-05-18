package sg.edu.np.mad.greencycle.Fragments.Resources;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sg.edu.np.mad.greencycle.R;

public class ResourceAdapter extends RecyclerView.Adapter<ResourceViewHolder> {
    private List<Resource> resourceList;
    private Context context;

    public ResourceAdapter(List<Resource> resourceList, Context context) {
        this.resourceList = resourceList;
        this.context = context;
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.resources, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        Resource resource = resourceList.get(position);
        holder.resourceinfo.setText(resource.getResourceinfo());
        holder.resourcetitle.setText(resource.getResourcetitle());

        // Handle click event on resourcetitle TextView
        holder.resourcetitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the link here
                String url = resource.getResourcelink(); // Assuming getLink() is a method in your Resource class to get the link
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return resourceList.size();
    }


    public void setResourceList(List<Resource> resourceList) {
        this.resourceList = resourceList;
        notifyDataSetChanged();
    }
}
