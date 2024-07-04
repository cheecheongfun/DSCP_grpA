package sg.edu.np.mad.greencycle.ImageLog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;

import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import java.util.Calendar;
import java.util.HashSet;

import sg.edu.np.mad.greencycle.R;

public class CustomDatePickerFragment extends DialogFragment {

    public interface OnDateSelectedListener {
        void onDatesSelected(HashSet<Calendar> selectedDates);
    }

    private HashSet<Calendar> selectedDates = new HashSet<>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_custom_date_picker, null);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar date = Calendar.getInstance();
            date.set(year, month, dayOfMonth);
            if (selectedDates.contains(date)) {
                selectedDates.remove(date);
            } else {
                selectedDates.add(date);
            }
        });

        builder.setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    OnDateSelectedListener listener = (OnDateSelectedListener) getActivity();
                    listener.onDatesSelected(selectedDates);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dismiss());
        return builder.create();
    }
}
