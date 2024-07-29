package sg.edu.np.mad.greencycle.Analytics;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.Classes.Tank;
import sg.edu.np.mad.greencycle.TankSelection.TankSelection;
import sg.edu.np.mad.greencycle.R;

public class Analytics extends AppCompatActivity {
    TextView tvDay, tvWeek, tvMonth, tvYear, back,temp;
    User user;
    Tank tank;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analytics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.analyticsPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");

        tvDay = findViewById(R.id.tvDay);
        tvWeek = findViewById(R.id.tvWeek);
        tvYear = findViewById(R.id.tvYear);
        back = findViewById(R.id.backButton);
        temp = findViewById(R.id.button2);

        temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),DeleteDAOActivity.class);
                startActivity(intent);
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTextViewClick(view);
            }
        };

        tvDay.setOnClickListener(listener);
        tvWeek.setOnClickListener(listener);
        tvYear.setOnClickListener(listener);

        loadFragment(new Analytics_day(), user, tank); // Pass user and tank objects
        highlightTextView(tvDay);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Analytics.this, TankSelection.class);
                intent.putExtra("user", user);
                intent.putExtra("where", "Analytics");
                startActivity(intent);
            }
        });
    }

    private void handleTextViewClick(View view) {
        resetTextViewColors();
        highlightTextView((TextView) view);

        Fragment fragment = null;
        if (view.getId() == R.id.tvDay) {
            fragment = new Analytics_day();
        } else if (view.getId() == R.id.tvWeek) {
            fragment = new Week_charts();
        } else if (view.getId() == R.id.tvYear) {
            fragment = new Analytics_year();
        }

        if (fragment != null) {
            loadFragment(fragment, user, tank);
        }
    }

    private void loadFragment(Fragment fragment, User user, Tank tank) {
        Bundle args = new Bundle();
        args.putParcelable("user", user);
        args.putParcelable("tank", tank);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.analytics_frag, fragment);
        transaction.commit();
    }

    private void resetTextViewColors() {
        resetTextViewStyle(tvDay);
        resetTextViewStyle(tvWeek);
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

    public static class CustomUnderlineSpan extends UnderlineSpan {
        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(true);
            ds.setStrokeWidth(2); // Adjust thickness as needed
        }
    }
}
