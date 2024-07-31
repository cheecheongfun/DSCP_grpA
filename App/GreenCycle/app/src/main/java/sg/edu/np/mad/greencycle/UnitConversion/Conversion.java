package sg.edu.np.mad.greencycle.UnitConversion;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import sg.edu.np.mad.greencycle.R;

public class Conversion extends Dialog {

    private EditText inputNo;
    private Spinner inputUnit, outputUnit;
    private TextView outputNo;
    private Button convertButton;
    private ImageButton copy;
    private ImageButton close;
    private String[] units;

    public Conversion(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversion_activity);

        // Adjust dialog window dimensions
        int width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        inputNo = findViewById(R.id.input_no);
        inputUnit = findViewById(R.id.input_unit);
        outputUnit = findViewById(R.id.output_unit);
        outputNo = findViewById(R.id.output_no);
        convertButton = findViewById(R.id.convert_button);
        close = findViewById(R.id.close_button);
        inputNo.requestFocus();
        copy = findViewById(R.id.clipboard_button);

        units = getContext().getResources().getStringArray(R.array.units_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputUnit.setAdapter(adapter);
        outputUnit.setAdapter(adapter);

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convert();
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get text to copy
                String textToCopy = outputNo.getText().toString();

                // Copy to clipboard
                ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Converted Amount", textToCopy);
                clipboardManager.setPrimaryClip(clipData);

                // Notify user
                Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); // Close the dialog
            }
        });
    }

    private void convert() {
        int inputAmount;
        try {
            inputAmount = Integer.parseInt(inputNo.getText().toString());
        } catch (NumberFormatException e) {
            outputNo.setText("Invalid input");
            return;
        }

        String inputUnitStr = (String) inputUnit.getSelectedItem();
        String outputUnitStr = (String) outputUnit.getSelectedItem();

        double baseAmount = toBaseUnit(inputAmount, inputUnitStr);
        double convertedValue = fromBaseUnit(baseAmount, outputUnitStr);

        String formattedValue = String.format("%.3g %s", convertedValue, outputUnitStr);
        outputNo.setText(formattedValue);
    }

    private double toBaseUnit(int amount, String unit) {
        switch (unit) {
            case "ml":
                return amount;
            case "g":
                return amount * 1.0; // Assuming 1 g of water = 1 ml
            case "tbsp":
                return amount * 15.0; // 1 tbsp = 15 ml
            case "tsp":
                return amount * 5.0; // 1 tsp = 5 ml
            case "cup":
                return amount * 240.0; // 1 cup = 240 ml
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    private double fromBaseUnit(double amount, String unit) {
        switch (unit) {
            case "ml":
                return amount;
            case "g":
                return amount * 1.0; // Assuming 1 ml of water = 1 g
            case "tbsp":
                return amount / 15.0; // 1 tbsp = 15 ml
            case "tsp":
                return amount / 5.0; // 1 tsp = 5 ml
            case "cup":
                return amount / 240.0; // 1 cup = 240 ml
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }
}
