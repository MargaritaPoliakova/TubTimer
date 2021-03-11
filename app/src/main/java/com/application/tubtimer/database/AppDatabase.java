package com.application.tubtimer.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Timer.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TimerDao timerDao();
}
