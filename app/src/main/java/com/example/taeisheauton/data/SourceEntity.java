package com.example.taeisheauton.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sources")
public class SourceEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public SourceType type;
    public boolean isActive;
    public long importedAt;

    public SourceEntity() {
    }

    public SourceEntity(String name, SourceType type, boolean isActive, long importedAt) {
        this.name = name;
        this.type = type;
        this.isActive = isActive;
        this.importedAt = importedAt;
    }
}
