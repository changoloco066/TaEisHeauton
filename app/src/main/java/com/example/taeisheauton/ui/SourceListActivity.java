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

import java.util.ArrayList;
import java.util.List;

public class SourceListActivity extends AppCompatActivity {

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

        List<SourceItem> testSources = new ArrayList<>();
        testSources.add(new SourceItem("Meditaciones.pdf", true));
        testSources.add(new SourceItem("Texto pegado - 20 sept", false));
        testSources.add(new SourceItem("Reflexiones.txt", false));

        RecyclerView recyclerView = findViewById(R.id.sourceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SourceAdapter(testSources));
    }
}