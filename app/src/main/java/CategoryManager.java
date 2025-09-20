package com.example.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryManager {
    private static final String PREFS_NAME = "categories_prefs";
    private static final String KEY_CATEGORIES = "categories";

    // Default categories
    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "Groceries", "Household", "Utilities", "Medical", "Transport", "Shopping", "Other"
    );

    public static List<String> getCategories(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(KEY_CATEGORIES, null);
        if (set == null || set.isEmpty()) {
            return new ArrayList<>(DEFAULT_CATEGORIES);
        }
        return new ArrayList<>(set);
    }

    public static void saveCategories(Context context, List<String> categories) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(categories);
        prefs.edit().putStringSet(KEY_CATEGORIES, set).apply();
    }

    public static void resetToDefault(Context context) {
        saveCategories(context, DEFAULT_CATEGORIES);
    }
}
