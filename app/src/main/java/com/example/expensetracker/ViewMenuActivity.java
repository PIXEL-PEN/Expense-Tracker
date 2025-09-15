package com.example.expensetracker;
import android.content.Intent;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class ViewMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_menu);
        AppCompatButton btnViewAll = findViewById(R.id.btnViewAll);
        btnViewAll.setOnClickListener(v -> startActivity(new Intent(ViewMenuActivity.this, ViewAllActivity.class)));

        AppCompatButton btnViewAll = findViewById(R.id.btnViewAll);

        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(ViewMenuActivity.this, ViewAllActivity.class);
            startActivity(intent);
        });
    }
}
