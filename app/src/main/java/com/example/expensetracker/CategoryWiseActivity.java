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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CategoryWiseActivity extends AppCompatActivity {

    private LinearLayout expensesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_wise);

        expensesContainer = findViewById(R.id.categorywise_container);
        LayoutInflater inflater = LayoutInflater.from(this);

        // --- Load user’s chosen currency ---
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String code = prefs.getString("currency_code", "THB");
        String symbol = CurrencyUtils.symbolFor(code);

        // Get all expenses
        List<Expense> allExpenses = ExpenseDatabase
                .getDatabase(this)
                .expenseDao()
                .getAll();

        // Group by category
        Map<String, List<Expense>> grouped = new LinkedHashMap<>();
        for (Expense e : allExpenses) {
            if (!grouped.containsKey(e.category)) {
                grouped.put(e.category, new ArrayList<>());
            }
            grouped.get(e.category).add(e);
        }

        expensesContainer.removeAllViews();

        // For each category: banner → rows → total
        for (Map.Entry<String, List<Expense>> entry : grouped.entrySet()) {
            String category = entry.getKey();
            List<Expense> items = entry.getValue();

            // Category banner
            TextView banner = new TextView(this);
            banner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(36)
            ));
            banner.setBackgroundColor(0xFFE1C699);
            banner.setText(category);
            banner.setTextSize(16);
            banner.setTypeface(Typeface.DEFAULT_BOLD);
            banner.setTextColor(0xFF000000);
            banner.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
            banner.setPadding(dp(16), 0, 0, 0);
            expensesContainer.addView(banner);

            double total = 0.0;

            // Rows for each expense in the category
            for (Expense e : items) {
                View row = inflater.inflate(R.layout.item_expense_date_row, expensesContainer, false);

                TextView textDescription = row.findViewById(R.id.text_description);
                TextView textCategory    = row.findViewById(R.id.text_category);
                TextView textAmount      = row.findViewById(R.id.text_amount);

                textDescription.setText(e.description);
                textCategory.setText(e.date);

                // Amount with smaller currency symbol
                String formatted = String.format(Locale.ENGLISH, "%.2f", e.amount);
                SpannableString display = new SpannableString(symbol + " " + formatted);
                display.setSpan(new RelativeSizeSpan(0.7f), 0, symbol.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

            String totalFormatted = String.format(Locale.ENGLISH, "%.2f", total);
            SpannableString totalDisplay = new SpannableString(symbol + " " + totalFormatted);
            totalDisplay.setSpan(new RelativeSizeSpan(0.7f), 0, symbol.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            amountTv.setText(totalDisplay);

            amountTv.setTextSize(18);
            amountTv.setTypeface(Typeface.DEFAULT_BOLD);
            amountTv.setTextColor(0xFFB71C1C);

            totalRow.addView(label);
            totalRow.addView(amountTv);
            expensesContainer.addView(totalRow);
        }
    }

    private int dp(int dps) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dps * density);
    }
}
