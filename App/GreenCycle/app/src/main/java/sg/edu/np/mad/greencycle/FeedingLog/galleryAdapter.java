package sg.edu.np.mad.greencycle.FeedingLog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;
import java.util.Map;
import sg.edu.np.mad.greencycle.R;

public class galleryAdapter extends BaseAdapter {
    private Context context;
    public List<Map<String, String>> imageDataList;
    private ImageDeletionListener deletionListener;

    public interface ImageDeletionListener {
        void onDeleteImage(Map<String, String> imageData);
    }

    public galleryAdapter(Context context, List<Map<String, String>> imageDataList, ImageDeletionListener deletionListener) {
        this.context = context;
        this.imageDataList = imageDataList;
        this.deletionListener = deletionListener;
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
            holder.imageCard = convertView.findViewById(R.id.imageCard);
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

        holder.imageCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (deletionListener != null) {
                    deletionListener.onDeleteImage(imageData);
                }
                return false;
            }
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView dateText;
        ImageButton deleteButton;
        CardView imageCard;
    }
}
