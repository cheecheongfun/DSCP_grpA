package sg.edu.np.mad.greencycle.StartUp;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import sg.edu.np.mad.greencycle.R;

public class LandingPage extends AppCompatActivity {
    private ImageView animationView;
    private AnimationDrawable animationDrawable;
    private static final int SPLASH_DELAY_MILLIS = 2000;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.landing_page);

        animationView = findViewById(R.id.animate);
        animationDrawable = (AnimationDrawable) animationView.getDrawable();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(null, "Landing Page 3 seconds");
        animationDrawable.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ToLogin();
            }
        }, SPLASH_DELAY_MILLIS);
    }

    // qwqwqwqwqwqw///

    private void ToLogin() {
        Intent intent = new Intent(LandingPage.this, LoginPage.class);
        startActivity(intent);
        finish();
    }
}
