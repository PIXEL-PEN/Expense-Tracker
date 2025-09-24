package com.example.expensetracker;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private EditText editDescription, editAmount;
    private TextView textDate;
    private Button btnSave;

    private Expense editingExpense = null;  // holds expense when editing
    private ArrayAdapter<String> categoryAdapter;
    private List<String> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        spinnerCategory = findViewById(R.id.spinner_category);
        editDescription = findViewById(R.id.edit_description);
        editAmount = findViewById(R.id.edit_amount);
        textDate = findViewById(R.id.text_date);
        btnSave = findViewById(R.id.btn_save);

        // ✅ Load categories dynamically
        categories = new ArrayList<>(CategoryManager.getOrderedCategories(this));



        categories.add("➕ Manage Categories"); // special entry
        categoryAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_selected,   // bold for selected item
                categories
        );
        categoryAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown); // normal for dropdown
        spinnerCategory.setAdapter(categoryAdapter);

        // Handle category management
        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == categories.size() - 1) {
                    // User picked "Manage Categories"
                    showManageCategoriesDialog();
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Date picker
        textDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        String formatted = String.format(Locale.ENGLISH,
                                "%02d %s %04d",
                                dayOfMonth,
                                getMonthAbbreviation(month),
                                year);
                        textDate.setText(formatted);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        // ✅ Edit mode check
        int expenseId = getIntent().getIntExtra("expense_id", -1);
        if (expenseId != -1) {
            editingExpense = ExpenseDatabase.getDatabase(this).expenseDao().getById(expenseId);
            if (editingExpense != null) {
                editDescription.setText(editingExpense.description);
                editAmount.setText(String.valueOf(editingExpense.amount));
                textDate.setText(editingExpense.date);

                int pos = categoryAdapter.getPosition(editingExpense.category);
                if (pos >= 0) spinnerCategory.setSelection(pos);
            }
        } else {
            // ✅ Auto-seed today's date
            Calendar today = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM. yyyy", Locale.ENGLISH);
            textDate.setText(sdf.format(today.getTime()));
        }

        // ✅ Save button
        btnSave.setOnClickListener(v -> {
            String description = editDescription.getText().toString().trim();
            String amountStr = editAmount.getText().toString().trim();
            String date = textDate.getText().toString();
            String category = spinnerCategory.getSelectedItem().toString();

            if (category.equals("➕ Manage Categories")) {
                Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show();
                return;
            }

            if (description.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            ExpenseDatabase db = ExpenseDatabase.getDatabase(this);

            if (editingExpense != null) {
                editingExpense.description = description;
                editingExpense.amount = amount;
                editingExpense.date = date;
                editingExpense.category = category;
                db.expenseDao().update(editingExpense);
                Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show();
            } else {
                Expense newExpense = new Expense();
                newExpense.category = category;
                newExpense.date = date;
                newExpense.description = description;
                newExpense.amount = amount;
                db.expenseDao().insert(newExpense);
                Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show();
            }

            // ✅ Navigate back to View Menu
            Intent intent = new Intent(this, ViewMenuActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void showManageCategoriesDialog() {
        String[] options = {"Add Category", "Delete Category", "Reset Defaults"};
        new AlertDialog.Builder(this)
                .setTitle("Manage Categories")
                .setItems(options, (DialogInterface dialog, int which) -> {
                    if (which == 0) {
                        // Add
                        showAddCategoryDialog();
                    } else if (which == 1) {
                        // Delete
                        showDeleteCategoryDialog();
                    } else {
                        // Reset defaults
                        CategoryManager.resetToDefault(this);
                        reloadCategories();
                    }
                })
                .show();
    }

    private void showAddCategoryDialog() {
        final EditText input = new EditText(this);
        input.setHint("New category");
        new AlertDialog.Builder(this)
                .setTitle("Add Category")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    String newCat = input.getText().toString().trim();
                    if (!newCat.isEmpty() && !categories.contains(newCat)) {
                        categories.add(categories.size() - 1, newCat);
                        CategoryManager.saveCategories(this, categories.subList(0, categories.size() - 1));
                        categoryAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteCategoryDialog() {
        String[] cats = categories.subList(0, categories.size() - 1).toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setItems(cats, (d, which) -> {
                    String toRemove = cats[which];
                    categories.remove(toRemove);
                    CategoryManager.saveCategories(this, categories.subList(0, categories.size() - 1));
                    categoryAdapter.notifyDataSetChanged();
                })
                .show();
    }

    private void reloadCategories() {
        categories = new ArrayList<>(CategoryManager.getCategories(this));
        categories.add("➕ Manage Categories");
        categoryAdapter.clear();
        categoryAdapter.addAll(categories);
        categoryAdapter.notifyDataSetChanged();
    }

    private String getMonthAbbreviation(int monthIndex) {
        String[] months = {"Jan.", "Feb.", "Mar.", "Apr.", "May", "Jun.",
                "Jul.", "Aug.", "Sep.", "Oct.", "Nov.", "Dec."};
        return months[monthIndex];
    }
}
