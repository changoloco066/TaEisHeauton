package com.example.taeisheauton.model;

public class Meditation {
private int book;
private int number;
private String text;

    public Meditation(int book, int number, String text){
        this.book = book;
        this.number = number;
        this.text = text;

    }

    public int getBook(){
        return book;
    }

    public int getNumber(){
        return number;
    }

    public String getText(){
        return text;
    }

    @Override
    public String toString(){
        return "libro "  + getBook() + " " + "numero " + getNumber() + " " + "texto " + getText();
    }


}
