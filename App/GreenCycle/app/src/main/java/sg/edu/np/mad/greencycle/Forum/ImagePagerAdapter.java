package sg.edu.np.mad.greencycle.Forum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.function.Consumer;

import sg.edu.np.mad.greencycle.R;

public class ImagePagerAdapter extends PagerAdapter {

    private final Context context;
    private final List<String> imageUrls;
    private final Consumer<Integer> onImageClickListener;

    public ImagePagerAdapter(Context context, List<String> imageUrls, Consumer<Integer> onImageClickListener) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.onImageClickListener = onImageClickListener;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);

        String imageUrl = imageUrls.get(position);
        Glide.with(context)
                .load(imageUrl)
                .into(imageView);

        imageView.setOnClickListener(v -> onImageClickListener.accept(position));

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
