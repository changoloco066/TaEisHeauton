package com.example.taeisheauton.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SourceDao {
    @Insert
    long insert(SourceEntity source);

    @Query("SELECT * FROM sources ORDER BY importedAt DESC")
    List<SourceEntity> getAll();

    @Query("SELECT * FROM sources WHERE isActive = 1 LIMIT 1")
    SourceEntity getActive();

    @Query("UPDATE sources SET isActive = 0")
    void deactivateAll();

    @Query("UPDATE sources SET isActive = 1 WHERE id = :id")
    void activate(int id);

    @Query("DELETE FROM sources WHERE id = :id")
    void delete(int id);
}

