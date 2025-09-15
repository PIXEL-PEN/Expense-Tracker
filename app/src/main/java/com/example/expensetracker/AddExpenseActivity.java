package com.example.expensetracker;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AddExpenseActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private ArrayAdapter<String> spinnerAdapter;
    private TextView textDate;
    private Calendar selectedDate;

    // SharedPreferences keys
    private static final String PREFS_NAME = "ExpensePrefs";
    private static final String KEY_USER_CATEGORIES = "UserCategories";

    // Default categories (fixed, cannot be deleted)
    private final List<String> defaultCategories = Arrays.asList(
            "Groceries",
            "Household",
            "Utilities",
            "Medical",
            "Transport",
            "Other"
    );

    // Mutable categories list (defaults + user + "EDIT CATEGORIES+")
    private final List<String> categories = new ArrayList<>();
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        spinnerCategory = findViewById(R.id.spinner_category);
        textDate = findViewById(R.id.text_date);

        // Load categories
        loadCategories();

        // Custom adapter for spinner with bold salmon selected & normal dropdown
        spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_selected, categories) {
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = getLayoutInflater().inflate(R.layout.spinner_item_dropdown, parent, false);
                TextView tv = view.findViewById(R.id.spinner_dropdown_text);
                tv.setText(getItem(position));
                return view;
            }

            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = getLayoutInflater().inflate(R.layout.spinner_item_selected, parent, false);
                TextView tv = view.findViewById(R.id.spinner_selected_text);
                tv.setText(getItem(position));
                return view;
            }
        };
        spinnerCategory.setAdapter(spinnerAdapter);

        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = categories.get(position);
                if ("EDIT CATEGORIES+".equals(selected)) {
                    showEditCategoriesDialog();
                    spinnerCategory.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // --- DATE SEEDING ---
        selectedDate = Calendar.getInstance();
        updateDateDisplay();

        textDate.setOnClickListener(v -> {
            int year = selectedDate.get(Calendar.YEAR);
            int month = selectedDate.get(Calendar.MONTH);
            int day = selectedDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(
                    AddExpenseActivity.this,
                    (view, y, m, d) -> {
                        selectedDate.set(Calendar.YEAR, y);
                        selectedDate.set(Calendar.MONTH, m);
                        selectedDate.set(Calendar.DAY_OF_MONTH, d);
                        updateDateDisplay();
                    },
                    year, month, day
            );
            dpd.show();
        });
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM. yyyy", Locale.ENGLISH);
        textDate.setText(sdf.format(selectedDate.getTime()));
    }

    // -------------------- Category Persistence --------------------

    private void saveUserCategories(List<String> userCategories) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putStringSet(KEY_USER_CATEGORIES, new HashSet<>(userCategories)).apply();
    }

    private List<String> loadUserCategoriesFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(KEY_USER_CATEGORIES, new HashSet<>());
        return new ArrayList<>(set);
    }

    private void loadCategories() {
        categories.clear();
        categories.addAll(defaultCategories);

        List<String> userCategories = loadUserCategoriesFromPrefs();
        categories.addAll(userCategories);

        categories.add("EDIT CATEGORIES+");
    }

    // -------------------- Dialogs --------------------

    private void showEditCategoriesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Categories");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_categories, null);
        builder.setView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> dialogCategories = new ArrayList<>(categories);
        dialogCategories.remove("EDIT CATEGORIES+");
        categoryAdapter = new CategoryAdapter(dialogCategories);
        recyclerView.setAdapter(categoryAdapter);

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
                categories.add(categories.size() - 1, newCategory);
                saveUserCategories(getUserCategoriesOnly());
                spinnerAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid or duplicate category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private List<String> getUserCategoriesOnly() {
        List<String> userCategories = new ArrayList<>(categories);
        userCategories.removeAll(defaultCategories);
        userCategories.remove("EDIT CATEGORIES+");
        return userCategories;
    }

    // ---------------- RecyclerView Adapter for Categories ----------------

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        private final List<String> dialogCategories;

        CategoryAdapter(List<String> categories) {
            this.dialogCategories = categories;
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
                        saveUserCategories(getUserCategoriesOnly());
                        notifyDataSetChanged();
                        spinnerAdapter.notifyDataSetChanged();
                        Toast.makeText(AddExpenseActivity.this, "Category deleted", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }
    }
}
