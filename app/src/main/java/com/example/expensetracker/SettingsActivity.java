package com.example.expensetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

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

        // ---- Currency Spinner ----
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

        ArrayAdapter<String> adapterCurrency = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapterCurrency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapterCurrency);

        String savedCode = prefs.getString("currency_code", "THB");
        int restoredCurrencyIndex = 0;
        for (int i = 0; i < currencies.size(); i++) {
            if (currencies.get(i).startsWith(savedCode + " ")) {
                restoredCurrencyIndex = i;
                break;
            }
        }
        spinnerCurrency.setSelection(restoredCurrencyIndex, false);

        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean initial = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (initial) { initial = false; return; }
                String label = currencies.get(position);
                String code = label.split(" ")[0];
                prefs.edit().putString("currency_code", code).apply();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        // ---- Date Format Spinner ----
        spinnerDateFormat = findViewById(R.id.spinner_date_format);

        final List<String> dateFormats = Arrays.asList(
                "dd/MM/yyyy",
                "MM/dd/yyyy",
                "yyyy-MM-dd",
                "dd MMM yyyy",
                "EEE, dd MMM yyyy",
                "dd.MM.yyyy"
        );

        ArrayAdapter<String> adapterDate = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                dateFormats
        );
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDateFormat.setAdapter(adapterDate);

        String savedFormat = prefs.getString("date_format", "dd/MM/yyyy");
        int restoredFormatIndex = dateFormats.indexOf(savedFormat);
        if (restoredFormatIndex >= 0) {
            spinnerDateFormat.setSelection(restoredFormatIndex, false);
        }

        spinnerDateFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean initial = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (initial) { initial = false; return; }
                String format = dateFormats.get(position);
                prefs.edit().putString("date_format", format).apply();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
}
