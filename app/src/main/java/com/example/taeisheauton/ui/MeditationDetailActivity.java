package com.example.taeisheauton.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taeisheauton.R;
import com.example.taeisheauton.data.AppDatabase;
import com.example.taeisheauton.data.MeditationDao;
import com.example.taeisheauton.data.MeditationEntity;

public class MeditationDetailActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation_detail);

        int meditationId = getIntent().getIntExtra("meditation_id", -1);
        TextView detailText = findViewById(R.id.detailText);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            MeditationDao dao = db.meditationDao();
            MeditationEntity meditation = dao.getById(meditationId);

            runOnUiThread(() -> {
                if(meditation != null){
                    detailText.setText("Libro " + meditation.book + ", " + meditation.number + ": " + meditation.text);
                } else {
                    detailText.setText("No se encontró la meditación");
                }
            });
        }).start();
    }
}
