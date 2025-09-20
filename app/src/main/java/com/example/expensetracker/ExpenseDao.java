package com.example.expensetracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    List<Expense> getAll();

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    Expense getById(int id);

    @Query("DELETE FROM expenses")
    void clearAll();
}
