package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnAdd, btnView, btnSettings, btnAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd = findViewById(R.id.btn_add);
        btnView = findViewById(R.id.btn_view);
        btnSettings = findViewById(R.id.btn_settings);
        btnAbout = findViewById(R.id.btn_about);

        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivity(i);
        });

        btnView.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ViewMenuActivity.class);
            startActivity(i);
        });

        btnSettings.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        });

        btnAbout.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(i);
        });
    }
}
