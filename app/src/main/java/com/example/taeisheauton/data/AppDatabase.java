package com.example.taeisheauton.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {MeditationEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MeditationDao meditationDao();

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "taeisheauton-database"
            ).build();
        }
        return instance;
    }
}