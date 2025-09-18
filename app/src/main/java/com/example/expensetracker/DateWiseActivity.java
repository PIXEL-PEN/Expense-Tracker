package com.example.expensetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

public class DateWiseActivity extends AppCompatActivity {

    private TextView bannerDate;
    private LinearLayout expensesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_wise);

        bannerDate = findViewById(R.id.bannerDate);
        expensesContainer = findViewById(R.id.expenses_container_date);

        // TODO: Replace this with a real selected date passed via Intent
        String selectedDate = "15 Sep. 2025";

        // Set banner text
        bannerDate.setText(selectedDate);

        // Load expenses for this date
        List<Expense> expenses = ExpenseDatabase
                .getDatabase(this)
                .expenseDao()
                .getByExactDate(selectedDate);

        LayoutInflater inflater = LayoutInflater.from(this);
        double total = 0.0;

        for (Expense e : expenses) {
            View row = inflater.inflate(R.layout.item_expense_date_row, expensesContainer, false);

            TextView textDescription = row.findViewById(R.id.text_description);
            TextView textCategory = row.findViewById(R.id.text_category);
            TextView textAmount = row.findViewById(R.id.text_amount);

            textDescription.setText(e.description);
            textCategory.setText(e.category);
            textAmount.setText(String.format("$%.2f", e.amount));

            expensesContainer.addView(row);

            // Divider (standardized color)
            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
            ));
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.dividerGray));
            expensesContainer.addView(divider);

            total += e.amount;
        }

        // Add total row (always visible)
        View totalRow = inflater.inflate(R.layout.item_expense_date_total, expensesContainer, false);
        TextView tvTotalAmount = totalRow.findViewById(R.id.tvTotalAmountDate);
        tvTotalAmount.setText(String.format("$%.2f", total));
        expensesContainer.addView(totalRow);
    }
}
