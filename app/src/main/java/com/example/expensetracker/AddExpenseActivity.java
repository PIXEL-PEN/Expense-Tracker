package com.example.expensetracker;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddExpenseActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private ArrayAdapter<String> spinnerAdapter;

    // Default categories (fixed, cannot be deleted)
    private final List<String> defaultCategories = Arrays.asList(
            "Groceries",
            "Household",
            "Utilities",
            "Medical",
            "Transport",
            "Other"
    );

    // Mutable categories list
    private final List<String> categories = new ArrayList<>();
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        spinnerCategory = findViewById(R.id.spinner_category);

        // Initialize categories with defaults
        categories.clear();
        categories.addAll(defaultCategories);

        // Add special "EDIT CATEGORIES+" option at end
        categories.add("EDIT CATEGORIES+");

        spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = categories.get(position);
                if ("EDIT CATEGORIES+".equals(selected)) {
                    showEditCategoriesDialog();
                    // Reset spinner to first item after opening dialog
                    spinnerCategory.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void showEditCategoriesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Categories");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_categories, null);
        builder.setView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adapter for displaying categories with optional delete
        categoryAdapter = new CategoryAdapter(new ArrayList<>(categories));
        recyclerView.setAdapter(categoryAdapter);

        // Remove the "EDIT CATEGORIES+" entry inside dialog list
        categoryAdapter.removeEditOption();

        builder.setPositiveButton("Add", (dialog, which) -> {
            showAddCategoryDialog();
        });

        builder.setNegativeButton("Close", null);

        builder.show();
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Category");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newCategory = input.getText().toString().trim();
            if (!newCategory.isEmpty() && !categories.contains(newCategory)) {
                // Insert before "EDIT CATEGORIES+"
                categories.add(categories.size() - 1, newCategory);
                spinnerAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid or duplicate category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // ---------------- Nested RecyclerView Adapter ----------------
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        private final List<String> dialogCategories;

        CategoryAdapter(List<String> categories) {
            this.dialogCategories = categories;
        }

        void removeEditOption() {
            dialogCategories.remove("EDIT CATEGORIES+");
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_edit, parent, false);
            return new CategoryViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            String category = dialogCategories.get(position);
            holder.bind(category);
        }

        @Override
        public int getItemCount() {
            return dialogCategories.size();
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder {
            private final android.widget.TextView textCategory;
            private final android.widget.ImageView btnDelete;

            CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                textCategory = itemView.findViewById(R.id.text_category);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }

            void bind(String category) {
                textCategory.setText(category);

                if (defaultCategories.contains(category)) {
                    btnDelete.setVisibility(View.GONE);
                } else {
                    btnDelete.setVisibility(View.VISIBLE);
                    btnDelete.setOnClickListener(v -> {
                        categories.remove(category);
                        dialogCategories.remove(category);
                        notifyDataSetChanged();
                        spinnerAdapter.notifyDataSetChanged();
                        Toast.makeText(AddExpenseActivity.this, "Category deleted", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }
    }
}
