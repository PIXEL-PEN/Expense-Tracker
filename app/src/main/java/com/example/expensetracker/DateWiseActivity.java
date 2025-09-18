package com.example.expensetracker;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DateWiseActivity extends AppCompatActivity {

    private TextView bannerDate;
    private LinearLayout expensesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_wise);

        bannerDate = findViewById(R.id.bannerDate);
        expensesContainer = findViewById(R.id.expenses_container_date);

        // Today’s date for DB (WITH period)
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdfDb = new SimpleDateFormat("dd MMM. yyyy", Locale.ENGLISH);
        String dateStored = sdfDb.format(today.getTime());   // e.g. "18 Sep. 2025"

        // Banner display (same as stored, already has the dot)
        bannerDate.setText(dateStored);

        // All expenses
        List<Expense> allExpenses = ExpenseDatabase
                .getDatabase(this)
                .expenseDao()
                .getAll();

        Toast.makeText(this,
                "Expenses in DB: " + allExpenses.size(),
                Toast.LENGTH_LONG).show();

        // Expenses for today
        List<Expense> expenses = ExpenseDatabase
                .getDatabase(this)
                .expenseDao()
                .getByExactDate(dateStored);

        Toast.makeText(this,
                "Expenses matching today (" + dateStored + "): " + expenses.size(),
                Toast.LENGTH_LONG).show();

        // Inflate rows
        LayoutInflater inflater = LayoutInflater.from(this);
        expensesContainer.removeAllViews();

        double total = 0.0;
        for (int i = 0; i < expenses.size(); i++) {
            Expense e = expenses.get(i);

            View row = inflater.inflate(R.layout.item_expense_date_row, expensesContainer, false);
            TextView textDescription = row.findViewById(R.id.text_description);
            TextView textCategory    = row.findViewById(R.id.text_category);
            TextView textAmount      = row.findViewById(R.id.text_amount);

            textDescription.setText(e.description);
            textCategory.setText(e.category);
            textAmount.setText(String.format(Locale.ENGLISH, "$%.2f", e.amount));

            expensesContainer.addView(row);

            if (i < expenses.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(0xFFCCCCCC);
                expensesContainer.addView(divider);
            }

            total += e.amount;
        }

        // Total row
        LinearLayout totalRow = new LinearLayout(this);
        totalRow.setOrientation(LinearLayout.HORIZONTAL);
        totalRow.setPadding(dp(12), dp(12), dp(12), dp(12));
        totalRow.setBackgroundColor(0xFFF5F5F5);

        TextView label = new TextView(this);
        label.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
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

    private int dp(int dps) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dps * density);
    }
}
