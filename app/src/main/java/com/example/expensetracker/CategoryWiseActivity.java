package com.example.expensetracker;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

        // Get all expenses (oldest → newest)
        List<Expense> allExpenses = ExpenseDatabase
                .getDatabase(this)
                .expenseDao()
                .getAll();

        // Group by category
        Map<String, List<Expense>> grouped = new LinkedHashMap<>();
        for (Expense e : allExpenses) {
            grouped.putIfAbsent(e.category, new java.util.ArrayList<>());
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
            banner.setBackgroundColor(0xFFD3D3D3);
            banner.setText(category);
            banner.setTextSize(16);
            banner.setTypeface(Typeface.DEFAULT_BOLD);
            banner.setTextColor(0xFF000000);
            banner.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
            banner.setPadding(dp(16), 0, 0, 0);
            expensesContainer.addView(banner);

            double categoryTotal = 0.0;

            // Rows for this category
            for (int i = 0; i < items.size(); i++) {
                Expense e = items.get(i);

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(dp(12), dp(6), dp(12), dp(6));

                // Left column: description + date
                LinearLayout leftCol = new LinearLayout(this);
                leftCol.setOrientation(LinearLayout.VERTICAL);
                leftCol.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView descriptionTv = new TextView(this);
                descriptionTv.setText(e.description);
                descriptionTv.setTextSize(15);
                descriptionTv.setTypeface(Typeface.DEFAULT_BOLD);
                descriptionTv.setTextColor(0xFF000000);

                TextView dateTv = new TextView(this);
                dateTv.setText(e.date);
                dateTv.setTextSize(13);
                dateTv.setTextColor(0xFF666666);

                leftCol.addView(descriptionTv);
                leftCol.addView(dateTv);

                // Right column: amount
                TextView amountTv = new TextView(this);
                amountTv.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                amountTv.setText(String.format(Locale.ENGLISH, "$%.2f", e.amount));
                amountTv.setTextSize(15);
                amountTv.setTypeface(Typeface.DEFAULT_BOLD);
                amountTv.setTextColor(0xFF000000);

                row.addView(leftCol);
                row.addView(amountTv);
                expensesContainer.addView(row);

                // Divider
                if (i < items.size() - 1) {
                    View divider = new View(this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    divider.setLayoutParams(lp);
                    divider.setBackgroundColor(0xFFCCCCCC);
                    expensesContainer.addView(divider);
                }

                categoryTotal += e.amount;
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

            TextView totalAmountTv = new TextView(this);
            totalAmountTv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            totalAmountTv.setText(String.format(Locale.ENGLISH, "$%.2f", categoryTotal));
            totalAmountTv.setTextSize(18);
            totalAmountTv.setTypeface(Typeface.DEFAULT_BOLD);
            totalAmountTv.setTextColor(0xFFB71C1C);

            totalRow.addView(label);
            totalRow.addView(totalAmountTv);
            expensesContainer.addView(totalRow);
        }
    }

    private int dp(int dps) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dps * density);
    }
}
