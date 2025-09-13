package com.example.expensetracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private EditText editDescription, editAmount;
    private TextView textDate;
    private AppCompatButton btnSave;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Initialize views
        spinnerCategory = findViewById(R.id.spinner_category);
        editDescription = findViewById(R.id.edit_description);
        editAmount = findViewById(R.id.edit_amount);
        textDate = findViewById(R.id.text_date);
        btnSave = findViewById(R.id.btn_save);

        // Set up spinner with custom layouts
        String[] categories = {"Food", "Transport", "Entertainment", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item_selected,
                categories
        ) {
            @Override
            public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                return getLayoutInflater().inflate(R.layout.spinner_item_dropdown, parent, false);
            }
        };
        spinnerCategory.setAdapter(adapter);

        // Initialize date to current
        selectedDate = Calendar.getInstance();
        updateDateDisplay();

        // Open date picker on click
        textDate.setOnClickListener(v -> {
            int year = selectedDate.get(Calendar.YEAR);
            int month = selectedDate.get(Calendar.MONTH);
            int day = selectedDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(
                    AddExpenseActivity.this,
                    (view, y, m, d) -> {
                        selectedDate.set(Calendar.YEAR, y);
                        selectedDate.set(Calendar.MONTH, m);
                        selectedDate.set(Calendar.DAY_OF_MONTH, d);
                        updateDateDisplay();
                    },
                    year, month, day
            );
            dpd.show();
        });

        // Save button click (example)
        btnSave.setOnClickListener(v -> {
            String category = spinnerCategory.getSelectedItem().toString();
            String description = editDescription.getText().toString();
            String amount = editAmount.getText().toString();
            String date = textDate.getText().toString();

            // TODO: Save to database or file
        });
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM. yyyy", Locale.ENGLISH);
        textDate.setText(sdf.format(selectedDate.getTime()));
    }
}
