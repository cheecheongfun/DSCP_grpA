package sg.edu.np.mad.greencycle.FeedingLog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sg.edu.np.mad.greencycle.Classes.FeedSchedule;
import sg.edu.np.mad.greencycle.R;

public class FeedScheduleAdapter extends ArrayAdapter<FeedSchedule> {
    private Context context;
    private List<FeedSchedule> feedSchedules;
    private int selectedIndex = 0;

    public FeedScheduleAdapter(Context context, List<FeedSchedule> feedSchedules) {
        super(context, 0, feedSchedules);
        this.context = context;
        this.feedSchedules = feedSchedules;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeedSchedule feedSchedule = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.schedule, parent, false);
        }

        TextView scheduleName = convertView.findViewById(R.id.foodText2);
        TextView scheduleName1 = convertView.findViewById(R.id.foodText3);
        CheckBox check = convertView.findViewById(R.id.check);
        RadioButton radioButton = convertView.findViewById(R.id.radioButton);

        scheduleName.setVisibility(View.GONE);
        scheduleName1.setText(feedSchedule.getScheduleName());
        check.setVisibility(View.GONE);
        radioButton.setVisibility(View.VISIBLE);

        // Manage selection state
        radioButton.setChecked(position == selectedIndex);
        radioButton.setOnClickListener(v -> {
            selectedIndex = position;
            notifyDataSetChanged();
        });

        return convertView;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
}


