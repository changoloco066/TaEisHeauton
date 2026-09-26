package com.example.taeisheauton.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;
import android.app.PendingIntent;
import android.content.Intent;

import com.example.taeisheauton.R;
import com.example.taeisheauton.data.AppDatabase;
import com.example.taeisheauton.data.MeditationDao;
import com.example.taeisheauton.data.MeditationEntity;
import com.example.taeisheauton.data.SourceEntity;
import com.example.taeisheauton.ui.MeditationDetailActivity;
import com.example.taeisheauton.data.SourceDao;
import com.example.taeisheauton.data.SourceEntity;


public class MeditationWidgetProvider extends AppWidgetProvider {

        public static final String ACTION_REFRESH = "com.example.taeisheauton.widget.ACTION_REFRESH";

        @Override
        public void onReceive(Context context, Intent intent){
            super.onReceive(context, intent);

            if(ACTION_REFRESH.equals(intent.getAction())){
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName componentName = new ComponentName(context, MeditationWidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
                onUpdate(context, appWidgetManager, appWidgetIds);

            }
        }

        @Override
        public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            for (int appWidgetId : appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId);
            }
        }

        private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(context);
                MeditationDao meditationDao = db.meditationDao();
                SourceDao sourceDao = db.sourceDao();

                SourceEntity activeSource = sourceDao.getActive();
                MeditationEntity meditation = null;

                if(activeSource != null){
                    meditation = meditationDao.getRandomFromSource(activeSource.id);

                }

                String bookText;
                String numberText;

                if (meditation != null) {
                    bookText = "Libro " + meditation.book;
                    numberText = meditation.number + ".-";
                } else {
                    bookText = context.getString(R.string.no_meditations);
                    numberText = " ";
                }
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_meditation);
                views.setTextViewText(R.id.widgetBookText, bookText);
                views.setTextViewText(R.id.widgetNumberText, numberText);

                Intent refreshIntent = new Intent(context, MeditationWidgetProvider.class);
                refreshIntent.setAction(ACTION_REFRESH);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, refreshIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.refreshButton, pendingIntent);

                if(meditation != null){
                    Intent detailIntent = new Intent(context, MeditationDetailActivity.class);
                    detailIntent.putExtra("meditation_id", meditation.id);
                    detailIntent.setFlags(detailIntent.FLAG_ACTIVITY_NEW_TASK);

                    PendingIntent detailPendingIntent = PendingIntent.getActivity(
                      context, appWidgetId, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );
                    views.setOnClickPendingIntent(R.id.widgetBookText, detailPendingIntent);
                    views.setOnClickPendingIntent(R.id.widgetNumberText, detailPendingIntent);
                }
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }).start();
        }
    }
