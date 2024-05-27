package sg.edu.np.mad.greencycle.ImageLog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;
import java.util.Map;
import sg.edu.np.mad.greencycle.R;

public class galleryAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, String>> imageDataList;

    public galleryAdapter(Context context, List<Map<String, String>> imageDataList) {
        this.context = context;
        this.imageDataList = imageDataList;
    }

    @Override
    public int getCount() {
        return imageDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.compostimage);
            holder.dateText = convertView.findViewById(R.id.tvDate); // If you want to show the date
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String, String> imageData = imageDataList.get(position);
        Glide.with(context)
                .load(imageData.get("imageUrl"))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.uploadicon)
                .into(holder.imageView);

        if (holder.dateText != null) {
            holder.dateText.setText(imageData.get("timestamp"));
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView dateText;  // Optional: Only if you included a TextView in image_item.xml
    }
}
