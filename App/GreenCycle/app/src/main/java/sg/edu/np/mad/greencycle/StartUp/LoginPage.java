package sg.edu.np.mad.greencycle.StartUp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import sg.edu.np.mad.greencycle.Fragments.MainActivity;
import sg.edu.np.mad.greencycle.R;
import androidx.biometric.BiometricPrompt;
import java.util.concurrent.Executor;

public class LoginPage extends AppCompatActivity {

    Button btnAuth, loginButton,registerButton;
    TextView tvAuthStatus;
    private Executor executor;
    private BiometricPrompt.PromptInfo promptInfo;
    private BiometricPrompt biometricPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(null, "LoginPage");
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_page);

        btnAuth = findViewById(R.id.btnAuth);
        tvAuthStatus = findViewById(R.id.tvAuthStatus);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.register);
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(LoginPage.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                tvAuthStatus.setText("Error: " + errString);
                Log.i(null,"error" + errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                tvAuthStatus.setText("Successfully Auth");
                Log.i(null,"success");
                Intent intent = new Intent(LoginPage.this, MainActivity.class);
                intent.putExtra("tab","home_tab");
                startActivity(intent);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                tvAuthStatus.setText("Authentication failed");
                Log.i(null,"failed");
            }
        });


        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Login using fingerprint or face")
                .setNegativeButtonText("Cancel")
                .build();

        btnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                biometricPrompt.authenticate(promptInfo);
                Log.i(null,"bio prompt");
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(null,"login button pressed");
                Intent intent = new Intent(LoginPage.this, MainActivity.class);
                intent.putExtra("tab","home_tab");
                startActivity(intent);
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(null,"register button pressed");
                Intent intent = new Intent(LoginPage.this, RegistrationPage.class);
                startActivity(intent);
                finish();
            }
        });

    }
}