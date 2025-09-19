package com.example.expensetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ViewAllActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all);

        RecyclerView recyclerView = findViewById(R.id.recycler_expenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load all expenses
        List<Expense> expenses = ExpenseDatabase.getDatabase(this).expenseDao().getAll();

        // Attach adapter
        ExpenseAdapter adapter = new ExpenseAdapter(expenses);
        recyclerView.setAdapter(adapter);

        // Calculate total
        double total = 0.0;
        for (Expense e : expenses) {
            total += e.amount;
        }

        // Load user’s selected currency
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String code = prefs.getString("currency_code", "THB");
        String symbol = CurrencyUtils.symbolFor(code);

        // Format footer total with smaller currency symbol
        String formattedTotal = String.format(Locale.ENGLISH, "%.2f %s", total, symbol);
        SpannableString totalDisplay = new SpannableString(formattedTotal);
        int start = formattedTotal.length() - symbol.length();
        totalDisplay.setSpan(new RelativeSizeSpan(0.85f), start, formattedTotal.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvTotalAmount.setText(totalDisplay);
    }
}
