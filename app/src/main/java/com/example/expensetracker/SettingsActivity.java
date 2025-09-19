package com.example.expensetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spinnerCurrency;
    private Spinner spinnerDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // ---------------- Currency Spinner ----------------
        spinnerCurrency = findViewById(R.id.spinner_currency);

        final List<String> currencies = Arrays.asList(
                "THB — Thai Baht (฿)",
                "USD — US Dollar ($)",
                "EUR — Euro (€)",
                "GBP — British Pound (£)",
                "JPY — Japanese Yen (¥)",
                "CNY — Chinese Yuan (¥)",
                "INR — Indian Rupee (₹)",
                "AUD — Australian Dollar ($)",
                "CAD — Canadian Dollar ($)",
                "SGD — Singapore Dollar ($)",
                "HKD — Hong Kong Dollar ($)",
                "MYR — Malaysian Ringgit (RM)"
        );

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_selected,
                currencies
        );
        currencyAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        spinnerCurrency.setAdapter(currencyAdapter);

        String savedCode = prefs.getString("currency_code", "THB");
        int restoredIndex = 0;
        for (int i = 0; i < currencies.size(); i++) {
            if (currencies.get(i).startsWith(savedCode + " ")) {
                restoredIndex = i;
                break;
            }
        }
        spinnerCurrency.setSelection(restoredIndex, false);

        spinnerCurrency.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            boolean initial = true;

            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (initial) { initial = false; return; }
                String label = currencies.get(position);
                String code = label.split(" ")[0];
                prefs.edit().putString("currency_code", code).apply();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // ---------------- Date Format Spinner ----------------
        spinnerDateFormat = findViewById(R.id.spinner_date_format);

        final List<String> formats = Arrays.asList(
                "dd MMM yyyy",
                "dd/MM/yyyy",
                "MM/dd/yyyy",
                "yyyy-MM-dd",
                "EEE, dd MMM yyyy",
                "dd.MM.yyyy"
        );

        ArrayAdapter<String> formatAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_selected,
                formats
        );
        formatAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        spinnerDateFormat.setAdapter(formatAdapter);

        String savedFormat = prefs.getString("date_format", "dd MMM yyyy");
        int restoredFmtIndex = formats.indexOf(savedFormat);
        if (restoredFmtIndex >= 0) {
            spinnerDateFormat.setSelection(restoredFmtIndex, false);
        }

        spinnerDateFormat.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            boolean initial = true;

            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (initial) { initial = false; return; }
                String fmt = formats.get(position);
                prefs.edit().putString("date_format", fmt).apply();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // ---------------- Export Button ----------------
        findViewById(R.id.btn_export).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Export Data")
                    .setMessage("Choose export method:")
                    .setPositiveButton("Storage (CSV)", (dialog, which) -> exportToStorage())
                    .setNegativeButton("Email", (dialog, which) -> exportAndEmail())
                    .setNeutralButton("Cancel", null)
                    .show();
        });

        // ---------------- Reset Button ----------------
        findViewById(R.id.btn_reset).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset Database")
                    .setMessage("This will delete all expenses. Are you sure?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        ExpenseDatabase.getDatabase(this).expenseDao().clearAll();
                        Toast.makeText(this, "All expenses cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // ---------------- Helper Methods ----------------

    private void exportToStorage() {
        try {
            File file = new File(getExternalFilesDir(null), "expenses.csv");
            FileWriter writer = new FileWriter(file);

            List<Expense> expenses = ExpenseDatabase.getDatabase(this).expenseDao().getAll();
            for (Expense e : expenses) {
                writer.append(e.category).append(",")
                        .append(e.description).append(",")
                        .append(String.valueOf(e.amount)).append(",")
                        .append(e.date).append("\n");
            }
            writer.flush();
            writer.close();

            Toast.makeText(this, "Exported to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void exportAndEmail() {
        try {
            File file = new File(getExternalFilesDir(null), "expenses.csv");
            FileWriter writer = new FileWriter(file);

            List<Expense> expenses = ExpenseDatabase.getDatabase(this).expenseDao().getAll();
            for (Expense e : expenses) {
                writer.append(e.category).append(",")
                        .append(e.description).append(",")
                        .append(String.valueOf(e.amount)).append(",")
                        .append(e.date).append("\n");
            }
            writer.flush();
            writer.close();

            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/csv");
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Expense Export");
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(emailIntent, "Send email..."));

        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
