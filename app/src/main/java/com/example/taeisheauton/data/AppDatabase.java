package com.example.taeisheauton.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import android.content.Context;

@Database(entities = {MeditationEntity.class, SourceEntity.class}, version = 2)
@TypeConverters(Converters.class)

public abstract class AppDatabase extends RoomDatabase {

    public abstract MeditationDao meditationDao();
    public abstract SourceDao sourceDao();

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "taeisheauton-database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}