package com.example.taeisheauton.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MeditationDao {

    @Insert
    void insertAll(List<MeditationEntity> meditations);

    @Query("DELETE FROM meditations")
    void deleteAll();

    @Query("SELECT * FROM meditation ORDER BY RANDOM() LIMIT 1")
    MeditationEntity getRandom();

    @Query("SELECT COUNT(*) FROM meditations")
    int count();
}
