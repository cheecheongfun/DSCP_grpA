package sg.edu.np.mad.greencycle.StartUp;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sg.edu.np.mad.greencycle.R;

import java.io.IOException;

public class EmailVerificationActivity extends AppCompatActivity {

    private static final String API_KEY = "d86e3f76c011440aab7b16cb13eb8d80"; // Replace with your actual API key
    private static final String BASE_URL = "https://emailvalidation.abstractapi.com/v1/";

    private EditText emailEditText;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emailverifier);

        emailEditText = findViewById(R.id.emailEditText);
        resultTextView = findViewById(R.id.resultTextView);
        Button verifyButton = findViewById(R.id.verifyButton);

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                verifyEmail(email);
            }
        });
    }

    private void verifyEmail(String email) {
        OkHttpClient client = new OkHttpClient();

        String url = BASE_URL + "?api_key=" + API_KEY + "&email=" + email;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("EmailVerification", "API request error: " + e.getMessage());
                runOnUiThread(() -> resultTextView.setText("API request error."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    boolean isValidFormat = jsonResponse.get("is_valid_format").getAsJsonObject().get("value").getAsBoolean();
                    boolean isSmtpValid = jsonResponse.get("is_smtp_valid").getAsJsonObject().get("value").getAsBoolean();

                    runOnUiThread(() -> {
                        if (isValidFormat && isSmtpValid) {
                            resultTextView.setText("Email is valid.");
                        } else {
                            resultTextView.setText("Email is invalid.");
                        }
                    });
                } else {
                    Log.e("EmailVerification", "API request failed with response code: " + response.code());
                    runOnUiThread(() -> resultTextView.setText("API request failed."));
                }
            }
        });
    }
}


