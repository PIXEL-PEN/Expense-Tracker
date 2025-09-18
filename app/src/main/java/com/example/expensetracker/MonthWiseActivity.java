package com.example.expensetracker;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;

public class MonthWiseActivity extends AppCompatActivity {

    private LinearLayout expensesContainer;

    // Match DB reality → stored with a dot, e.g. "18 Sep. 2025"
    private final SimpleDateFormat dbFormat = new SimpleDateFormat("dd MMM. yyyy", Locale.ENGLISH);
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM - yyyy", Locale.ENGLISH); // banner
    private final SimpleDateFormat dayDisplayFormat = new SimpleDateFormat("dd MMM. yyyy", Locale.ENGLISH); // rows

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_wise);

        expensesContainer = findViewById(R.id.expenses_container_month);

        // Load all expenses
        List<Expense> allExpenses = ExpenseDatabase
                .getDatabase(this)
                .expenseDao()
                .getAll();

        // Group: Month → Day → Expenses
        Map<String, Map<String, List<Expense>>> grouped = new LinkedHashMap<>();

        for (Expense e : allExpenses) {
            try {
                Date parsed = dbFormat.parse(e.date);
                if (parsed == null) continue;

                String monthKey = monthFormat.format(parsed);     // "September - 2025"
                String dayKey = dayDisplayFormat.format(parsed);  // "18 Sep. 2025"

                grouped.putIfAbsent(monthKey, new LinkedHashMap<>());
                grouped.get(monthKey).putIfAbsent(dayKey, new ArrayList<>());
                grouped.get(monthKey).get(dayKey).add(e);

            } catch (ParseException ex) {
                // Skip malformed date
            }
        }

        expensesContainer.removeAllViews();

        // Inflate UI
        for (Map.Entry<String, Map<String, List<Expense>>> monthEntry : grouped.entrySet()) {
            String month = monthEntry.getKey();
            Map<String, List<Expense>> dailyMap = monthEntry.getValue();

            // Month banner
            TextView banner = new TextView(this);
            banner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(36)
            ));
            banner.setBackgroundColor(0xFFD3D3D3);
            banner.setText(month);
            banner.setTextSize(16);
            banner.setTypeface(Typeface.DEFAULT_BOLD);
            banner.setTextColor(0xFF000000);
            banner.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
            banner.setPadding(dp(16), 0, 0, 0);
            expensesContainer.addView(banner);

            double monthlyTotal = 0.0;

            // Each day row
            for (Map.Entry<String, List<Expense>> dayEntry : dailyMap.entrySet()) {
                String day = dayEntry.getKey();
                List<Expense> dailyExpenses = dayEntry.getValue();

                double dailyTotal = 0.0;
                for (Expense ex : dailyExpenses) {
                    dailyTotal += ex.amount;
                }
                monthlyTotal += dailyTotal;

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(dp(12), dp(6), dp(12), dp(6));

                TextView dayTv = new TextView(this);
                dayTv.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                dayTv.setText(day);
                dayTv.setTextSize(15);
                dayTv.setTypeface(Typeface.DEFAULT_BOLD);
                dayTv.setTextColor(0xFF000000);

                TextView amountTv = new TextView(this);
                amountTv.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                amountTv.setText(String.format(Locale.ENGLISH, "$%.2f", dailyTotal));
                amountTv.setTextSize(15);
                amountTv.setTypeface(Typeface.DEFAULT_BOLD);
                amountTv.setTextColor(0xFF000000);

                row.addView(dayTv);
                row.addView(amountTv);
                expensesContainer.addView(row);

                // Divider
                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(0xFFCCCCCC);
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

            TextView totalAmountTv = new TextView(this);
            totalAmountTv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            totalAmountTv.setText(String.format(Locale.ENGLISH, "$%.2f", monthlyTotal));
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
