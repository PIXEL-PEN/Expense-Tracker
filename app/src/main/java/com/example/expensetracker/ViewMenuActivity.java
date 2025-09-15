package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class ViewMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_menu);

        // --- View All ---
        AppCompatButton btnViewAll = findViewById(R.id.btnViewAll);
        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(ViewMenuActivity.this, ViewAllActivity.class);
            startActivity(intent);
        });

        // --- Date Wise ---
        AppCompatButton btnDateWise = findViewById(R.id.btnDateWise);
        btnDateWise.setOnClickListener(v -> {
            Intent intent = new Intent(ViewMenuActivity.this, DateWiseActivity.class);
            startActivity(intent);
        });

        // --- Month Wise ---
        AppCompatButton btnMonthWise = findViewById(R.id.btnMonthWise);
        btnMonthWise.setOnClickListener(v -> {
            Intent intent = new Intent(ViewMenuActivity.this, MonthWiseActivity.class);
            startActivity(intent);
        });

        // --- Category Wise ---
        AppCompatButton btnCategoryWise = findViewById(R.id.btnCategoryWise);
        btnCategoryWise.setOnClickListener(v -> {
            Intent intent = new Intent(ViewMenuActivity.this, CategoryWiseActivity.class);
            startActivity(intent);
        });
    }
}
