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

    @Query("SELECT * FROM meditations ORDER BY RANDOM() LIMIT 1")
    MeditationEntity getRandom();

    @Query("SELECT * FROM meditations WHERE sourceId = :sourceId ORDER BY RANDOM() LIMIT 1")
    MeditationEntity getRandomFromSource(int sourceId);

    @Query("SELECT COUNT(*) FROM meditations")
    int count();

    @Query("SELECT * FROM meditations WHERE id = :id")
    MeditationEntity getById(int id);

    @Query("DELETE FROM meditations WHERE sourceID = :sourceID")
    void deleteBySource(int sourceID);


}
