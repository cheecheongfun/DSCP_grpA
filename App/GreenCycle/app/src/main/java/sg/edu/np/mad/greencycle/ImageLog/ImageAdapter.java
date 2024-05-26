package sg.edu.np.mad.greencycle.ImageLog;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import sg.edu.np.mad.greencycle.R;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context context;
    private List<Map<String, String>> imageDataList;

    public ImageAdapter(Context context, List<Map<String, String>> imageDataList) {
        this.context = context;
        this.imageDataList = imageDataList != null ? imageDataList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> imageData = imageDataList.get(position);
        Glide.with(context)
                .load(imageData.get("imageUrl"))
                .into(holder.imageView);
        holder.dateText.setText(imageData.get("timestamp"));
    }

    @Override
    public int getItemCount() {
        return imageDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView dateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.compostimage);
            dateText = itemView.findViewById(R.id.tvDate);
        }
    }
}
