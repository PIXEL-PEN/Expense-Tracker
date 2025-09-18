package com.example.expensetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdfDb = new SimpleDateFormat("dd MMM. yyyy", Locale.ENGLISH);
        String dateStored = sdfDb.format(today.getTime());   // e.g. "18 Sep. 2025"
        bannerDate.setText(dateStored);

        List<Expense> expenses = ExpenseDatabase
                .getDatabase(this)
                .expenseDao()
                .getByExactDate(dateStored);

        LayoutInflater inflater = LayoutInflater.from(this);
        expensesContainer.removeAllViews();

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
        }

        // NOTE: Intentionally NOT adding a total row here.
    }
}
