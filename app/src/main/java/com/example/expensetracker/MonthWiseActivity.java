package com.example.expensetracker;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MonthWiseActivity extends AppCompatActivity {

    private LinearLayout expensesContainer;

    private final SimpleDateFormat parseFormat = new SimpleDateFormat("dd MMM. yyyy", Locale.ENGLISH);
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("MMM. yyyy", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_wise);

        expensesContainer = findViewById(R.id.monthwise_container);
        LayoutInflater inflater = LayoutInflater.from(this);

        // Load selected currency
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String code = prefs.getString("currency_code", "THB");
        String symbol = CurrencyUtils.symbolFor(code);

        // Get all expenses
        List<Expense> allExpenses = ExpenseDatabase
                .getDatabase(this)
                .expenseDao()
                .getAll();

        // Group by month key
        Map<String, List<Expense>> grouped = new LinkedHashMap<>();
        for (Expense e : allExpenses) {
            String monthKey = extractMonthKey(e.date);
            if (!grouped.containsKey(monthKey)) {
                grouped.put(monthKey, new ArrayList<>());
            }
            grouped.get(monthKey).add(e);
        }

        expensesContainer.removeAllViews();

        // For each month: banner → rows → total
        for (Map.Entry<String, List<Expense>> entry : grouped.entrySet()) {
            String month = entry.getKey();
            List<Expense> items = entry.getValue();

            // Month banner
            TextView banner = new TextView(this);
            banner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(36)
            ));
            banner.setBackgroundColor(0xFFE1C699);
            banner.setText(month);
            banner.setTextSize(16);
            banner.setTypeface(Typeface.DEFAULT_BOLD);
            banner.setTextColor(0xFF000000);
            banner.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
            banner.setPadding(dp(16), 0, 0, 0);
            expensesContainer.addView(banner);

            double total = 0.0;

            // Rows for each expense in that month
            for (Expense e : items) {
                View row = inflater.inflate(R.layout.item_expense_date_row, expensesContainer, false);

                TextView textDescription = row.findViewById(R.id.text_description);
                TextView textCategory = row.findViewById(R.id.text_category);
                TextView textAmount = row.findViewById(R.id.text_amount);

                textDescription.setText(e.description);
                textCategory.setText(e.date);

                // Format amount + smaller currency symbol after the number
                String formatted = String.format(Locale.ENGLISH, "%.2f %s", e.amount, symbol);
                SpannableString display = new SpannableString(formatted);
                int start = formatted.length() - symbol.length();
                display.setSpan(new RelativeSizeSpan(0.85f), start, formatted.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                textAmount.setText(display);

                expensesContainer.addView(row);

                // Divider
                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(0xFF888888);
                expensesContainer.addView(divider);

                total += e.amount;
            }

            // TOTAL row
            LinearLayout totalRow = new LinearLayout(this);
            totalRow.setOrientation(LinearLayout.HORIZONTAL);
            totalRow.setPadding(dp(12), dp(12), dp(12), dp(12));

            TextView label = new TextView(this);
            label.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            label.setText("TOTAL");
            label.setTextSize(18);
            label.setTypeface(Typeface.DEFAULT_BOLD);
            label.setTextColor(0xFFB71C1C);

            TextView amountTv = new TextView(this);
            amountTv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            String totalFormatted = String.format(Locale.ENGLISH, "%.2f %s", total, symbol);
            SpannableString totalDisplay = new SpannableString(totalFormatted);
            int start = totalFormatted.length() - symbol.length();
            totalDisplay.setSpan(new RelativeSizeSpan(0.85f), start, totalFormatted.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            amountTv.setText(totalDisplay);
            amountTv.setTextSize(18);
            amountTv.setTypeface(Typeface.DEFAULT_BOLD);
            amountTv.setTextColor(0xFFB71C1C);

            totalRow.addView(label);
            totalRow.addView(amountTv);
            expensesContainer.addView(totalRow);
        }
    }

    private String extractMonthKey(String dateStr) {
        try {
            Date parsed = parseFormat.parse(dateStr);
            if (parsed != null) {
                return displayFormat.format(parsed); // Always "Sep. 2025"
            }
        } catch (ParseException ignored) {}
        return dateStr; // fallback
    }

    private int dp(int dps) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dps * density);
    }
}
