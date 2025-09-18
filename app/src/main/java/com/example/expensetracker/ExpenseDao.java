package com.example.expensetracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insert(Expense expense);

    // Used by ViewAllActivity
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    List<Expense> getAll();

    // Optional helpers for other screens
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    List<Expense> getByCategory(String category);

    // Matches exact date string (e.g., "15 Sep. 2025")
    @Query("SELECT * FROM expenses WHERE date = :date ORDER BY rowid DESC")
    List<Expense> getByExactDate(String date);

    // Prefix match (e.g., "Sep. 2025" if you store that format)
    @Query("SELECT * FROM expenses WHERE date LIKE :prefix || '%' ORDER BY rowid DESC")
    List<Expense> getByDatePrefix(String prefix);
}
