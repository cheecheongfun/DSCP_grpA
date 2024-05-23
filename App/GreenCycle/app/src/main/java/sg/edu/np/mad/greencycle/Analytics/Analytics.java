package sg.edu.np.mad.greencycle.Analytics;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import sg.edu.np.mad.greencycle.R;

public class Analytics extends AppCompatActivity {
    TextView tvDay, tvWeek, tvMonth, tvYear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analytics);
        tvDay = findViewById(R.id.tvDay);
        tvWeek = findViewById(R.id.tvWeek);
        tvMonth = findViewById(R.id.tvMonth);
        tvYear = findViewById(R.id.tvYear);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTextViewClick(view);
            }
        };

        tvDay.setOnClickListener(listener);
        tvWeek.setOnClickListener(listener);
        tvMonth.setOnClickListener(listener);
        tvYear.setOnClickListener(listener);
    }

    private void handleTextViewClick(View view) {
        resetTextViewColors();
        highlightTextView((TextView) view);

        if (view.getId() == R.id.tvDay) {
            loadFragment(new Analytics_day());

        }
        else if (view.getId() == R.id.tvWeek){
            loadFragment(new Analytics_week());
        }
        else if (view.getId() == R.id.tvMonth){
            loadFragment(new Analytics_month());
        }
        else if (view.getId() == R.id.tvYear){
            loadFragment(new Analytics_year());
        }
    }

    private void resetTextViewColors() {
        resetTextViewStyle(tvDay);
        resetTextViewStyle(tvWeek);
        resetTextViewStyle(tvMonth);
        resetTextViewStyle(tvYear);
    }

    private void resetTextViewStyle(TextView textView) {
        textView.setTextColor(getResources().getColor(R.color.light_grey));
        textView.setPaintFlags(textView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        textView.setText(textView.getText().toString()); // Reset any custom spans
    }

    private void highlightTextView(TextView textView) {
        textView.setTextColor(getResources().getColor(R.color.mid_green));
        SpannableString content = new SpannableString(textView.getText());
        content.setSpan(new CustomUnderlineSpan(), 0, content.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(content);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.analytics_frag, fragment);
        transaction.commit();
    }

    public static class CustomUnderlineSpan extends UnderlineSpan {
        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(true);
            ds.setStrokeWidth(2); // Adjust thickness as needed
        }
    }
}
