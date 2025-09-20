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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

        expensesContainer = findViewById(R.id.datewise_container);
        LayoutInflater inflater = LayoutInflater.from(this);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String code = prefs.getString("currency_code", "THB");
        String symbol = CurrencyUtils.symbolFor(code);

        List<Expense> allExpenses = ExpenseDatabase
                .getDatabase(this)
                .expenseDao()
                .getAll();

        // Group by the stored date string
        Map<String, List<Expense>> grouped = new LinkedHashMap<>();
        for (Expense e : allExpenses) {
            List<Expense> list = grouped.get(e.date);
            if (list == null) {
                list = new ArrayList<>();
                grouped.put(e.date, list);
            }
            list.add(e);
        }

        // Order: inside each date oldest -> newest
        for (Map.Entry<String, List<Expense>> entry : grouped.entrySet()) {
            List<Expense> items = entry.getValue();
            Collections.sort(items, Comparator.comparingInt(exp -> exp.id));
        }

        // Order dates by earliest id (older dates first)
        Map<String, Integer> dateMinId = new LinkedHashMap<>();
        for (Map.Entry<String, List<Expense>> entry : grouped.entrySet()) {
            int minId = Integer.MAX_VALUE;
            for (Expense e : entry.getValue()) {
                if (e.id < minId) minId = e.id;
            }
            dateMinId.put(entry.getKey(), minId);
        }
        List<String> dates = new ArrayList<>(grouped.keySet());
        Collections.sort(dates, Comparator.comparingInt(dateMinId::get));

        expensesContainer.removeAllViews();

        for (String dateKey : dates) {
            List<Expense> items = grouped.get(dateKey);

            // Banner: Month - Year (e.g., "September - 2025")
            TextView banner = new TextView(this);
            banner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(36)
            ));
            banner.setBackgroundColor(0xFFE1C699);
            banner.setText(bannerMonthYearForGroup(items));
            banner.setTextSize(16);
            banner.setTypeface(Typeface.DEFAULT_BOLD);
            banner.setTextColor(0xFF000000);
            banner.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
            banner.setPadding(dp(16), 0, 0, 0);
            expensesContainer.addView(banner);

            double total = 0.0;

            // Rows (oldest -> newest so newly added appear below older)
            for (int j = 0; j < items.size(); j++) {
                Expense e = items.get(j);

                View row = inflater.inflate(R.layout.item_expense_date_row, expensesContainer, false);

                TextView textDescription = row.findViewById(R.id.text_description);
                TextView textCategory   = row.findViewById(R.id.text_category);
                TextView textAmount     = row.findViewById(R.id.text_amount);

                if (textDescription == null || textCategory == null || textAmount == null) {
                    continue;
                }

                textDescription.setText(e.description);
                textCategory.setText(e.category);

                String formatted = String.format(Locale.ENGLISH, "%.2f %s", e.amount, symbol);
                SpannableString display = new SpannableString(formatted);
                int start = formatted.length() - symbol.length();
                display.setSpan(new RelativeSizeSpan(0.85f), start, formatted.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textAmount.setText(display);

                // Click → Edit/Delete
                row.setOnClickListener(v -> {
                    new AlertDialog.Builder(DateWiseActivity.this)
                            .setTitle("Edit or Delete")
                            .setMessage("What would you like to do?")
                            .setPositiveButton("Edit", (d, which) -> {
                                Intent intent = new Intent(DateWiseActivity.this, AddExpenseActivity.class);
                                intent.putExtra("expense_id", e.id);
                                startActivity(intent);
                            })
                            .setNegativeButton("Delete", (d, which) -> {
                                ExpenseDatabase.getDatabase(DateWiseActivity.this)
                                        .expenseDao()
                                        .delete(e);
                                recreate();
                            })
                            .setNeutralButton("Cancel", null)
                            .show();
                });

                expensesContainer.addView(row);

                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(0xFF888888);
                expensesContainer.addView(divider);

                total += e.amount;
            }

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

    private int dp(int dps) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dps * density);
    }

    /**
     * Build "MMMM - yyyy" for a given date group.
     * Tries to parse any item's date string; if no year is present, assumes current year.
     */
    private String bannerMonthYearForGroup(List<Expense> items) {
        // Try all items until one parses with a year
        for (Expense e : items) {
            String out = tryFormatMonthYear(e.date);
            if (out != null) return out;
        }
        // If still nothing worked (e.g., "20 Sep." with no year everywhere), assume current year
        String raw = items.isEmpty() ? "" : items.get(0).date;
        String guess = tryFormatMonthYearAssumingCurrentYear(raw);
        return guess != null ? guess : "Unknown - " + Calendar.getInstance().get(Calendar.YEAR);
    }

    // Try to parse when the date string already contains a year
    private String tryFormatMonthYear(String raw) {
        if (raw == null) return null;
        String[] patterns = {
                "yyyy-MM-dd",
                "dd MMM yyyy",
                "dd MMM. yyyy",
                "d MMM yyyy",
                "d MMM. yyyy",
                "MM/dd/yyyy",
                "dd/MM/yyyy",
                "yyyy/MM/dd",
                "yyyy-MM"
        };
        for (String p : patterns) {
            try {
                SimpleDateFormat in = new SimpleDateFormat(p, Locale.ENGLISH);
                in.setLenient(false);
                Date d = in.parse(raw);
                if (d != null) {
                    SimpleDateFormat out = new SimpleDateFormat("MMMM - yyyy", Locale.ENGLISH);
                    return out.format(d);
                }
            } catch (Exception ignore) { }
        }
        return null;
    }

    // If the raw string has no year (e.g., "20 Sep."), assume current year and parse.
    private String tryFormatMonthYearAssumingCurrentYear(String raw) {
        if (raw == null) return null;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String[] noYearBases = {
                "d MMM",
                "dd MMM",
                "d MMM.",
                "dd MMM."
        };
        for (String base : noYearBases) {
            try {
                // Compose a pattern with year and a composed input with the current year
                String patternWithYear = base + " yyyy"; // e.g., "d MMM yyyy" or "d MMM. yyyy"
                SimpleDateFormat in = new SimpleDateFormat(patternWithYear, Locale.ENGLISH);
                in.setLenient(false);
                Date d = in.parse(raw + " " + year);
                if (d != null) {
                    SimpleDateFormat out = new SimpleDateFormat("MMMM - yyyy", Locale.ENGLISH);
                    return out.format(d);
                }
            } catch (Exception ignore) { }
        }
        return null;
    }
}
