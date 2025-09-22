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

public class MonthWiseActivity extends AppCompatActivity {

    private LinearLayout expensesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_wise);

        expensesContainer = findViewById(R.id.monthwise_container);
        LayoutInflater inflater = LayoutInflater.from(this);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String code = prefs.getString("currency_code", "THB");
        String symbol = CurrencyUtils.symbolFor(code);

        List<Expense> allExpenses = ExpenseDatabase
                .getDatabase(this)
                .expenseDao()
                .getAll();

        // ✅ Group by normalized month key (yyyy-MM) → then by full date (yyyy-MM-dd)
        Map<String, Map<String, List<Expense>>> grouped = new LinkedHashMap<>();
        for (Expense e : allExpenses) {
            String monthKey = formatToYearMonth(e.date);   // robust normalization
            if (monthKey == null) continue;

            if (!grouped.containsKey(monthKey)) {
                grouped.put(monthKey, new LinkedHashMap<>());
            }
            Map<String, List<Expense>> dateMap = grouped.get(monthKey);

            // keep the original stored date string as the daily key (should be yyyy-MM-dd)
            if (!dateMap.containsKey(e.date)) {
                dateMap.put(e.date, new ArrayList<>());
            }
            dateMap.get(e.date).add(e);
        }

        expensesContainer.removeAllViews();

        // Sort months chronologically (yyyy-MM sorts lexicographically fine)
        List<String> months = new ArrayList<>(grouped.keySet());
        Collections.sort(months);

        for (String monthKey : months) {
            Map<String, List<Expense>> dateMap = grouped.get(monthKey);

            // ✅ Month banner always "September - 2025"
            TextView banner = new TextView(this);
            banner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(29)
            ));
            banner.setBackgroundColor(0xFFE1C699);
            banner.setText(formatYearMonth(monthKey));
            banner.setTextSize(16);
            banner.setTypeface(Typeface.DEFAULT_BOLD);
            banner.setTextColor(0xFF000000);
            banner.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
            banner.setPadding(dp(16), 0, 0, 0);
            expensesContainer.addView(banner);

            double monthTotal = 0.0;

            // Sort dates within the month (expect yyyy-MM-dd → lexicographic is chronological)
            List<String> dates = new ArrayList<>(dateMap.keySet());
            Collections.sort(dates, Comparator.naturalOrder());

            for (String date : dates) {
                List<Expense> items = dateMap.get(date);

                // Sum total for that day
                double dayTotal = 0;
                for (Expense e : items) dayTotal += e.amount;
                monthTotal += dayTotal;

                // Row: date on left, total on right
                View row = inflater.inflate(R.layout.item_expense_date_row, expensesContainer, false);

                TextView textDescription = row.findViewById(R.id.text_description);
                TextView textCategory   = row.findViewById(R.id.text_category);
                TextView textAmount     = row.findViewById(R.id.text_amount);

                textDescription.setText(formatFullDate(date)); // "20 Sep. 2025"
                textCategory.setVisibility(View.GONE);

                String formatted = String.format(Locale.ENGLISH, "%.2f %s", dayTotal, symbol);
                SpannableString display = new SpannableString(formatted);
                int start = formatted.length() - symbol.length();
                display.setSpan(new RelativeSizeSpan(0.85f), start, formatted.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textAmount.setText(display);

                // Click → open DayDetailActivity with the *full date*
                row.setOnClickListener(v -> {
                    Intent intent = new Intent(MonthWiseActivity.this, DayDetailActivity.class);
                    intent.putExtra("selected_date", date); // yyyy-MM-dd expected
                    startActivity(intent);
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

            // TOTAL row for the month
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

            String totalFormatted = String.format(Locale.ENGLISH, "%.2f %s", monthTotal, symbol);
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

    // ✅ Normalize any stored date into yyyy-MM (handles legacy formats)
    private String formatToYearMonth(String raw) {
        if (raw == null) return null;
        String[] patterns = {
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "MM/dd/yyyy",
                "dd MMM yyyy",
                "dd MMM. yyyy"
        };
        for (String p : patterns) {
            try {
                SimpleDateFormat in = new SimpleDateFormat(p, Locale.ENGLISH);
                in.setLenient(false);
                Date d = in.parse(raw);
                if (d != null) {
                    SimpleDateFormat out = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH);
                    return out.format(d);
                }
            } catch (Exception ignore) {}
        }
        // If parsing failed (e.g., already "yyyy-MM"), return raw
        return raw;
    }

    // yyyy-MM → "September - 2025"
    private String formatYearMonth(String raw) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH);
            Date d = in.parse(raw);
            SimpleDateFormat out = new SimpleDateFormat("MMMM - yyyy", Locale.ENGLISH);
            return out.format(d);
        } catch (Exception e) {
            return raw;
        }
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
