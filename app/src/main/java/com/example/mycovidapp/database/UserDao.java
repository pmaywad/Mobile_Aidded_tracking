package com.example.mycovidapp.database;

import androidx.room.Query;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;

@Dao
public interface UserDao {
    @Query("SELECT COUNT(*) FROM User")
    public int count();

    @Query("SELECT * FROM User where date=(SELECT MAX(date) FROM User)")
    public User getLatestData();

    @Insert
    public long insert(User user);

    @Update
    public int update(User user);
}
