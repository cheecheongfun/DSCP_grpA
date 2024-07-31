package sg.edu.np.mad.greencycle.SolarForecast;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.List;

import sg.edu.np.mad.greencycle.R;

public class DateMarkerView extends MarkerView {

    private final TextView tvContent;
    private final List<String> dates;

    public DateMarkerView(Context context, int layoutResource, List<String> dates) {
        super(context, layoutResource);
        this.dates = dates;
        tvContent = findViewById(R.id.tvContent);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int index = (int) e.getX();
        if (index >= 0 && index < dates.size()) {
            tvContent.setText(dates.get(index));
        } else {
            tvContent.setText("");
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}
