package com.example.taeisheauton;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taeisheauton.data.AppDatabase;
import com.example.taeisheauton.data.MeditationDao;
import com.example.taeisheauton.data.MeditationEntity;
import com.example.taeisheauton.model.Meditation;
import com.example.taeisheauton.parser.MeditationParser;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText inputText = findViewById(R.id.inputText);
        Button importButton = findViewById(R.id.importButton);

        importButton.setOnClickListener(v -> {
            String text = inputText.getText().toString();
            MeditationParser parser = new MeditationParser();
            List<Meditation> meditations = parser.parse(text);
            List<MeditationEntity> entities = new ArrayList<>();

            for(Meditation meditation : meditations){
                entities.add(new MeditationEntity(meditation));
            }

            AppDatabase db = AppDatabase.getInstance(this);
            MeditationDao dao= db.meditationDao();
            new Thread(() ->{
                dao.deleteAll();
                dao.insertAll(entities);

                runOnUiThread(() ->{
                    Toast.makeText(this, "Se importaron " + entities.size() + "meditaciones", Toast.LENGTH_SHORT).show();
                });
            }).start();

            Toast.makeText(this, "Se importaron " + entities.size() + " meditaciones", Toast.LENGTH_SHORT).show();

        });
    }
}
