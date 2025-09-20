package com.example.expensetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Apply theme before inflating layout
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String theme = prefs.getString("theme", "Light");
        if (theme.equals("Dark")) {
            getWindow().getDecorView().setBackgroundColor(0xFF000000); // black
        } else {
            getWindow().getDecorView().setBackgroundColor(0xFFFFFFFF); // white
        }

        setContentView(R.layout.activity_main);

        LinearLayout btnAdd = findViewById(R.id.btnAdd);
        LinearLayout btnView = findViewById(R.id.btnView);
        LinearLayout btnGear = findViewById(R.id.btnGear);

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
            btnGear.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class))
            );
        }
    }
}
