package com.example.taeisheauton.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.taeisheauton.model.Meditation;

public class MeditationUpdateWorker extends Worker{

    public MeditationUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params){
        super(context, params);
    }
    @NonNull
    @Override
    public Result doWork(){
        Context context = getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, MeditationWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);

        MeditationWidgetProvider provider = new MeditationWidgetProvider();
        provider.onUpdate(context, appWidgetManager, appWidgetIds);

        return Result.success();
    }
}
