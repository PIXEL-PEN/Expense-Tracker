package com.example.expensetracker;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddExpenseActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private ArrayAdapter<String> categoryAdapter;

    private final List<String> defaultCategories = Arrays.asList(
            "Groceries", "Transport", "Medical", "Clothing", "Household",
            "Electronics", "Utilities", "Personal Care", "Subscriptions", "Travel", "Other"
    );

    private List<String> userCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        spinnerCategory = findViewById(R.id.spinner_category);

        loadCategories();
        updateCategorySpinner();

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = categoryAdapter.getItem(position);
                if ("EDIT CATEGORIES+".equals(selected)) {
                    openEditCategoriesDialog();
                    // Reset selection to first item so we don’t stay stuck on the edit option
                    spinnerCategory.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateCategorySpinner() {
        List<String> allCategories = new ArrayList<>();
        allCategories.addAll(defaultCategories);
        allCategories.addAll(userCategories);
        allCategories.add("EDIT CATEGORIES+");

        categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, allCategories);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        SharedPreferences prefs = getSharedPreferences("categories", MODE_PRIVATE);
        Set<String> saved = prefs.getStringSet("userCategories", new HashSet<>());
        userCategories = new ArrayList<>(saved);
    }

    private void saveCategories() {
        SharedPreferences prefs = getSharedPreferences("categories", MODE_PRIVATE);
        prefs.edit().putStringSet("userCategories", new HashSet<>(userCategories)).apply();
    }

    private void openEditCategoriesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Categories");

        // Layout with list + input
        final ListView listView = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, userCategories);
        listView.setAdapter(adapter);

        // Long press to delete user category
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String cat = userCategories.get(position);
            userCategories.remove(position);
            adapter.notifyDataSetChanged();
            saveCategories();
            updateCategorySpinner();
            Toast.makeText(this, "Deleted: " + cat, Toast.LENGTH_SHORT).show();
            return true;
        });

        // Input for adding new category
        final EditText input = new EditText(this);
        input.setHint("New category name");

        // Wrap them in a layout
        final android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(listView);
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newCat = input.getText().toString().trim();
            if (!newCat.isEmpty() &&
                    !defaultCategories.contains(newCat) &&
                    !userCategories.contains(newCat)) {
                userCategories.add(newCat);
                saveCategories();
                updateCategorySpinner();
                Toast.makeText(this, "Added: " + newCat, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid or duplicate category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Close", null);

        builder.show();
    }
}
