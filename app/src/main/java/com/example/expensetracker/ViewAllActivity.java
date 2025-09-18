package com.example.expensetracker;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class ViewAllActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private TextView tvTotalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all);

        recyclerView = findViewById(R.id.recycler_expenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration());


        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        // Load all expenses from DB
        List<Expense> expenses = ExpenseDatabase.getDatabase(this).expenseDao().getAll();

        // Set up adapter
        adapter = new ExpenseAdapter(expenses);
        recyclerView.setAdapter(adapter);

        // Calculate total
        double total = 0.0;
        for (Expense e : expenses) {
            total += e.amount;
        }
        tvTotalAmount.setText(String.format("$%.2f", total));

        // 👇 Keep footer above system nav bar
        View footer = findViewById(R.id.footer_bar);
        if (footer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(footer, (v, insets) -> {
                int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                int basePadding = Math.round(16f * getResources().getDisplayMetrics().density);
                v.setPadding(v.getPaddingLeft(),
                        v.getPaddingTop(),
                        v.getPaddingRight(),
                        basePadding + bottomInset);
                return insets;
            });
        }
    }
}
