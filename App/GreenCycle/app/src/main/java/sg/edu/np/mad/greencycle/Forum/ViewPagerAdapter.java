package sg.edu.np.mad.greencycle.Forum;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.List;

import sg.edu.np.mad.greencycle.R;

public class ViewPagerAdapter extends PagerAdapter {
    private List<Uri> imageUris;
    private Context context;
    private OnImageDeleteListener onImageDeleteListener;
    private OnImageClickListener onImageClickListener;

    public interface OnImageDeleteListener {
        void onImageDelete(int position);
    }

    public interface OnImageClickListener {
        void onImageClick(Uri imageUri);
    }

    public ViewPagerAdapter(List<Uri> imageUris, Context context, OnImageDeleteListener onImageDeleteListener, OnImageClickListener onImageClickListener) {
        this.imageUris = imageUris;
        this.context = context;
        this.onImageDeleteListener = onImageDeleteListener;
        this.onImageClickListener = onImageClickListener;
    }

    @Override
    public int getCount() {
        return imageUris.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item1, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);
        ImageButton deleteButton = view.findViewById(R.id.deleteButton);

        Uri imageUri = imageUris.get(position);
        Glide.with(context).load(imageUri).into(imageView);

        deleteButton.setOnClickListener(v -> {
            if (onImageDeleteListener != null) {
                onImageDeleteListener.onImageDelete(position);
            }
        });

        imageView.setOnClickListener(v -> {
            if (onImageClickListener != null) {
                onImageClickListener.onImageClick(imageUri);
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public void removeImage(int position) {
        imageUris.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
