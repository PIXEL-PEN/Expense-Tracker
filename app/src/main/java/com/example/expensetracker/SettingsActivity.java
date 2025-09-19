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

        // Restore selection
        String savedCode = prefs.getString("currency_code", "THB");
        int restoredIndex = 0;
        for (int i = 0; i < currencies.size(); i++) {
            if (currencies.get(i).startsWith(savedCode + " ")) {
                restoredIndex = i;
                break;
            }
        }
        spinnerCurrency.setSelection(restoredIndex, false);

        // Always save on change
        spinnerCurrency.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String label = currencies.get(position);   // "USD — US Dollar ($)"
                String code = label.split(" ")[0];        // "USD"
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
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
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
                    .setPositiveButton("Storage (CSV)", (dialog, which) -> {
                        exportToStorage();
                    })
                    .setNegativeButton("Email (HTML)", (dialog, which) -> {
                        exportAndEmail();
                    })
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
            File file = new File(getExternalFilesDir(null), "expenses.html");
            FileWriter writer = new FileWriter(file);

            // Write basic HTML table
            writer.append("<html><body><h2>Expenses Export</h2><table border='1'>");
            writer.append("<tr><th>Category</th><th>Description</th><th>Amount</th><th>Date</th></tr>");

            List<Expense> expenses = ExpenseDatabase.getDatabase(this).expenseDao().getAll();
            for (Expense e : expenses) {
                writer.append("<tr>")
                        .append("<td>").append(e.category).append("</td>")
                        .append("<td>").append(e.description).append("</td>")
                        .append("<td>").append(String.valueOf(e.amount)).append("</td>")
                        .append("<td>").append(e.date).append("</td>")
                        .append("</tr>");
            }

            writer.append("</table></body></html>");
            writer.flush();
            writer.close();

            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/html");
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Expense Export");
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(emailIntent, "Send email..."));

        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
