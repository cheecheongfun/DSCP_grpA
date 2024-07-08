package sg.edu.np.mad.greencycle.LiveData;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

// Fionn, S10240073K
public class LiveData extends AppCompatActivity {
    User user;
    Tank tank;
    FirebaseDatabase database;
    DatabaseReference reference;
    Interpreter tflite;
    String soil;
    TextView backButton, temp, humidity, pH, nitrogen,phosphorous,potassium, feedback1, feedback2, tankName, mlOutput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.live_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        tank = receivingEnd.getParcelableExtra("tank");

        // Call layout elements
        tankName = findViewById(R.id.tankName);
        backButton = findViewById(R.id.backButton);
        temp = findViewById(R.id.temperatureData);
        humidity = findViewById(R.id.humidityData);
        pH = findViewById(R.id.phData);
        nitrogen = findViewById(R.id.nitrogenData);
        phosphorous = findViewById(R.id.phosphorousData);
        potassium = findViewById(R.id.potassiumData);
        feedback1 = findViewById(R.id.point1);
        feedback2 = findViewById(R.id.point2);
        mlOutput = findViewById(R.id.mlOutput);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LiveData.this, TankSelection.class);
                intent.putExtra("user", user);
                intent.putExtra("where", "LiveData");
                startActivity(intent);
                finish();
            }
        });
        

        tankName.setText(tank.getTankName());
        temp.setText("Temperature: " + tank.getTemperature() + "°C");
        humidity.setText("Humidity: " + tank.getHumidity() + "%");
        pH.setText("pH Level: " + tank.getPHValue());
        nitrogen.setText("Nitrogen: " + tank.getNpkValues().get(0));
        phosphorous.setText("Phosphorous: " + tank.getNpkValues().get(1));
        potassium.setText("Potassium: " + tank.getNpkValues().get(2));

        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelDownloader.getInstance()
                .getModel("Soil_Classifier", DownloadType.LOCAL_MODEL, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(CustomModel model) {
                        // Download complete. Depending on your app, you could enable
                        // the ML feature, or switch from the local model to the remote
                        // model, etc.
                        File modelFile = model.getFile();
                        if (modelFile != null) {
                            // Load the TensorFlow Lite model
                            tflite = new Interpreter(loadModelFile(modelFile));

                            // Prepare input data
                            float[][] input = new float[1][3]; // Example input
                            input[0][0] = (float) tank.getTemperature(); // Actual temperature data
                            input[0][1] = (float) tank.getHumidity(); // Actual humidity data
                            input[0][2] = 1.0f; // Third input, could be any constant value

                            // Prepare output buffer
                            float[][] output = new float[1][4];

                            // Run inference
                            tflite.run(input, output);

                            // Interpret the model output and display predicted class label
                            int[] predictedLabels = new int[output[0].length];
                            for (int i = 0; i < output[0].length; i++) {
                                predictedLabels[i] = Math.round(output[0][i]);
                            }
                            String[] classLabels = {"Balanced", "High Nitrogen", "High Phosphorous", "High Potassium"};
                            String predictedClass = classLabels[predictedLabels[0]];

                            // Update the UI with the predicted class label
                            mlOutput.setText(predictedClass);
                        }
                    }
                });
    }
    public MappedByteBuffer loadModelFile(File modelFile) {
        try (FileInputStream inputStream = new FileInputStream(modelFile)) {
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = 0;
            long declaredLength = modelFile.length();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}