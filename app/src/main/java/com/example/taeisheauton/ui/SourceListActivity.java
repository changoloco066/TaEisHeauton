package com.example.taeisheauton.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taeisheauton.R;
import com.example.taeisheauton.data.AppDatabase;
import com.example.taeisheauton.data.MeditationDao;
import com.example.taeisheauton.data.SourceDao;
import com.example.taeisheauton.data.SourceEntity;

import java.util.List;

public class SourceListActivity extends AppCompatActivity {

    private SourceAdapter adapter;
    private SourceDao sourceDao;
    private MeditationDao meditationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_source_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sourceRecyclerView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AppDatabase db = AppDatabase.getInstance(this);
        sourceDao = db.sourceDao();
        meditationDao = db.meditationDao();

        RecyclerView recyclerView = findViewById(R.id.sourceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSources();
    }

    private void loadSources() {
        new Thread(() -> {
            List<SourceEntity> sources = sourceDao.getAll();

            runOnUiThread(() -> {
                adapter = new SourceAdapter(sources, new SourceAdapter.OnSourceActionListener() {
                    @Override
                    public void onActivate(SourceEntity source) {
                        activateSource(source);
                    }

                    @Override
                    public void onDelete(SourceEntity source) {
                        deleteSource(source);
                    }
                });
                RecyclerView recyclerView = findViewById(R.id.sourceRecyclerView);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void activateSource(SourceEntity source) {
        new Thread(() -> {
            sourceDao.deactivateAll();
            sourceDao.activate(source.id);

            runOnUiThread(this::loadSources);
        }).start();
    }

    private void deleteSource(SourceEntity source) {
        new Thread(() -> {
            meditationDao.deleteBySource(source.id);
            sourceDao.delete(source.id);

            runOnUiThread(this::loadSources);
        }).start();
    }
}