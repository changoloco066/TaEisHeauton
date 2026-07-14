package com.example.taeisheauton.parser;

import com.example.taeisheauton.model.Meditation;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MeditationParser {
    private ParserState state;
    private int currentBook;
    private int currentNumber;
    private StringBuilder currentText;
    private List<Meditation> meditations;

    private static final Pattern BOOK_PATTERN = Pattern.compile("Libro\\s+([IVXL]+)");
    private static final Pattern ENTRY_PATTERN = Pattern.compile("(\\d+)\\.\\-?\\s+(.+)");
    public MeditationParser(){
        meditations = new ArrayList<>();
    }

    public List<Meditation> parse (String input){
        meditations.clear();
        state = ParserState.BEFORE_BOOKS;
        currentBook = 0;
        currentNumber = 0;
        currentText = new StringBuilder();

       String[] lines = input.split("\n");
       for(String line : lines){
           String trimmed = line.trim();

           Matcher bookMatcher = BOOK_PATTERN.matcher(trimmed);
           Matcher entryMatcher = ENTRY_PATTERN.matcher(trimmed);

           if(bookMatcher.matches()){
               if(state == ParserState.INSIDE_ENTRY){
                   Meditation meditation = new Meditation(currentBook, currentNumber, currentText.toString());
                   meditations.add(meditation);
               }
               currentBook = 0; // Placeholder, esta pendiente romanToInt(bookMatcher.group(1))
               state = ParserState.INSIDE_BOOK;
           } else if (entryMatcher.matches()){
               if(state == ParserState.INSIDE_ENTRY){
                   Meditation meditation = new Meditation(currentBook, currentNumber, currentText.toString());
                   meditations.add(meditation);
               }
                   currentNumber = Integer.parseInt(entryMatcher.group(1));
                   currentText = new StringBuilder(entryMatcher.group(2));
                   state = ParserState.INSIDE_ENTRY;
           } else if (state == ParserState.INSIDE_ENTRY && !trimmed.isEmpty()){
               currentText.append(trimmed).append(" ");

           }

       }
        if(state == ParserState.INSIDE_ENTRY) {
            Meditation meditation = new Meditation(currentBook, currentNumber, currentText.toString());
            meditations.add(meditation);
        }
        return meditations;
    }
}
