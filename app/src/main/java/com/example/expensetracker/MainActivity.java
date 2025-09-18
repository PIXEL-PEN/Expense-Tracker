package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout btnAdd = findViewById(R.id.btnAdd);
        LinearLayout btnView = findViewById(R.id.btnView);
        LinearLayout btnGear = findViewById(R.id.btnGear); // <- your gear icon container

        if (btnAdd != null) {
            btnAdd.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, AddExpenseActivity.class))
            );
        }

        if (btnView != null) {
            btnView.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, ViewMenuActivity.class))
            );
        }

        if (btnGear != null) {
            // Short press → open Settings
            btnGear.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class))
            );

            // Long press → confirm then clear all expenses
            btnGear.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Clear all expenses?")
                        .setMessage("This will delete ALL records permanently. Continue?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            ExpenseDatabase.getDatabase(this).expenseDao().clearAll();
                            Toast.makeText(this, "All expenses cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }
    }
}
