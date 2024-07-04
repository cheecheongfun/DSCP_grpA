package sg.edu.np.mad.greencycle.StartUp;
// Oh Ern Qi S10243067K
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import sg.edu.np.mad.greencycle.Classes.HashUtils;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;
import androidx.biometric.BiometricPrompt;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;
public class LoginPage extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnAuth, loginButton, registerButton;
    TextView tvAuthStatus, forgotpasswordButton;
    private Executor executor;
    private BiometricPrompt.PromptInfo promptInfo;
    private BiometricPrompt biometricPrompt;

    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_page);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
//        btnAuth = findViewById(R.id.btnAuth);
        tvAuthStatus = findViewById(R.id.tvAuthStatus);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.register);
        forgotpasswordButton = findViewById(R.id.forgotPassword);
        executor = ContextCompat.getMainExecutor(this);
        FirebaseApp.initializeApp(this);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        loginButton.setOnClickListener(view -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (!username.isEmpty() && !password.isEmpty()) {
                reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                String storedSalt = user.getSalt();
                                Log.i(null, "StoredSalt: " + storedSalt);
                                String storedHashedPassword = user.getPassword();
                                Log.i(null, "storedHashedPassword: " + storedHashedPassword);
                                String hashedEnteredPassword = HashUtils.hashPassword(password, storedSalt);
                                Log.i(null, "enteredHash: " + hashedEnteredPassword);

                                if (storedHashedPassword.equals(hashedEnteredPassword)) {
                                    Log.i(null, "Login success");
                                    Intent intent = new Intent(LoginPage.this, MainActivity.class);
                                    intent.putExtra("user", user);
                                    intent.putExtra("tab", "home_tab");
                                    startActivity(intent);
                                    finish();
                                } else {
                                    tvAuthStatus.setText("Login Failed: Invalid username or password");
                                    Log.i(null, "Login failed");
                                }
                            } else {
                                tvAuthStatus.setText("Login Failed: Invalid username or password");
                                Log.i(null, "Login failed");
                            }
                        } else {
                            tvAuthStatus.setText("Login Failed: Invalid username or password");
                            Log.i(null, "Login failed");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        tvAuthStatus.setText("Database Error: " + databaseError.getMessage());
                        Log.i(null, "Database error");
                    }
                });

            } else {
                tvAuthStatus.setText("Please enter username and password");
            }
        });

        registerButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginPage.this, RegistrationPage.class);
            startActivity(intent);
        });

        forgotpasswordButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginPage.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
//            biometricPrompt = new BiometricPrompt(LoginPage.this, executor, new BiometricPrompt.AuthenticationCallback() {
//            @Override
//            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
//                super.onAuthenticationError(errorCode, errString);
//                tvAuthStatus.setText("Error: " + errString);
//                Log.i(null, "error" + errString);
//            }
//
//            @Override
//            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
//                super.onAuthenticationSucceeded(result);
//                tvAuthStatus.setText("Successfully Auth");
//                Log.i(null, "success");
//                Intent intent = new Intent(LoginPage.this, MainActivity.class);
//                intent.putExtra("tab", "home_tab");
//                startActivity(intent);
//                finish();
//            }
//
//            @Override
//            public void onAuthenticationFailed() {
//                super.onAuthenticationFailed();
//                tvAuthStatus.setText("Authentication failed");
//                Log.i(null, "failed");
//            }
//        });
//
//        promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("Biometric Authentication")
//                .setSubtitle("Login using fingerprint or face")
//                .setNegativeButtonText("Cancel")
//                .build();
//
//        btnAuth.setOnClickListener(view -> biometricPrompt.authenticate(promptInfo));
}


//public class LoginPage extends AppCompatActivity {
//
//    Button btnAuth, loginButton,registerButton;
//    TextView tvAuthStatus;
//    private Executor executor;
//    private BiometricPrompt.PromptInfo promptInfo;
//    private BiometricPrompt biometricPrompt;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Log.i(null, "LoginPage");
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.login_page);
//
//        btnAuth = findViewById(R.id.btnAuth);
//        tvAuthStatus = findViewById(R.id.tvAuthStatus);
//        loginButton = findViewById(R.id.loginButton);
//        registerButton = findViewById(R.id.register);
//        executor = ContextCompat.getMainExecutor(this);
//        biometricPrompt = new BiometricPrompt(LoginPage.this, executor, new BiometricPrompt.AuthenticationCallback() {
//            @Override
//            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
//                super.onAuthenticationError(errorCode, errString);
//                tvAuthStatus.setText("Error: " + errString);
//                Log.i(null,"error" + errString);
//            }
//
//            @Override
//            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
//                super.onAuthenticationSucceeded(result);
//                tvAuthStatus.setText("Successfully Auth");
//                Log.i(null,"success");
//                Intent intent = new Intent(LoginPage.this, MainActivity.class);
//                intent.putExtra("tab","home_tab");
//                startActivity(intent);
//                finish();
//            }
//
//            @Override
//            public void onAuthenticationFailed() {
//                super.onAuthenticationFailed();
//                tvAuthStatus.setText("Authentication failed");
//                Log.i(null,"failed");
//            }
//        });
//
//
//        promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("Biometric Authentication")
//                .setSubtitle("Login using fingerprint or face")
//                .setNegativeButtonText("Cancel")
//                .build();
//
//        btnAuth.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                biometricPrompt.authenticate(promptInfo);
//                Log.i(null,"bio prompt");
//            }
//        });
//
//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i(null,"login button pressed");
//                Intent intent = new Intent(LoginPage.this, MainActivity.class);
//                intent.putExtra("tab","home_tab");
//                startActivity(intent);
//                finish();
//            }
//        });
//
//        registerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i(null,"register button pressed");
//                Intent intent = new Intent(LoginPage.this, RegistrationPage.class);
//                startActivity(intent);
//                finish();
//            }
//        });
//
//    }
//}