package com.application.tubtimer.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface TimerDao {
    @Query("SELECT * FROM Timer")
    List<Timer> getAll();

    @Query("SELECT * FROM Timer WHERE number = :number")
    Timer getByNumber(int number);

    @Query("SELECT * FROM Timer WHERE type = :type")
    List<Timer> getByType(int type);

    @Query("DELETE FROM Timer WHERE number = :number")
    void  deleteByNumber(int number);

    @Insert
    void insert(Timer timer);

    @Update
    void update(Timer timer);

    @Delete
    void delete(Timer timer);

}