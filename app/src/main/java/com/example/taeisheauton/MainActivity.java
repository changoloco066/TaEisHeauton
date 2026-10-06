package com.example.taeisheauton;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import com.example.taeisheauton.data.AppDatabase;
import com.example.taeisheauton.data.MeditationDao;
import com.example.taeisheauton.data.MeditationEntity;
import com.example.taeisheauton.data.SourceDao;
import com.example.taeisheauton.data.SourceEntity;
import com.example.taeisheauton.data.SourceType;
import com.example.taeisheauton.model.Meditation;
import com.example.taeisheauton.parser.MeditationParser;
import com.example.taeisheauton.ui.SourceCardAdapter;
import com.example.taeisheauton.ui.SourceListActivity;
import com.example.taeisheauton.widget.MeditationUpdateWorker;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private SourceCardAdapter cardAdapter;
    private TextView emptyStateText;
    private SourceDao sourceDao;
    private MeditationDao meditationDao;
    private SourceType pendingFileType;

    private final ActivityResultLauncher<String> filePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null || pendingFileType == null) return;
                importFromUri(uri, pendingFileType);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PeriodicWorkRequest updateRequest = new PeriodicWorkRequest.Builder(
                MeditationUpdateWorker.class, 12, TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "meditation-daily-update",
                ExistingPeriodicWorkPolicy.KEEP, updateRequest
        );

        AppDatabase db = AppDatabase.getInstance(this);
        meditationDao = db.meditationDao();
        sourceDao = db.sourceDao();

        PDFBoxResourceLoader.init(getApplicationContext());

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.inflateMenu(R.menu.main_menu);
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_sources) {
                startActivity(new Intent(MainActivity.this, SourceListActivity.class));
                return true;
            }
            return false;
        });

        emptyStateText = findViewById(R.id.emptyStateText);
        RecyclerView recyclerView = findViewById(R.id.sourcesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        cardAdapter = new SourceCardAdapter(new ArrayList<>(), source -> {
            new Thread(() -> {
                sourceDao.deactivateAll();
                sourceDao.activate(source.id);
                loadSources();
            }).start();
        });
        recyclerView.setAdapter(cardAdapter);

        FloatingActionButton fab = findViewById(R.id.addSourceFab);
        fab.setOnClickListener(v -> {
            String[] options = {"Pegar texto", "Importar PDF", "Importar TXT"};
            new AlertDialog.Builder(this)
                    .setTitle("Agregar fuente")
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                showPasteDialog();
                                break;
                            case 1:
                                pendingFileType = SourceType.PDF;
                                filePicker.launch("application/pdf");
                                break;
                            case 2:
                                pendingFileType = SourceType.TXT;
                                filePicker.launch("text/*");
                                break;
                        }
                    })
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(this::loadSources).start();
    }

    private void loadSources() {
        List<SourceEntity> sources = sourceDao.getAll();
        runOnUiThread(() -> {
            cardAdapter.setSources(sources);
            emptyStateText.setVisibility(sources.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void showPasteDialog() {
        EditText input = new EditText(this);
        input.setHint("Pega aquí el texto de tus meditaciones");
        input.setMinLines(8);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        input.setMovementMethod(new ScrollingMovementMethod());

        new AlertDialog.Builder(this)
                .setTitle("Pegar texto")
                .setView(input)
                .setPositiveButton("Importar", (dialog, which) ->
                        importPastedText(input.getText().toString()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void importFromUri(android.net.Uri uri, SourceType type) {
        new Thread(() -> {
            try {
                String text;
                if (type == SourceType.PDF) {
                    try (java.io.InputStream in = getContentResolver().openInputStream(uri);
                         PDDocument doc = PDDocument.load(in)) {
                        text = new PDFTextStripper().getText(doc);
                    }
                } else {
                    try (java.io.InputStream in = getContentResolver().openInputStream(uri);
                         java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream()) {
                        byte[] chunk = new byte[4096];
                        int read;
                        while ((read = in.read(chunk)) != -1) {
                            buffer.write(chunk, 0, read);
                        }
                        text = new String(buffer.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
                    }
                }

                String fileName = "Fuente - " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
                android.database.Cursor c = getContentResolver().query(uri, null, null, null, null);
                if (c != null) {
                    int idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (c.moveToFirst() && idx >= 0) fileName = c.getString(idx);
                    c.close();
                }

                importFromText(text, type, fileName);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "No se pudo leer el archivo", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void importFromText(String text, SourceType type, String sourceName) {
        MeditationParser parser = new MeditationParser();
        List<Meditation> meditations = parser.parse(text);

        new Thread(() -> {
            SourceEntity newSource = new SourceEntity(sourceName, type, true, System.currentTimeMillis());

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
                loadSources();
            });
        }).start();
    }

    private void importPastedText(String text) {
        importFromText(text, SourceType.PASTE,
                "Texto pegado - " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));
    }

}
