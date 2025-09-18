package com.example.expensetracker;

import android.graphics.Typeface;
import android.os.Bundle;
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

public class DateWiseActivity extends AppCompatActivity {

    private LinearLayout expensesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_wise);

        expensesContainer = findViewById(R.id.expenses_container_date);
        LayoutInflater inflater = LayoutInflater.from(this);

        // Get all expenses (oldest → newest)
        List<Expense> allExpenses = ExpenseDatabase
                .getDatabase(this)
                .expenseDao()
                .getAll();

        // Group by stored date string (e.g., "18 Sep. 2025")
        Map<String, List<Expense>> grouped = new LinkedHashMap<>();
        for (Expense e : allExpenses) {
            if (!grouped.containsKey(e.date)) {
                grouped.put(e.date, new ArrayList<>());
            }
            grouped.get(e.date).add(e);
        }

        expensesContainer.removeAllViews();

        // For each date: banner → rows → total
        for (Map.Entry<String, List<Expense>> entry : grouped.entrySet()) {
            String date = entry.getKey();
            List<Expense> daily = entry.getValue();

            // Banner (scrollable, styled)
            TextView banner = new TextView(this);
            banner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(32)
            ));
            banner.setBackgroundColor(0xFFD3D3D3);
            banner.setText(date);
            banner.setTextSize(15);
            banner.setTypeface(Typeface.DEFAULT_BOLD);
            banner.setTextColor(0xFF000000); // solid black
            banner.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
            banner.setPadding(dp(16), 0, 0, 0); // match other headers’ padding
            expensesContainer.addView(banner);

            double total = 0.0;

            // Rows
            for (Expense e : daily) {
                View row = inflater.inflate(R.layout.item_expense_date_row, expensesContainer, false);
                TextView textDescription = row.findViewById(R.id.text_description);
                TextView textCategory    = row.findViewById(R.id.text_category);
                TextView textAmount      = row.findViewById(R.id.text_amount);

                textDescription.setText(e.description);
                textCategory.setText(e.category);
                textAmount.setText(String.format(Locale.ENGLISH, "$%.2f", e.amount));

                expensesContainer.addView(row);

                // Divider after every row (including last)
                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(0xFFCCCCCC);
                expensesContainer.addView(divider);

                total += e.amount;
            }

            // TOTAL row (scrollable with body)
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
            amountTv.setText(String.format(Locale.ENGLISH, "$%.2f", total));
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
