package com.example.expensetracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insert(Expense expense);

    // All expenses, oldest → newest
    @Query("SELECT * FROM expenses ORDER BY date ASC")
    List<Expense> getAll();

    // By category, oldest → newest
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date ASC")
    List<Expense> getByCategory(String category);

    // Exact date match, oldest → newest
    @Query("SELECT * FROM expenses WHERE date = :date ORDER BY date ASC")
    List<Expense> getByExactDate(String date);

    // Prefix match (e.g., "Sep 2025"), oldest → newest
    @Query("SELECT * FROM expenses WHERE date LIKE :prefix || '%' ORDER BY date ASC")
    List<Expense> getByDatePrefix(String prefix);

    @Query("DELETE FROM expenses")
    void clearAll();




}
