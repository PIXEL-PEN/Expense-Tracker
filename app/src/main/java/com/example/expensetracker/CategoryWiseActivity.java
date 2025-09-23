package com.example.expensetracker;

import android.content.Intent;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String code = prefs.getString("currency_code", "THB");
        String symbol = CurrencyUtils.symbolFor(code);

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

        // Sort categories by latest expense date → newest first
        List<Map.Entry<String, List<Expense>>> categories = new ArrayList<>(grouped.entrySet());
        Collections.sort(categories, (a, b) -> {
            String latestA = latestDate(a.getValue());
            String latestB = latestDate(b.getValue());
            return latestB.compareTo(latestA);
        });

        for (Map.Entry<String, List<Expense>> entry : categories) {
            String category = entry.getKey();
            List<Expense> items = entry.getValue();

            // Rows ledger style → oldest first
            Collections.sort(items, Comparator.comparing(e -> e.date));

            // Category banner
            TextView banner = new TextView(this);
            banner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(29)
            ));
            banner.setBackgroundColor(0xFFE1C699);
            banner.setText(category);
            banner.setTextSize(16);
            banner.setTypeface(Typeface.DEFAULT_BOLD);
            banner.setTextColor(0xFF000000);
            banner.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
            banner.setPadding(dp(16), 0, 0, 0);
            expensesContainer.addView(banner);

            double catTotal = 0.0;

            for (Expense e : items) {
                catTotal += e.amount;

                View row = inflater.inflate(R.layout.item_expense_date_row, expensesContainer, false);

                TextView textDescription = row.findViewById(R.id.text_description);
                TextView textCategory   = row.findViewById(R.id.text_category);
                TextView textAmount     = row.findViewById(R.id.text_amount);

                textDescription.setText(e.description);
                textCategory.setText(formatFullDate(e.date));

                String formatted = String.format(Locale.ENGLISH, "%.2f %s", e.amount, symbol);
                SpannableString display = new SpannableString(formatted);
                int start = formatted.length() - symbol.length();
                display.setSpan(new RelativeSizeSpan(0.85f), start, formatted.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textAmount.setText(display);

                // ✅ Same edit/delete dialog as DateWise
                row.setOnClickListener(v -> {
                    String details = "Category: " + e.category + "\n"
                            + "Date: " + formatFullDate(e.date) + "\n"
                            + "Item: " + e.description + "\n"
                            + "Amount: " + String.format(Locale.ENGLISH, "%.2f %s", e.amount, symbol);

                    AlertDialog dialog = new AlertDialog.Builder(CategoryWiseActivity.this)
                            .setTitle("Expense Details")
                            .setMessage(details)
                            .setNegativeButton("CLOSE", (d, which) -> d.dismiss())
                            .setNeutralButton("DELETE", (d, which) -> {
                                ExpenseDatabase.getDatabase(CategoryWiseActivity.this)
                                        .expenseDao()
                                        .delete(e);
                                recreate();
                            })
                            .setPositiveButton("EDIT", (d, which) -> {
                                Intent intent = new Intent(CategoryWiseActivity.this, AddExpenseActivity.class);
                                intent.putExtra("expense_id", e.id);
                                startActivity(intent);
                            })
                            .create();

                    dialog.show();
                });

                expensesContainer.addView(row);

                // Divider
                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(0xFF888888);
                expensesContainer.addView(divider);
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

            String totalFormatted = String.format(Locale.ENGLISH, "%.2f %s", catTotal, symbol);
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

    private int dp(int dps) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dps * density);
    }

    // Helper: latest date in a category (for sorting banners)
    private String latestDate(List<Expense> list) {
        String latest = "";
        for (Expense e : list) {
            if (e.date != null && e.date.compareTo(latest) > 0) {
                latest = e.date;
            }
        }
        return latest;
    }

    // yyyy-MM-dd → "20 Sep. 2025"
    private String formatFullDate(String raw) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date d = in.parse(raw);
            SimpleDateFormat out = new SimpleDateFormat("dd MMM. yyyy", Locale.ENGLISH);
            return out.format(d);
        } catch (Exception e) {
            return raw;
        }
    }
}
