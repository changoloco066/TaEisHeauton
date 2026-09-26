package com.example.taeisheauton;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import android.text.method.ScrollingMovementMethod;

import com.example.taeisheauton.data.AppDatabase;
import com.example.taeisheauton.data.MeditationDao;
import com.example.taeisheauton.data.MeditationEntity;
import com.example.taeisheauton.model.Meditation;
import com.example.taeisheauton.parser.MeditationParser;
import com.example.taeisheauton.widget.MeditationUpdateWorker;
import com.example.taeisheauton.data.SourceDao;
import com.example.taeisheauton.data.SourceEntity;
import com.example.taeisheauton.data.SourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PeriodicWorkRequest updateRequest = new PeriodicWorkRequest.Builder(
                MeditationUpdateWorker.class, 12, TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "meditation-daily-update",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP, updateRequest
        );

        EditText inputText = findViewById(R.id.inputText);
        Button importButton = findViewById(R.id.importButton);
        inputText.setMovementMethod(new ScrollingMovementMethod());

        importButton.setOnClickListener(v -> {
            String text = inputText.getText().toString();
            MeditationParser parser = new MeditationParser();
            List<Meditation> meditations = parser.parse(text);

            AppDatabase db = AppDatabase.getInstance(this);
            MeditationDao meditationDao = db.meditationDao();
            SourceDao sourceDao = db.sourceDao();

            new Thread(() -> {
                String sourceName = "Texto pegado - " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
                SourceEntity newSource = new SourceEntity(sourceName, SourceType.PASTE, true, System.currentTimeMillis());

                sourceDao.deactivateAll();
                long newSourceId = sourceDao.insert(newSource);

                List<MeditationEntity> entities = new ArrayList<>();
                for (Meditation meditation : meditations) {
                    MeditationEntity entity = new MeditationEntity(meditation);
                    entity.sourceId = (int) newSourceId;
                    entities.add(entity);
                }

                meditationDao.insertAll(entities);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Se importaron " + entities.size() + " meditaciones", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });
    }
}