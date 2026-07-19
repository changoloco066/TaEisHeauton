package com.example.taeisheauton;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.taeisheauton.data.AppDatabase;
import com.example.taeisheauton.data.MeditationDao;
import com.example.taeisheauton.data.MeditationEntity;
import com.example.taeisheauton.model.Meditation;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseTest {

    @Test
    public void insertAndGetRandom() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AppDatabase db = AppDatabase.getInstance(context);
        MeditationDao dao = db.meditationDao();

        dao.deleteAll();

        List<MeditationEntity> entities = new ArrayList<>();
        entities.add(new MeditationEntity(new Meditation(1, 15, "texto de prueba 1")));
        entities.add(new MeditationEntity(new Meditation(2, 10, "texto de prueba 2")));
        dao.insertAll(entities);

        assertEquals(2, dao.count());

        MeditationEntity random = dao.getRandom();
        assertNotNull(random);
    }
}