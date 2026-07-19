package com.example.taeisheauton.data;

import com.example.taeisheauton.model.Meditation;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meditations")
public class MeditationEntity{
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int book;
    public int number;
    public String text;

    public MeditationEntity(){

    }

    public MeditationEntity(Meditation m){
        this.book = m.getBook();
        this.number = m.getNumber();
        this.text = m.getText();
    }
}


