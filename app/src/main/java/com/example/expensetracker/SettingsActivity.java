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

        // ---------------- Export Button (CSV to storage, HTML via email) ----------------
        findViewById(R.id.btn_export).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Export Data")
                    .setMessage("Choose export method:")
                    .setPositiveButton("Storage (CSV)", (dialog, which) -> exportCsvToStorage())
                    .setNegativeButton("Email (HTML)", (dialog, which) -> exportHtmlAndEmail())
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

    // ---------------- Helpers ----------------

    private void exportCsvToStorage() {
        try {
            File file = new File(getExternalFilesDir(null), "expenses.csv");
            writeCsv(file);
            Toast.makeText(this, "Exported CSV to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void exportHtmlAndEmail() {
        try {
            File file = new File(getExternalFilesDir(null), "expenses.html");
            writeHtml(file);

            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/html");
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Expense Export (HTML)");
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(emailIntent, "Send email..."));

        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void writeCsv(File file) throws IOException {
        List<Expense> expenses = ExpenseDatabase.getDatabase(this).expenseDao().getAll();
        FileWriter writer = new FileWriter(file);
        writer.append("Category,Description,Amount,Date\n");
        for (Expense e : expenses) {
            writer.append(escapeCsv(e.category)).append(",")
                    .append(escapeCsv(e.description)).append(",")
                    .append(String.valueOf(e.amount)).append(",")
                    .append(escapeCsv(e.date)).append("\n");
        }
        writer.flush();
        writer.close();
    }

    private void writeHtml(File file) throws IOException {
        List<Expense> expenses = ExpenseDatabase.getDatabase(this).expenseDao().getAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html><head><meta charset='utf-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
                .append("<style>")
                .append("body{font-family:system-ui,Segoe UI,Roboto,Helvetica,Arial,sans-serif;padding:16px;background:#fafafa;color:#263238;}")
                .append("h1{font-size:20px;margin:0 0 12px 0;}")
                .append("table{width:100%;border-collapse:collapse;background:white;}")
                .append("th,td{padding:10px;border:1px solid #CFD8DC;text-align:left;font-size:14px;}")
                .append("th{background:#ECEFF1;font-weight:700;}")
                .append("tr:nth-child(even){background:#F7F9FA;}")
                .append("</style></head><body>")
                .append("<h1>Expense Export</h1>")
                .append("<table><thead><tr>")
                .append("<th>Category</th><th>Description</th><th>Amount</th><th>Date</th>")
                .append("</tr></thead><tbody>");

        for (Expense e : expenses) {
            sb.append("<tr>")
                    .append("<td>").append(html(e.category)).append("</td>")
                    .append("<td>").append(html(e.description)).append("</td>")
                    .append("<td>").append(e.amount).append("</td>")
                    .append("<td>").append(html(e.date)).append("</td>")
                    .append("</tr>");
        }
        sb.append("</tbody></table></body></html>");

        FileWriter writer = new FileWriter(file);
        writer.write(sb.toString());
        writer.flush();
        writer.close();
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String out = s.replace("\"", "\"\"");
        return needQuotes ? "\"" + out + "\"" : out;
    }

    private String html(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
