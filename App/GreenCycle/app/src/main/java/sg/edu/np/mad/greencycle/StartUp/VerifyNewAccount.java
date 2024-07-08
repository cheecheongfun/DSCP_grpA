package sg.edu.np.mad.greencycle.StartUp;


import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.Profile.changePassword;
import sg.edu.np.mad.greencycle.Profile.changeemail;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.StartUp.JavaMailAPI;
import sg.edu.np.mad.greencycle.StartUp.LoginPage;
import sg.edu.np.mad.greencycle.StartUp.MainActivity;
import sg.edu.np.mad.greencycle.StartUp.RegistrationPage;
import sg.edu.np.mad.greencycle.StartUp.ResetPassword;


public class VerifyNewAccount extends AppCompatActivity {

    private EditText etUsername, etVerificationCode;
    private Button btnSendCode, btnSubmit, back;
    private EditText box1, box2, box3, box4, box5, box6;
    private String generatedCode;
    String Password, email, salt;

    private static final String API_KEY = "d86e3f76c011440aab7b16cb13eb8d80"; // Replace with your actual API key
    private static final String BASE_URL = "https://emailvalidation.abstractapi.com/v1/";

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        etUsername = findViewById(R.id.etForgotUsername);
        btnSendCode = findViewById(R.id.sendUsernameButton);
        btnSubmit = findViewById(R.id.submitCodeButton);
        back = findViewById(R.id.backToLoginButton);

        TextView Instruction = findViewById(R.id.forgotPasswordInstructions);
        TextView Title = findViewById(R.id.forgotPasswordText);

        Instruction.setText("Enter your Email");
        Title.setText("Create Account");

        box1 = findViewById(R.id.codeBox1);
        box2 = findViewById(R.id.codeBox2);
        box3 = findViewById(R.id.codeBox3);
        box4 = findViewById(R.id.codeBox4);
        box5 = findViewById(R.id.codeBox5);
        box6 = findViewById(R.id.codeBox6);

        // Array of EditText boxes
        final EditText[] boxes = {box1, box2, box3, box4, box5, box6};

        // Set TextChangedListener for all boxes except the last one
        for (int i = 0; i < boxes.length - 1; i++) {
            final int currentIndex = i;
            boxes[currentIndex].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1) {
                        // Move focus to the next box
                        boxes[currentIndex + 1].requestFocus();
                    }
                }
            });
        }

        // Listen for paste events in the first box (box1)
        box1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Paste the clipboard content into the boxes
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = clipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    String pasteData = clipData.getItemAt(0).getText().toString();
                    if (pasteData.length() == 6) {
                        // Populate boxes with pasted code
                        for (int i = 0; i < Math.min(pasteData.length(), boxes.length); i++) {
                            boxes[i].setText(String.valueOf(pasteData.charAt(i)));
                        }
                    } else {
                        Toast.makeText(VerifyNewAccount.this, "Invalid code length", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });

        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etUsername.getText().toString().trim();
                email = email.toLowerCase();
                if (!email.isEmpty()) {
                    String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
                    if (email.matches(emailPattern)) {
                        checkEmailInFirebase(email);
                    } else {
                        Toast.makeText(VerifyNewAccount.this, "Incorrect Email Format.", Toast.LENGTH_SHORT).show();
                    }

                    // Disable button and start countdown
                    btnSendCode.setEnabled(false);
                    new CountDownTimer(30000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            btnSendCode.setText(millisUntilFinished / 1000 + "s");
                        }

                        @Override
                        public void onFinish() {
                            btnSendCode.setEnabled(true);
                            btnSendCode.setText("Resend");
                        }
                    }.start();
                } else {
                    Toast.makeText(VerifyNewAccount.this, "Please enter your Email.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputCode = box1.getText().toString() +
                        box2.getText().toString() +
                        box3.getText().toString() +
                        box4.getText().toString() +
                        box5.getText().toString() +
                        box6.getText().toString();
                if (inputCode.equals(generatedCode)) {
                    Intent intent = new Intent(VerifyNewAccount.this, RegistrationPage.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(VerifyNewAccount.this, "Incorrect verification code.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void sendEmail(String code, String email) {
        String subject = "Your Verification Code";
        String message = "Your verification code is: " + code;
        JavaMailAPI javaMailAPI = new JavaMailAPI(email, subject, message);
        javaMailAPI.execute();
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
                runOnUiThread(() -> Toast.makeText(VerifyNewAccount.this, "Could not verify Email", Toast.LENGTH_SHORT).show());
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
                            generatedCode = generateRandomCode();
                            sendEmail(generatedCode, email);
                        } else {
                            Toast.makeText(VerifyNewAccount.this, "Email does not exist", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("EmailVerification", "API request failed with response code: " + response.code());
                    runOnUiThread(() -> Toast.makeText(VerifyNewAccount.this, "Could not verify Email", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void checkEmailInFirebase(String email) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query emailQuery = usersRef.orderByChild("email").equalTo(email);
        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(VerifyNewAccount.this, "Email is already registered.", Toast.LENGTH_SHORT).show();
                } else {
                    verifyEmail(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseCheck", "Database error: " + databaseError.getMessage());
                Toast.makeText(VerifyNewAccount.this, "Could not check email", Toast.LENGTH_SHORT).show();
            }
        });
    }
}





